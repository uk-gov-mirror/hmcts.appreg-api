package uk.gov.hmcts.appregister.common.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;

import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.entity.AsyncJob;

import java.util.UUID;

@Component
public interface AsyncJobRepository extends JpaRepository<AsyncJob, Long> {

    /**
     * finds an asynchronous job by job number and the user id.
     * @param jobType The job type
     * @param userId The user id
     */
    @Query("""
            SELECT aj
            FROM AsyncJob aj
            WHERE aj.jobType = :jobType
            AND aj.userName = :userId
        """)
    AsyncJob findByJobTypeAndUser(String jobType, String userId);

    /**
     * finds an asynchronous job by job number and the user id.
     * @param jobId The job id to return the job.
     */
    @Query("""
            SELECT aj
            FROM AsyncJob aj
            WHERE aj.uuid = :jobId
        """)
    AsyncJob findByJobId(UUID jobId);
}
