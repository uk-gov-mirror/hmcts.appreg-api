package uk.gov.hmcts.appregister.common.async;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.appregister.common.async.exception.JobException;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycleEvent;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.async.model.TrackJobStatusResponse;
import uk.gov.hmcts.appregister.common.async.reader.CsvReader;
import uk.gov.hmcts.appregister.common.async.validator.StartJobValidator;
import uk.gov.hmcts.appregister.generated.model.JobStatus;
import uk.gov.hmcts.appregister.generated.model.JobType;

@ExtendWith(MockitoExtension.class)
public class AsyncJobServiceImplTest {
    @Mock private JobStatusPersistence persistence;

    @Mock private StartJobValidator startJobValidator;

    @InjectMocks private AsyncJobServiceImpl asyncJobServiceImpl;

    @Test
    public void testAsyncStart() throws Exception {
        // set the page size
        asyncJobServiceImpl.setPageSize(1);

        // setup the user provider to get hold of the user name
        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        // setup the callback
        AsyncJobLifecycle<PersonCsvPojo> lifecycle = Mockito.mock(AsyncJobLifecycle.class);

        // setup the reader for the csv file
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("person.csv");
        File fileToLoad = new File(resource.getFile());

        List<PersonCsvPojo> output = new ArrayList<>();
        CsvReader<PersonCsvPojo> csvReader = new CsvReader<>(fileToLoad, PersonCsvPojo.class);
        try (csvReader) {
            JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

            // mock the persistence start job
            when(persistence.startJob(Mockito.notNull())).thenReturn(jobIdRequest);

            JobStatusResponse statusResponse =
                    JobStatusResponse.builder()
                            .type(JobType.BULK_UPLOAD_ENTRIES)
                            .uuid(jobId)
                            .userName(userId)
                            .status(JobStatus.RECEIVED)
                            .build();

            when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

            JobTypeRequest jobRequest =
                    JobTypeRequest.builder()
                            .jobType(JobType.DURATION_REPORT)
                            .userName(userId)
                            .build();

            // start the job and wait for the async response
            TrackJobStatusResponse trackJobStatusResponse =
                    asyncJobServiceImpl.startJob(
                            jobRequest,
                            csvReader,
                            (data, context) -> output.addAll(data),
                            lifecycle);

            // assert that we synchronously get the response
            Assertions.assertEquals(jobId, trackJobStatusResponse.getJobId().getId());

            // wait for the underlying process
            trackJobStatusResponse.getFuture().get();

            // capture each event in a list so we can asserr
            ArgumentCaptor<AsyncJobLifecycleEvent<PersonCsvPojo>> lifecycleEventArgumentCaptor =
                    ArgumentCaptor.forClass(AsyncJobLifecycleEvent.class);
            verify(lifecycle, times(8))
                    .lifeCycleEventPerformed(lifecycleEventArgumentCaptor.capture());

            // verify the events were fired approriately
            verify(persistence, times(1))
                    .setJobStatus(
                            JobIdRequest.builder().id(jobId).userName(userId).build(),
                            JobStatus.RECEIVED);
            verify(persistence, times(3))
                    .setJobStatus(
                            JobIdRequest.builder().id(jobId).userName(userId).build(),
                            JobStatus.VALIDATING);
            verify(persistence, times(3))
                    .setJobStatus(
                            JobIdRequest.builder().id(jobId).userName(userId).build(),
                            JobStatus.PROCESSING);
            verify(persistence, times(1))
                    .setJobStatus(
                            JobIdRequest.builder().id(jobId).userName(userId).build(),
                            JobStatus.COMPLETED);

            // assert the number of the events fired
            Assertions.assertEquals(
                    JobStatus.RECEIVED,
                    lifecycleEventArgumentCaptor.getAllValues().get(0).getJobStatus());
            Assertions.assertEquals(
                    JobStatus.VALIDATING,
                    lifecycleEventArgumentCaptor.getAllValues().get(1).getJobStatus());
            Assertions.assertEquals(
                    "Alice",
                    lifecycleEventArgumentCaptor.getAllValues().get(1).getData().get(0).getName());

            Assertions.assertEquals(
                    JobStatus.PROCESSING,
                    lifecycleEventArgumentCaptor.getAllValues().get(2).getJobStatus());

            Assertions.assertEquals(
                    "Alice",
                    lifecycleEventArgumentCaptor.getAllValues().get(2).getData().get(0).getName());

            Assertions.assertEquals(
                    JobStatus.VALIDATING,
                    lifecycleEventArgumentCaptor.getAllValues().get(3).getJobStatus());
            Assertions.assertEquals(
                    "Bob",
                    lifecycleEventArgumentCaptor.getAllValues().get(3).getData().get(0).getName());

            Assertions.assertEquals(
                    JobStatus.PROCESSING,
                    lifecycleEventArgumentCaptor.getAllValues().get(4).getJobStatus());

            Assertions.assertEquals(
                    "Bob",
                    lifecycleEventArgumentCaptor.getAllValues().get(4).getData().get(0).getName());
            Assertions.assertEquals(
                    JobStatus.VALIDATING,
                    lifecycleEventArgumentCaptor.getAllValues().get(5).getJobStatus());

            Assertions.assertEquals(
                    JobStatus.PROCESSING,
                    lifecycleEventArgumentCaptor.getAllValues().get(6).getJobStatus());

            Assertions.assertEquals(
                    JobStatus.COMPLETED,
                    lifecycleEventArgumentCaptor.getAllValues().get(7).getJobStatus());
        }

        Assertions.assertThrows(IOException.class, () -> csvReader.getInputStream());
    }

    @Test
    public void testAsyncAbruptProcessingFailure() throws Exception {
        BrokenLifecycleWithContext lifecycle = new BrokenLifecycleWithContext();
        JobIdRequest request = executeWithLifecycle(lifecycle);
        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        request,
                        BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR1);
    }

    @Test
    public void testAsyncAbruptProcessingStopFailure() throws Exception {
        BrokenProcessingStoppableLifecycle lifecycle = new BrokenProcessingStoppableLifecycle();
        lifecycle.stop = true;
        JobIdRequest request = executeWithLifecycle(lifecycle);
        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        request,
                        BrokenLifecycleWithContext.ERROR
                                + ", Job failed during PROCESSING for job "
                                + request.getId()
                                + ". Forced termination");
    }

    @Test
    public void testAsyncValidationFailureStop() throws Exception {

        BrokenValidationStoppableLifecycle lifecycle = new BrokenValidationStoppableLifecycle();
        JobIdRequest request = executeWithLifecycle(lifecycle);

        // fail was executed and we logged only one error
        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        request,
                        BrokenLifecycleWithContext.ERROR
                                + ", Job failed during VALIDATING for job "
                                + request.getId()
                                + ". Forced termination");
    }

    @Test
    public void testAsyncValidationFailureDoNotStop() throws Exception {
        BrokenValidationStoppableLifecycle lifecycle = new BrokenValidationStoppableLifecycle();
        lifecycle.stop = false;
        JobIdRequest request = executeWithLifecycle(lifecycle);

        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        request,
                        BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR
                                + ", Failed to process job: "
                                + request.getId().toString());
    }

    @Test
    public void testAsyncValidationFailureStopWithJobException() throws Exception {
        BrokenValidationJobExceptionLifecycle lifecycle =
                new BrokenValidationJobExceptionLifecycle();

        JobIdRequest request = executeWithLifecycle(lifecycle);

        // fail was executed and we logged only one error
        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1)).setFailure(request, BrokenLifecycleWithContext.ERROR);
    }

    private JobIdRequest executeWithLifecycle(
            AsyncJobLifecycle<PersonCsvPojo> personCsvPojoAsyncJobLifecycle) throws Exception {
        asyncJobServiceImpl.setPageSize(1);
        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("person.csv");
        File fileToLoad = new File(resource.getFile());

        List<PersonCsvPojo> output = new ArrayList<>();
        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        try (CsvReader<PersonCsvPojo> csvReader =
                new CsvReader<>(fileToLoad, PersonCsvPojo.class)) {

            when(persistence.startJob(Mockito.notNull())).thenReturn(jobIdRequest);

            JobStatusResponse statusResponse =
                    JobStatusResponse.builder()
                            .type(JobType.BULK_UPLOAD_ENTRIES)
                            .uuid(jobId)
                            .userName(userId)
                            .status(JobStatus.RECEIVED)
                            .build();

            when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

            JobTypeRequest jobRequest =
                    JobTypeRequest.builder()
                            .jobType(JobType.DURATION_REPORT)
                            .userName(userId)
                            .build();

            asyncJobServiceImpl
                    .startJob(
                            jobRequest,
                            csvReader,
                            (data, context) -> output.addAll(data),
                            personCsvPojoAsyncJobLifecycle)
                    .getFuture()
                    .get();
        }
        return jobIdRequest;
    }

    @Slf4j
    static class BrokenLifecycleWithContext implements AsyncJobLifecycle<PersonCsvPojo> {
        private static final String ERROR = "random error";
        private static final String ERROR1 = "random error1";
        private boolean failed = false;

        @Override
        public void processing(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            log.debug("Failing processing");

            // set error
            event.getContext().logError(ERROR);
            event.getContext().logError(ERROR1);

            // throw exception so we do not continue with the processing
            throw new IOException("Broken");
        }

        @Override
        public void failed(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            failed = true;
        }
    }

    @Slf4j
    static class BrokenValidationStoppableLifecycle implements AsyncJobLifecycle<PersonCsvPojo> {
        private static final String ERROR = "random error";
        private boolean failed = false;
        private boolean stop = true;

        @Override
        public void validating(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            event.getContext().logError(ERROR);

            // allow us to stop the validation
            if (stop) {
                event.getContext().setStopped(true);
            } else {
                event.getContext().setStopped(false);
            }
        }

        @Override
        public void failed(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            failed = true;
        }
    }

    @Slf4j
    static class BrokenProcessingStoppableLifecycle implements AsyncJobLifecycle<PersonCsvPojo> {
        private static final String ERROR = "random error";
        private boolean failed = false;
        private boolean stop = true;

        @Override
        public void processing(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            event.getContext().logError(ERROR);

            // allow us to stop the validation
            if (stop) {
                event.getContext().setStopped(true);
            } else {
                event.getContext().setStopped(false);
            }
        }

        @Override
        public void failed(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            failed = true;
        }
    }

    @Slf4j
    static class BrokenValidationJobExceptionLifecycle implements AsyncJobLifecycle<PersonCsvPojo> {
        private static final String ERROR = "random error";
        private boolean failed = false;

        @Override
        public void validating(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            throw new JobException(ERROR);
        }

        @Override
        public void failed(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            failed = true;
        }
    }
}
