package uk.gov.hmcts.appregister.report.controller;

import java.io.IOException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobStatusResponse;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.security.RoleNames;
import uk.gov.hmcts.appregister.generated.api.ReportsApi;
import uk.gov.hmcts.appregister.job.service.JobService;

@PreAuthorize(RoleNames.USER_ROLE_OR_ADMIN_ROLE_RESTRICTION)
@Controller
@RequiredArgsConstructor
@Slf4j
public class ReportController implements ReportsApi {
    private final JobService jobService;

    @Override
    public ResponseEntity<Resource> downloadReport(UUID jobId) {
        JobStatusResponse jobStatusResponse = jobService.getJobStatusById(jobId);
        try {
            InputStreamResource resource = new InputStreamResource(jobStatusResponse.read());
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"report.csv\"")
                    .header(HttpHeaders.CACHE_CONTROL, "no-cache")
                    .varyBy(HttpHeaders.ACCEPT)
                    .contentType(MediaType.parseMediaType("text/csv"))
                    .body(resource);
        } catch (IOException ioException) {
            log.error("Error reading download stream for job id: {}", jobId, ioException);
            throw new AppRegistryException(
                    JobError.JOB_DOES_NOT_HAVE_DATA_TO_GET_AN_DOWNLOAD_STREAM,
                    "Download stream not available");
        }
    }
}
