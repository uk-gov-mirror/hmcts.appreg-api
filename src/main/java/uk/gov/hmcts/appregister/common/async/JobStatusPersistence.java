package uk.gov.hmcts.appregister.common.async;

import org.springframework.core.io.InputStreamResource;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.generated.model.JobStatus;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

/**
 * This is a job status persistence interface that allows us to control the job persistence.
 */
public interface JobStatusPersistence {

    /**
     * sets the job with the state event passed to it
     * @param jobType The job type
     * @param jobStatus The job status
     */
    void setJobStatus(JobIdRequest jobType, JobStatus jobStatus);

    /**
     * Sets the status of the job as failed with the reason for failure.
     * @param jobType The job type
     * @param reasonFailed The reason for failure
     */
    void setFailure(JobIdRequest jobType, String reasonFailed);

    /**
     * gets the response
     * @param id The id of the job
     * @return The job status response if one exists else an empty
     */
    Optional<JobStatusResponse> getJobStatus(JobIdRequest id);

    /**
     * does the job type exist and is not it finished
     * @param id The job type
     * @return true if the job type exists and is not yet finished, false otherwise
     */
    boolean isJobTypeNotFinishedForUser(JobTypeRequest id);

    /**
     * starts a job.
     * @param request The job details to start.
     * @return The job id request to probe the details of the job.
     */
    JobIdRequest startJob(JobTypeRequest request);

    /**
     * writes a blob to the job id request
     * @param jobIdRequest The job id request.
     * @param inputStream The input stream to write.
     * @throws IOException Any problems
     */
    void writeBlob(JobIdRequest jobIdRequest, InputStream inputStream) throws IOException;

    /**
     * reads a blob from the job id request.
     * @param jobIdRequest The job id request.
     * @throws IOException Any problems
     */
    InputStreamResource readBlob(JobIdRequest jobIdRequest) throws IOException;
}
