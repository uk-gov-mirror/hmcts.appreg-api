package uk.gov.hmcts.appregister.common.async;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.entity.AsyncJob;
import uk.gov.hmcts.appregister.common.entity.TableNames;
import uk.gov.hmcts.appregister.common.entity.repository.AsyncJobRepository;
import uk.gov.hmcts.appregister.common.enumeration.JobStatusType;
import uk.gov.hmcts.appregister.generated.model.JobStatus1;
import uk.gov.hmcts.appregister.generated.model.JobType;

/**
 * A persistence layer to control the asynchronous job persistence.
 */
@Component
@RequiredArgsConstructor
public class AsyncJobPersistenceServiceImpl implements AsyncJobPersistenceService {
    /** Gets hold of the blob stream. */
    private static final String JDBC_BLOB_QUERY = "SELECT csv_output FROM %s WHERE id = ?".formatted(TableNames.ASYNC_JOBS);

    /** Update the blob with a stream. */
    private static final String JDBC_INSERT_BLOB_QUERY = "UPDATE %s SET csv_output = ? WHERE id = ?";

    /** The jdbc template to use to interact with the database. */
    private final JdbcTemplate jdbcTemplate;

    private final AsyncJobRepository asyncJobRepository;

    private final EntityManager entityManager;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void setJobStatus(JobIdRequest jobType, JobStatus1 jobStatus) {
        // do nothing
        AsyncJob asyncJob = asyncJobRepository.findByJobId(jobType.getId());

        // map the status to the enum.
        asyncJob.setJobState(getJobStatus(jobStatus));
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

        JobStatusResponse jobStatusResponse = JobStatusResponse
            .builder().status(getJobStatus(asyncJob.getJobState())).persistence(this)
            .uuid(asyncJob.getUuid())
            .userName(asyncJob.getUserName())
            .type(JobType.fromValue(asyncJob.getJobType())).build();
        return Optional.ofNullable(jobStatusResponse);
    }

    @Override
    public boolean isJobTypeFinishedForUser(JobTypeRequest id) {
        AsyncJob asyncJob = asyncJobRepository.findByJobTypeAndUser(
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

        return JobIdRequest.builder().id(asyncJob.getUuid())
            .userName(request.getUserName()).build();
    }

    @Override
    public void writeBlob(JobIdRequest jobIdRequest, InputStream inputStream) throws IOException {
        setBlob(inputStream, jobIdRequest);
    }

    @Override
    public InputStreamResource readBlob(JobIdRequest jobIdRequest) throws IOException {
        return getBlobToOutputStream(jobIdRequest);
    }

    private InputStreamResource getBlobToOutputStream(JobIdRequest jobId) throws IOException {
        File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");

        jdbcTemplate.query(
                JDBC_BLOB_QUERY.formatted(TableNames.ASYNC_JOBS),
                ps -> ps.setString(1, jobId.getId().toString()),
                rs -> {
                    if (rs.next()) {
                        try (InputStream in = rs.getBinaryStream("csv_output")) {
                            if (in != null) {
                                in.transferTo(new FileOutputStream(file));
                            }
                        } catch (IOException e) {
                            throw new SQLException(e);
                        }
                    }
                });

        // return the spring input stream resource
        return new InputStreamResource(new CloseableFileInputStream(file));
    }

    /**
     * sets the blob in the database.
     *
     * @param inputStream The input stream to write to the database.
     * @param jobId The job id we are setting the blob on.
     */
    public void setBlob(InputStream inputStream, JobIdRequest jobId) {
        jdbcTemplate.execute(
                JDBC_INSERT_BLOB_QUERY.formatted(TableNames.ASYNC_JOBS),
                (PreparedStatementCallback<Void>)
                        ps -> {
                            ps.setString(2, jobId.getId().toString());
                            ps.setBinaryStream(1, inputStream);
                            ps.executeUpdate();
                            return null;
                        });
    }

    private JobStatus1 getJobStatus(JobStatusType status) {
        if (status == JobStatusType.PENDING) {
            return JobStatus1.VALIDATING;
        } else if (status == JobStatusType.SUBMITTED) {
            return JobStatus1.RECEIVED;
        } else if (status == JobStatusType.COMPLETED) {
            return JobStatus1.COMPLETED;
        } else if (status == JobStatusType.FAILED) {
            return JobStatus1.FAILED;
        } else if (status == JobStatusType.RUNNING) {
            return JobStatus1.PROCESSING;
        }

        return null;
    }

    private JobStatusType getJobStatus(JobStatus1 status) {
        if (status == JobStatus1.RECEIVED) {
            return JobStatusType.SUBMITTED;
        } else if (status == JobStatus1.VALIDATING) {
            return JobStatusType.PENDING;
        } else if (status == JobStatus1.COMPLETED) {
            return JobStatusType.COMPLETED;
        } else if (status == JobStatus1.FAILED) {
            return JobStatusType.FAILED;
        } else if (status == JobStatus1.PROCESSING) {
            return JobStatusType.RUNNING;
        }

        return null;
    }

    /** A closeable input stream that is backed by a file and will delete when closed. */
    class CloseableFileInputStream extends FileInputStream {
        private File file;

        CloseableFileInputStream(File file) throws IOException {
            super(file);
        }

        @Override
        public void close() throws IOException {
            super.close();

            // delete the file
            file.delete();
        }
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

    public static String truncate(String str, int maxLength) {
        if (str == null) return null;
        return str.length() <= maxLength ? str : str.substring(0, maxLength);
    }
}
