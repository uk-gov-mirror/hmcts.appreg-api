package uk.gov.hmcts.appregister.job.service;

import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.generated.model.JobAcknowledgement;

import java.util.UUID;

public interface JobService {
    JobAcknowledgement getJobAckById(UUID jobId);

    JobStatusResponse getJobStatusById(UUID jobId);

}
