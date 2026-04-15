package uk.gov.hmcts.appregister.common.async.validator;

import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.appregister.common.async.AsyncJobPersistenceService;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.generated.model.JobType;

@ExtendWith(MockitoExtension.class)
public class StartJobValidatorTest {
    @Mock private AsyncJobPersistenceService persistence;

    @InjectMocks private StartJobValidator startJobValidator;

    @Test
    void testValidationFail() {
        JobTypeRequest jobIdRequest =
                JobTypeRequest.builder().jobType(JobType.BULK_UPLOAD_ENTRIES).build();
        when(persistence.isJobTypeFinishedForUser(jobIdRequest)).thenReturn(false);

        AppRegistryException ex =
                Assertions.assertThrows(
                        AppRegistryException.class, () -> startJobValidator.validate(jobIdRequest));
        Assertions.assertEquals(
                JobError.JOB_TYPE_IS_ALREADY_RUNNING.getCode(), ex.getCode().getCode());
    }

    @Test
    void testValidationSuccess() {
        JobTypeRequest jobIdRequest =
                JobTypeRequest.builder().jobType(JobType.BULK_UPLOAD_ENTRIES).build();
        when(persistence.isJobTypeNotFinishedForUser(jobIdRequest)).thenReturn(false);

        Assertions.assertTrue(
                startJobValidator.validate(jobIdRequest, (req, v) -> Boolean.TRUE).booleanValue());
    }
}
