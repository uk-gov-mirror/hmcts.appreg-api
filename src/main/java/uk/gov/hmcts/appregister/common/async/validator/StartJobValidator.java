package uk.gov.hmcts.appregister.common.async.validator;

import java.util.function.BiFunction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.common.async.AsyncJobPersistenceService;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.validator.Validator;

/**
 * A validator that checks if a job type is already running for a user and fails accordingly. This
 * check prevents us from running two of the same job types for the same user at the same time.
 */
@Component
@RequiredArgsConstructor
public class StartJobValidator implements Validator<JobTypeRequest, Void> {
    private final AsyncJobPersistenceService persistence;

    @Override
    public void validate(JobTypeRequest request) {
        if (!persistence.isJobTypeFinishedForUser(request)) {
            throw new AppRegistryException(
                    JobError.JOB_TYPE_IS_ALREADY_RUNNING, "Job type is already running");
        }
    }

    @Override
    public <R> R validate(
            JobTypeRequest validatable, BiFunction<JobTypeRequest, Void, R> validateSuccess) {
        validate(validatable);
        return validateSuccess.apply(validatable, null);
    }
}
