package uk.gov.hmcts.appregister.common.async;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.appregister.common.async.exception.JobException;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycleEvent;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.async.model.TrackJobStatusResponse;
import uk.gov.hmcts.appregister.common.async.reader.DataReader;
import uk.gov.hmcts.appregister.common.async.reader.PageReader;
import uk.gov.hmcts.appregister.common.async.reader.ReadPagePosition;
import uk.gov.hmcts.appregister.common.async.validator.StartJobValidator;
import uk.gov.hmcts.appregister.generated.model.JobStatus;

/**
 * A default implementation of the {@link AsyncJobService} interface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Setter
public class AsyncJobServiceImpl implements AsyncJobService {
    /** decouples the core lifecycles of an async job from its state management. */
    private final JobStatusPersistence persistence;

    private final TransactionalUnitOfWork transactionalUnitOfWork;

    /**
     * If executed within a Spring context then ensure that the job read page size is set in yaml.
     */
    @Value("${appreg.job.page-size}")
    private int pageSize;

    /** Validates whether a job can be started. */
    private final StartJobValidator validator;

    /**
     * A shared executor. We use virtual threads here as most of the processing will be IO which
     * should mean our service can scale better than a more traditional thread pool.
     */
    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    @Transactional
    public <T> TrackJobStatusResponse startJob(
            JobTypeRequest jobTypeRequest,
            DataReader<T> dataReader,
            AsyncJobLifecycle<T> lifecycle) {
        return startJob(jobTypeRequest, dataReader, null, lifecycle);
    }

    @Override
    @Transactional
    public <T> TrackJobStatusResponse startJob(
            JobTypeRequest jobRequest,
            DataReader<T> dataReader,
            PageReader<T> pageImport,
            AsyncJobLifecycle<T> lifecycle) {

        JobContext jobContext = new JobContext();

        // validate that the job is not already running
        validator.validate(jobRequest);

        // start job synchronously to this thread
        JobIdRequest id = persistence.startJob(jobRequest);

        // get the response data
        JobStatusResponse jobStatusResponse = persistence.getJobStatus(id).get();

        ReadPagePosition position = new ReadPagePosition(pageSize, 0);

        AsyncLifecycleProcessor<T> process =
                new AsyncLifecycleProcessor<>(
                        position, dataReader, jobStatusResponse, lifecycle, jobContext, pageImport);

        // the core import logic will be processed in a seperate thread
        Future<?> futureJobOutcome = executor.submit(process);

        return new TrackJobStatusResponse(getJobStatus(id).get(), futureJobOutcome);
    }

    @Transactional
    @Override
    public Optional<JobStatusResponse> getJobStatus(JobIdRequest jobId) {
        return persistence.getJobStatus(jobId);
    }

    /**
     * A runnable implementation that will process the async job within its own database
     * transaction.
     */
    @RequiredArgsConstructor
    class AsyncLifecycleProcessor<T> implements Runnable {

        private final ReadPagePosition position;

        private final DataReader<T> dataReader;

        private final JobStatusResponse jobStatusResponse;

        private final AsyncJobLifecycle<T> lifecycle;

        private final JobContext jobContext;

        private final PageReader<T> pageRead;

        @Override
        public void run() {
            // run the runnable inside of a database transaction
            transactionalUnitOfWork.inTransaction(
                    () -> {
                        try (dataReader) {
                            fireEventAndChangeState(
                                    jobStatusResponse,
                                    null,
                                    JobStatus.RECEIVED,
                                    lifecycle,
                                    jobContext);

                            dataReader.readData(
                                    position,
                                    (data, jobContext) -> {

                                        // decide wether to fail or continue
                                        handleFailure(
                                                jobContext, jobStatusResponse, JobStatus.RECEIVED);

                                        // validate the read page of data
                                        fireEventAndChangeState(
                                                jobStatusResponse,
                                                data,
                                                JobStatus.VALIDATING,
                                                lifecycle,
                                                jobContext);

                                        // if we dont have a page read callback then do not call.
                                        if (pageRead != null) {
                                            // process the data
                                            pageRead.readData(data, jobContext);
                                        }

                                        // decide wether to fail or continue
                                        handleFailure(
                                                jobContext,
                                                jobStatusResponse,
                                                JobStatus.VALIDATING);

                                        // if a failure has been detected then stop processing and
                                        // just ensure that
                                        // validation is captured for all. We do not support partial
                                        // fails at this point in time
                                        if (!jobContext.hasFailure()) {
                                            // process the state with the read data
                                            fireEventAndChangeState(
                                                    jobStatusResponse,
                                                    data,
                                                    JobStatus.PROCESSING,
                                                    lifecycle,
                                                    jobContext);

                                            // decide wether to fail or continue
                                            handleFailure(
                                                    jobContext,
                                                    jobStatusResponse,
                                                    JobStatus.PROCESSING);
                                        }
                                    },
                                    jobContext);

                            // if a failure was detected then fail else complete the job
                            if (jobContext.hasFailure()) {
                                throw new JobException(
                                        "Failed to process job: "
                                                + jobStatusResponse.getJobId().getId());
                            } else {
                                fireEventAndChangeState(
                                        jobStatusResponse,
                                        null,
                                        JobStatus.COMPLETED,
                                        lifecycle,
                                        jobContext);
                            }
                        } catch (Throwable t) {
                            log.error("Error processing job", t);

                            try {
                                fireEventAndChangeState(
                                        jobStatusResponse,
                                        null,
                                        JobStatus.FAILED,
                                        lifecycle,
                                        jobContext);
                            } catch (IOException e) {
                                log.error("Error calling failure lifecycle", e);
                            }

                            // if this is a job exception then log the error
                            if (t instanceof JobException) {
                                jobContext.logFailure(t.getMessage());
                            }

                            // set the fail state
                            persistence.setFailure(
                                    jobStatusResponse.getJobId(),
                                    jobContext.getCommaDelimitedFailureMessage() == null
                                            ? "Failed with unknown error"
                                            : jobContext.getCommaDelimitedFailureMessage());

                            // now force a failure to roll back any database transactional data that
                            // was commited
                            // in this transaction. This sits irrespective to
                            // any state transitions that may have been made for the job process.
                            throw new RuntimeException(t);
                        }
                    });
        }

        /**
         * fire an event and change the state of the underlying job.
         *
         * @param response The jobn to fire the event for and the forthcoming state transition.
         * @param data The read data to pass to the lifecycle event.
         * @param status The status to set the job to.
         * @param lifecycle The lifecycle to fire the event for.
         * @param context The job context to pass to the lifecycle event.
         */
        private <T> void fireEventAndChangeState(
                JobStatusResponse response,
                List<T> data,
                JobStatus status,
                AsyncJobLifecycle<T> lifecycle,
                JobContext context)
                throws IOException {
            log.debug("Processing {} for job {}", status, response.getJobId());

            // ensure we set the status
            persistence.setJobStatus(response.getJobId(), status);

            // fire the lifecycle event
            lifecycle.lifeCycleEventPerformed(
                    new AsyncJobLifecycleEvent<T>(response, data, context, status));

            log.debug("Processed {} for job {}", status, response.getJobId());
        }
    }

    /**
     * Decide how to handle a failure.
     *
     * @param jobContext The job context containing the failure message.
     * @param jobStatusResponse The job status response containing the job id.
     */
    private void handleFailure(
            JobContext jobContext, JobStatusResponse jobStatusResponse, JobStatus status)
            throws JobException {
        // if we have a failure but we want to validate all other
        // results then keep going.
        if (jobContext.hasFailure() && jobContext.isStoppedValidating()) {
            throw new JobException(
                    "Job failed during %s for job %s. Forced termination"
                            .formatted(status, jobStatusResponse.getJobId().getId()));
        }
    }
}
