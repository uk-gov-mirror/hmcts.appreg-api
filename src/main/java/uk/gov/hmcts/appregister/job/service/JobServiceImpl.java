package uk.gov.hmcts.appregister.job.service;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.generated.model.JobAcknowledgement;
import uk.gov.hmcts.appregister.job.mapper.JobMapper;
import uk.gov.hmcts.appregister.job.validator.JobExistanceValidator;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JobServiceImpl implements JobService {
    private final JobMapper jobMapper;

    private final JobExistanceValidator statusJobValidator;

    @Override
    public JobAcknowledgement getJobAckById(UUID jobId) {
        return jobMapper.toDto(getJobStatusById(jobId));
    }

    @Override
    public JobStatusResponse getJobStatusById(UUID jobId) {
        return statusJobValidator.validate(jobId, (uuid, success)
            -> success.getJobStatusResponse());
    }
}
