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
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.appregister.common.async.exception.JobException;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycle;
import uk.gov.hmcts.appregister.common.async.lifecycle.AsyncJobLifecycleEvent;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.async.model.TrackJobStatusResponse;
import uk.gov.hmcts.appregister.common.async.reader.CsvReader;
import uk.gov.hmcts.appregister.common.async.reader.PageReader;
import uk.gov.hmcts.appregister.common.async.validator.StartJobValidator;
import uk.gov.hmcts.appregister.generated.model.JobStatus;
import uk.gov.hmcts.appregister.generated.model.JobType;

@ExtendWith(MockitoExtension.class)
@Slf4j
public class AsyncJobServiceImplTest {
    @Mock private JobStatusPersistence persistence;

    @Mock private StartJobValidator startJobValidator;

    @Spy private TransactionalUnitOfWork service = new TransactionalUnitOfWork();

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

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader reader = executeWithLifecycleForFailure(jobIdRequest, lifecycle);

        // prove that we did not continue passing the data after the failure
        Assertions.assertEquals(1, reader.data.size());

        Assertions.assertTrue(lifecycle.failed);

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.VALIDATING);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        verify(persistence, times(1))
                .setFailure(
                        jobIdRequest,
                        BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR1);
    }

    @Test
    public void testAsyncAbruptProcessingStopFailure() throws Exception {
        BrokenProcessingStoppableLifecycle lifecycle = new BrokenProcessingStoppableLifecycle();
        lifecycle.stop = true;

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader reader = executeWithLifecycleForFailure(jobIdRequest, lifecycle);
        Assertions.assertTrue(lifecycle.failed);

        // prove that we did not continue passing the data after the failure
        Assertions.assertEquals(1, reader.data.size());

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.VALIDATING);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        verify(persistence, times(1))
                .setFailure(
                        jobIdRequest,
                        BrokenLifecycleWithContext.ERROR
                                + ", Job failed during PROCESSING for job "
                                + jobIdRequest.getId()
                                + ". Forced termination");
    }

    @Test
    public void testAsyncProcessingFailureNotStopping() throws Exception {
        BrokenProcessingStoppableLifecycle lifecycle = new BrokenProcessingStoppableLifecycle();
        lifecycle.stop = false;

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader reader = executeWithLifecycleForFailure(jobIdRequest, lifecycle);

        // prove that we continued passing the data even though we had an error in the processing
        Assertions.assertEquals(3, reader.data.size());

        Assertions.assertTrue(lifecycle.failed);

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(3))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.VALIDATING);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.PROCESSING);

        verify(persistence, times(1))
                .setFailure(
                        jobIdRequest,
                        BrokenProcessingStoppableLifecycle.ERROR
                                + ", "
                                + BrokenProcessingStoppableLifecycle.VALIDATE_ERROR
                                + ", "
                                + BrokenProcessingStoppableLifecycle.VALIDATE_ERROR
                                + ", "
                                + "Failed to process job: "
                                + jobIdRequest.getId().toString());
    }

    @Test
    public void testAsyncValidationFailureStop() throws Exception {

        BrokenValidationStoppableLifecycle lifecycle = new BrokenValidationStoppableLifecycle();

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader reader = executeWithLifecycleForFailure(jobIdRequest, lifecycle);

        // prove that we did not continue passing the data after the failure
        Assertions.assertEquals(1, reader.data.size());

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        // fail was executed and we logged only one error
        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        jobIdRequest,
                        BrokenLifecycleWithContext.ERROR
                                + ", Job failed during VALIDATING for job "
                                + jobIdRequest.getId()
                                + ". Forced termination");
        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.VALIDATING);
    }

    @Test
    public void testAsyncValidationFailureDoNotStop() throws Exception {
        BrokenValidationStoppableLifecycle lifecycle = new BrokenValidationStoppableLifecycle();
        lifecycle.stop = false;

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader reader = executeWithLifecycleForFailure(jobIdRequest, lifecycle);

        // prove that we did continue passing the data after the failure
        Assertions.assertEquals(3, reader.data.size());

        Assertions.assertTrue(lifecycle.failed);
        verify(persistence, times(1))
                .setFailure(
                        jobIdRequest,
                        BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR
                                + ", "
                                + BrokenLifecycleWithContext.ERROR
                                + ", Failed to process job: "
                                + jobIdRequest.getId().toString());

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        verify(persistence, times(3)).setJobStatus(jobIdRequest, JobStatus.VALIDATING);

        verify(persistence, times(0)).setJobStatus(jobIdRequest, JobStatus.PROCESSING);
    }

    @Test
    public void testAsyncValidationFailureStopWithJobException() throws Exception {
        BrokenValidationJobExceptionLifecycle lifecycle =
                new BrokenValidationJobExceptionLifecycle();

        String userId = "userId";
        UUID jobId = UUID.randomUUID();

        JobIdRequest jobIdRequest = JobIdRequest.builder().id(jobId).userName(userId).build();

        JobStatusResponse statusResponse =
                JobStatusResponse.builder()
                        .type(JobType.BULK_UPLOAD_ENTRIES)
                        .uuid(jobId)
                        .userName(userId)
                        .status(JobStatus.RECEIVED)
                        .build();

        when(persistence.getJobStatus(jobIdRequest)).thenReturn(Optional.of(statusResponse));

        DataPageReader request = executeWithLifecycleForFailure(jobIdRequest, lifecycle);

        // fail was executed and we logged only one error
        Assertions.assertTrue(lifecycle.failed);

        verify(persistence, times(1))
                .setJobStatus(
                        JobIdRequest.builder().id(jobId).userName(userId).build(),
                        JobStatus.RECEIVED);

        verify(persistence, times(1)).setJobStatus(jobIdRequest, JobStatus.FAILED);

        verify(persistence, times(1)).setFailure(jobIdRequest, BrokenLifecycleWithContext.ERROR);
    }

    private DataPageReader executeWithLifecycleForFailure(
            JobIdRequest jobIdRequest,
            AsyncJobLifecycle<PersonCsvPojo> personCsvPojoAsyncJobLifecycle)
            throws Exception {
        asyncJobServiceImpl.setPageSize(1);

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        URL resource = classLoader.getResource("person.csv");
        File fileToLoad = new File(resource.getFile());

        String userId = "userId";

        JobTypeRequest jobRequest =
                JobTypeRequest.builder().jobType(JobType.DURATION_REPORT).userName(userId).build();
        DataPageReader reader = new DataPageReader();

        try (CsvReader<PersonCsvPojo> csvReader =
                new CsvReader<>(fileToLoad, PersonCsvPojo.class)) {
            when(persistence.startJob(Mockito.notNull())).thenReturn(jobIdRequest);

            try {
                asyncJobServiceImpl
                        .startJob(jobRequest, csvReader, reader, personCsvPojoAsyncJobLifecycle)
                        .getFuture()
                        .get();
            } catch (Exception e) {
                // we expect an error to be propagated so catch it
                log.error("Failed", e);
            }
        }
        return reader;
    }

    class DataPageReader implements PageReader<PersonCsvPojo> {
        private List<PersonCsvPojo> data = new ArrayList<>();

        @Override
        public void readData(List<PersonCsvPojo> relatedData, JobContext jobContext)
                throws IOException {
            data.addAll(relatedData);
        }
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
            event.getContext().logFailure(ERROR);
            event.getContext().logFailure(ERROR1);

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
            event.getContext().logFailure(ERROR);

            // allow us to stop the validation
            if (stop) {
                event.getContext().setStoppedValidating(true);
            } else {
                event.getContext().setStoppedValidating(false);
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
        private static final String VALIDATE_ERROR = "validate error";

        private boolean failed = false;
        private boolean stop = true;
        private boolean validateFailAfterFirst = true;
        private int validated = 0;

        @Override
        public void processing(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            event.getContext().logFailure(ERROR);

            // allow us to stop the validation
            event.getContext().setStoppedValidating(stop);
        }

        @Override
        public void validating(AsyncJobLifecycleEvent<PersonCsvPojo> event) throws IOException {
            if (validated > 0 && validateFailAfterFirst) {
                event.getContext().logFailure(VALIDATE_ERROR);
            }

            validated = validated + 1;
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
