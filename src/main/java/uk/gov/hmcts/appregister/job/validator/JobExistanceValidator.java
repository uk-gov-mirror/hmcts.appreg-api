package uk.gov.hmcts.appregister.job.validator;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.async.AsyncJobService;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.common.validator.Validator;

import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;

@Component
@RequiredArgsConstructor
public class JobExistanceValidator implements Validator<UUID, JobSuccess> {
    private final AsyncJobService asyncJobService;

    private final UserProvider userProvider;

    @Override
    public void validate(UUID request) {
        validate(request, (uuid, jobSuccess) -> null);
    }

    @Override
    public <R> R validate(UUID uuid, BiFunction<UUID, JobSuccess, R> validateFunction) {
        Optional<JobStatusResponse> jobStatusResponse = asyncJobService.getJobStatus(JobIdRequest.builder()
                                                                                         .id(uuid)
                                                                                         .userName(
                                                                                             userProvider.getUserId()).build());

        if (jobStatusResponse.isEmpty()) {
            throw new AppRegistryException(JobError.JOB_DOES_NOT_EXIST_OR_NOT_FOR_USER,
                                           "Job type is already running");
        }

        JobSuccess jobSuccess = new JobSuccess();
        jobSuccess.setJobStatusResponse(jobSuccess.getJobStatusResponse());
        return validateFunction.apply(uuid, jobSuccess);
    }
}
