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
import uk.gov.hmcts.appregister.common.async.reader.PageRead;
import uk.gov.hmcts.appregister.common.async.reader.ReadPagePosition;
import uk.gov.hmcts.appregister.common.async.validator.StartJobValidator;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.generated.model.JobStatus;
import uk.gov.hmcts.appregister.generated.model.JobType;

/**
 * A default implementation of the {@link AsyncJobService} interface.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@Setter
public class AsyncJobServiceImpl implements AsyncJobService {
    /** decouples the core lifecyle of an async job from its state management. */
    private final JobStatusPersistence persistence;

    @Value("${appreg.job.page-size}")
    private int pageSize;

    private final StartJobValidator validator;

    /**
     * A shared executor. We use virtual threads asa most of the processing will be IO which should
     * mean our service can scale.
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
            PageRead<T> pageImport,
            AsyncJobLifecycle<T> lifecycle) {

        JobContext jobContext = new JobContext();

        // validate that the job is not already running
        validator.validate(jobRequest);

        // start job synchronously to this thread
        JobIdRequest id = persistence.startJob(jobRequest);

        // get the response data
        JobStatusResponse jobStatusResponse = persistence.getJobStatus(id).get();

        Future<?> future = null;

        ReadPagePosition position = new ReadPagePosition(pageSize, 0);

        AsyncLifecycleProcessing<T> process = new AsyncLifecycleProcessing<>(
            position,
            dataReader,
            jobStatusResponse,
            lifecycle,
            jobContext,
            pageImport);

        // the core import logic will be processed in a seperate thread
        future = executor.submit(process);

       return new TrackJobStatusResponse(getJobStatus(id).get(), future);
    }

    @Transactional
    @Override
    public Optional<JobStatusResponse> getJobStatus(JobIdRequest jobId) {
        return persistence.getJobStatus(jobId);
    }


    private <T> void fireEventAndChangeState(
        JobStatusResponse response,
        List<T> data,
        JobStatus status,
        AsyncJobLifecycle<T> lifecycle,
        JobContext context) throws IOException {
        log.debug("Processing {} for job {}", status, response.getJobId());

        // fire the lifecycle event
        lifecycle.lifeCycleEventPerformed(new AsyncJobLifecycleEvent<T>(
            response,
            data,
            context,
            status
        ));

        log.debug("Processed {} for job {}", status, response.getJobId());

        // ensure we set the status
        persistence.setJobStatus(response.getJobId(), status);
    }

    /**
     * A runnable that will process the async job.
     */
    @RequiredArgsConstructor
    class AsyncLifecycleProcessing<T> implements Runnable {

        private final ReadPagePosition position;

        private final DataReader<T> dataReader;

        private final JobStatusResponse jobStatusResponse;

        private final AsyncJobLifecycle<T> lifecycle;

        private final JobContext jobContext;

        private final PageRead<T> pageRead;

        @Override
        public void run() {
            try (dataReader){
                fireEventAndChangeState(
                    jobStatusResponse, null, JobStatus.RECEIVED, lifecycle, jobContext);

                dataReader.readData(
                    position,
                    (data, jobContext) -> {

                        // if we have a failure but we want to validate all other
                        // results then keep going.
                        if (jobContext.hasFailure() && jobContext.isStopped()) {
                            persistence.setFailure(jobStatusResponse.getJobId(), jobContext.getFailureMessage());
                            throw new JobException("Job failed during validation with message: " + jobContext.getFailureMessage());
                        }

                        // validate
                        fireEventAndChangeState(
                            jobStatusResponse, data, JobStatus.VALIDATING, lifecycle, jobContext);

                        // process the state
                        fireEventAndChangeState(
                            jobStatusResponse,
                            data,
                            JobStatus.PROCESSING,
                            lifecycle,
                            jobContext
                        );

                        if (pageRead != null) {
                            // process the data
                            pageRead.readData(data, jobContext);
                        }
                        return true;
                    },
                    jobContext
                );

                // if a failure was detected then fail else complete the job
                if (jobContext.hasFailure()) {
                    fireEventAndChangeState(
                        jobStatusResponse,
                        null,
                        JobStatus.FAILED, lifecycle, jobContext
                    );
                    persistence.setFailure(jobStatusResponse.getJobId(), jobContext.getFailureMessage());
                } else {
                    fireEventAndChangeState(
                        jobStatusResponse,
                        null,
                        JobStatus.COMPLETED, lifecycle, jobContext
                    );
                }
            } catch (Throwable t) {
                log.error("Error processing job", t);

                try {
                    fireEventAndChangeState(
                        jobStatusResponse,
                        null,
                        JobStatus.FAILED, lifecycle, jobContext
                    );
                } catch (IOException e) {
                    log.error("Error calling failure lifecycle", e);
                }

                // if this is a job exception then log the error
                if (t instanceof JobException) {
                    jobContext.logError(t.getMessage());
                }

                // set the fail state
                persistence.setFailure(jobStatusResponse.getJobId(), jobContext.getFailureMessage() == null ? "Failed with unknown error"
                    : jobContext.getFailureMessage());

            }
        }
    }
}
