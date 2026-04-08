package uk.gov.hmcts.appregister.common.async;

import lombok.RequiredArgsConstructor;

import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.stereotype.Component;

import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.exception.JobException;
import uk.gov.hmcts.appregister.common.async.model.JobIdRequest;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.generated.model.JobStatus;
import uk.gov.hmcts.appregister.generated.model.JobType;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

/**
 * TODO: This class needs implementing against the database. Waiting for ticket to be implemented.
 * https://tools.hmcts.net/jira/browse/ARCPOC-1139
 */
@Component
@RequiredArgsConstructor
public class JobStatusPersistenceImpl implements JobStatusPersistence {
    private final String JDBC_BLOB_QUERY = "content FROM files WHERE id = ?";
    private final String JDBC_INSERT_BLOB_QUERY = "content FROM files WHERE id = ?";

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void setJobStatus(JobIdRequest jobType, JobStatus jobStatus) {

    }

    public void setFailure(JobIdRequest jobType, String reasonFailed) {

    }

    @Override
    public Optional<JobStatusResponse> getJobStatus(JobIdRequest id) {
        return Optional.empty();
    }

    @Override
    public boolean isJobTypeNotFinishedForUser(JobTypeRequest id) {
        return false;
    }

    @Override
    public JobIdRequest startJob(JobTypeRequest request) {
        return null;
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
        InputStreamResource inputStream = null;
        File file = File.createTempFile(UUID.randomUUID().toString(), ".tmp");

            jdbcTemplate.query(
                JDBC_BLOB_QUERY,
                ps -> ps.setString(1, jobId.getId().toString()),
                rs -> {
                    if (rs.next()) {
                        try (InputStream in = rs.getBinaryStream("content")) {
                            if (in != null) {
                                in.transferTo(new FileOutputStream(file));
                            }
                        } catch (IOException e) {
                            throw new SQLException(e);
                        }
                    }
                }
            );

        // return the spring input stream resource
        return new InputStreamResource(new CloseableFileInputStream(file));
    }

    /**
     * sets the blob in the database.
     * @param inputStream The input stream to write to the database.
     * @param jobId
     */
    public void setBlob(InputStream inputStream, JobIdRequest jobId) {
        jdbcTemplate.execute(
            "JDBC_INSERT_BLOB_QUERY",
            (PreparedStatementCallback<Void>) ps -> {
                ps.setString(1, jobId.getId().toString());
                ps.setBinaryStream(2, inputStream);
                ps.executeUpdate();
                return null;
            }
        );
    }

    /**
     * A closeable input stream that is backed by a file and will delete when closed
     */
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
}
