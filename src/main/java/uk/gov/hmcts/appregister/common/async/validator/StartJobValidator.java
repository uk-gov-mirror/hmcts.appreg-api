package uk.gov.hmcts.appregister.common.async.validator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.async.JobStatusPersistence;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.validator.Validator;

import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class StartJobValidator implements Validator<JobTypeRequest, Void> {
    private final JobStatusPersistence persistence;

    @Override
    public void validate(JobTypeRequest request) {
        if (persistence.isJobTypeNotFinishedForUser(request)) {
            throw new AppRegistryException(JobError.JOB_TYPE_IS_ALREADY_RUNNING, "Job type is already running");
        }
    }

    @Override
    public <R> R validate(JobTypeRequest validatable, BiFunction<JobTypeRequest, Void, R> validateSuccess) {
        validate(validatable);
        return validateSuccess.apply(validatable, null);
    }
}
