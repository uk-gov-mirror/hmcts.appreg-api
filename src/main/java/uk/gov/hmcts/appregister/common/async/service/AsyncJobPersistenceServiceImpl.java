package uk.gov.hmcts.appregister.common.async.service;

import jakarta.persistence.EntityManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.appregister.common.async.DeleteableFileInputStream;
import uk.gov.hmcts.appregister.common.async.mapper.JobStatusMapper;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.entity.AsyncJob;
import uk.gov.hmcts.appregister.common.entity.TableNames;
import uk.gov.hmcts.appregister.common.entity.repository.AsyncJobRepository;
import uk.gov.hmcts.appregister.common.enumeration.JobStatusType;
import uk.gov.hmcts.appregister.common.util.AppRegTempFileUtil;
import uk.gov.hmcts.appregister.generated.model.JobStatus1;
import uk.gov.hmcts.appregister.generated.model.JobType;

/**
 * A persistence layer to control the asynchronous job persistence.
 */
@Component
@RequiredArgsConstructor
public class AsyncJobPersistenceServiceImpl implements AsyncJobPersistenceService {
    /** The schema from the Spring configuration. */
    @Value("${spring.jpa.properties.hibernate.default_schema}")
    private String schema;

    /** Gets hold of the blob stream. */
    private static final String JDBC_BLOB_QUERY = "SELECT csv_output, id FROM %s WHERE id = ?";

    /** Update the blob with a stream. */
    private static final String JDBC_INSERT_BLOB_QUERY =
            "UPDATE %s SET csv_output = ? WHERE id = ?";

    /** The jdbc template to use to interact with the database. */
    private final JdbcTemplate jdbcTemplate;

    private final AsyncJobRepository asyncJobRepository;

    private final EntityManager entityManager;

    private final JobStatusMapper jobStatusMapper;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setJobStatus(JobIdRequest jobType, JobStatus1 jobStatus) {
        // do nothing
        AsyncJob asyncJob = asyncJobRepository.findByJobId(jobType.getId());

        // map the status to the enum.
        asyncJob.setJobState(jobStatusMapper.getJobStatus(jobStatus));
        asyncJobRepository.save(asyncJob);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setFailure(JobIdRequest jobType, String reasonFailed) {
        AsyncJob asyncJob = asyncJobRepository.findByJobId(jobType.getId());

        // set the failure message. Truncate to 4000 characters.
        asyncJob.setFailureMessage(truncate(reasonFailed, 4000));
        asyncJobRepository.save(asyncJob);
    }

    @Override
    public Optional<JobStatusResponse> getJobStatus(JobIdRequest job) {
        AsyncJob asyncJob = asyncJobRepository.findByJobId(job.getId());
        if (asyncJob == null) {
            return Optional.empty();
        }

        JobStatusResponse jobStatusResponse =
                JobStatusResponse.builder()
                        .status(jobStatusMapper.getJobStatus(asyncJob.getJobState()))
                        .persistence(this)
                        .uuid(asyncJob.getUuid())
                        .userName(asyncJob.getUserName())
                        .type(JobType.fromValue(asyncJob.getJobType()))
                        .errorMessage(asyncJob.getFailureMessage())
                        .build();
        return Optional.ofNullable(jobStatusResponse);
    }

    @Override
    public boolean isJobTypeFinishedForUser(JobTypeRequest id) {
        AsyncJob asyncJob =
                asyncJobRepository.findByJobTypeAndUser(
                        id.getJobType().getValue(), id.getUserName());

        if (asyncJob != null) {
            return asyncJob.getJobState() == JobStatusType.COMPLETED
                    || asyncJob.getJobState() == JobStatusType.FAILED;
        }

        return true;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public JobIdRequest startJob(JobTypeRequest request) {
        AsyncJob asyncJob = new AsyncJob();
        asyncJob.setJobType(request.getJobType().getValue());
        asyncJob.setJobState(JobStatusType.SUBMITTED);

        // save the start job
        asyncJobRepository.save(asyncJob);

        asyncJob = refreshEntity(asyncJob);

        return JobIdRequest.builder()
                .id(asyncJob.getUuid())
                .userName(request.getUserName())
                .build();
    }

    @Override
    public void writeBlob(JobIdRequest jobIdRequest, InputStream inputStream) throws IOException {
        setBlob(inputStream, jobIdRequest);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public InputStreamResource readBlob(JobIdRequest jobIdRequest) throws IOException {
        return getBlobToOutputStream(jobIdRequest);
    }

    private InputStreamResource getBlobToOutputStream(JobIdRequest jobId) throws IOException {
        File file = AppRegTempFileUtil.generateTempFile();

        jdbcTemplate.query(
                JDBC_BLOB_QUERY.formatted(schema + "." + TableNames.ASYNC_JOBS),
                ps -> ps.setObject(1, jobId.getId()),
                rs -> {
                    try (InputStream in = rs.getBinaryStream(1)) {
                        if (in != null) {
                            in.transferTo(new FileOutputStream(file));
                        }
                    } catch (IOException e) {
                        throw new SQLException(e);
                    }
                });

        if (file.length() > 0) {
            // return the spring input stream resource
            return new InputStreamResource(new DeleteableFileInputStream(file));
        } else {
            file.delete();
        }
        return null;
    }

    /**
     * sets the blob in the database.
     *
     * @param inputStream The input stream to write to the database.
     * @param jobId The job id we are setting the blob on.
     */
    public void setBlob(InputStream inputStream, JobIdRequest jobId) {
        jdbcTemplate.execute(
                JDBC_INSERT_BLOB_QUERY.formatted(schema + "." + TableNames.ASYNC_JOBS),
                (PreparedStatementCallback<Void>)
                        ps -> {
                            ps.setObject(2, jobId.getId());
                            ps.setBinaryStream(1, inputStream);
                            ps.executeUpdate();
                            return null;
                        });
    }

    /**
     * Reloads the entity so DB-generated fields (e.g. UUID via gen_random_uuid()) are available
     * immediately after save. Calls: - flush(): force the INSERT - refresh(): reselect the row with
     * DB defaults/triggers
     */
    private AsyncJob refreshEntity(AsyncJob entity) {
        entityManager.flush();
        entityManager.refresh(entity);
        return entity;
    }

    private static String truncate(String str, int maxLength) {
        if (str == null) {
            return null;
        }

        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
