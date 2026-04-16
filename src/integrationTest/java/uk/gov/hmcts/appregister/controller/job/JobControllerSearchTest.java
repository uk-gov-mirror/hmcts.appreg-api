package uk.gov.hmcts.appregister.controller.job;

import static org.mockito.Mockito.when;

import io.restassured.response.Response;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import uk.gov.hmcts.appregister.common.async.exception.JobError;
import uk.gov.hmcts.appregister.common.async.model.JobTypeRequest;
import uk.gov.hmcts.appregister.common.async.model.TrackJobStatusResponse;
import uk.gov.hmcts.appregister.common.async.reader.CsvReader;
import uk.gov.hmcts.appregister.common.async.service.AsyncJobService;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationCodeRepository;
import uk.gov.hmcts.appregister.common.security.RoleEnum;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.generated.model.JobAcknowledgement;
import uk.gov.hmcts.appregister.generated.model.JobStatus1;
import uk.gov.hmcts.appregister.generated.model.JobType;
import uk.gov.hmcts.appregister.testutils.AwaitilityUtil;
import uk.gov.hmcts.appregister.testutils.BaseIntegration;
import uk.gov.hmcts.appregister.testutils.csv.ApplicationCodeCsvPojo;
import uk.gov.hmcts.appregister.testutils.csv.JobProcessCsvReadLifecycle;
import uk.gov.hmcts.appregister.testutils.token.TokenGenerator;
import uk.gov.hmcts.appregister.testutils.util.ProblemAssertUtil;

public class JobControllerSearchTest extends BaseIntegration {
    public static final String WEB_CONTEXT = "jobs";

    @Autowired private UserProvider userProvider;

    @Autowired private AsyncJobService asyncJobService;

    @Autowired private ApplicationCodeRepository applicationCodeRepository;

    @BeforeEach
    public void setUp() throws Exception {
        Authentication authentication = Mockito.mock(Authentication.class);
        when(authentication.getPrincipal())
                .thenReturn(TokenGenerator.builder().build().getJwtFromToken());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    @Test
    public void givenJob_whenJobStatusRequested_thenASuccessIsReturned() throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
                getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        JobTypeRequest request =
                JobTypeRequest.builder()
                        .jobType(JobType.FEES_REPORT)
                        .userName(userProvider.getUserId())
                        .build();

        CsvReader<ApplicationCodeCsvPojo> csvReaderForAppCode =
                new CsvReader<>(
                        getClass().getResourceAsStream("/appcodes.csv"),
                        ApplicationCodeCsvPojo.class);
        JobProcessCsvReadLifecycle jobProcessCsvReadLifecycle =
                new JobProcessCsvReadLifecycle(applicationCodeRepository);

        // fire off the job, let not wait
        TrackJobStatusResponse response =
                asyncJobService.startJob(request, csvReaderForAppCode, jobProcessCsvReadLifecycle);

        // wait for 30 seconds polling each second for the status to be true
        AwaitilityUtil.waitForMaxWithOneSecondPoll(
                () -> {
                    Response responseSpec =
                            restAssuredClient.executeGetRequest(
                                    getLocalUrl(
                                            WEB_CONTEXT
                                                    + "/"
                                                    + response.getJobId().getId().toString()),
                                    tokenGenerator.fetchTokenForRole());

                    // if not a 200 then fail
                    if (responseSpec.statusCode() != 200) {
                        return false;
                    } else {
                        JobAcknowledgement jobStatusResponse =
                                responseSpec.as(JobAcknowledgement.class);

                        // assert the response
                        responseSpec.then().statusCode(200);

                        // if not a completed or failed then fail
                        if (jobStatusResponse.getStatus() != JobStatus1.COMPLETED
                                && jobStatusResponse.getStatus() != JobStatus1.FAILED) {
                            return false;
                        }

                        Assertions.assertEquals(
                                JobStatus1.COMPLETED, jobStatusResponse.getStatus());
                        return true;
                    }
                },
                Duration.ofSeconds(30));
    }

    @Test
    public void givenJob_whenJobThatDoesNotExistIsRequested_thenAFailureIsReturned()
            throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
                getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        Response responseSpec =
                restAssuredClient.executeGetRequest(
                        getLocalUrl(WEB_CONTEXT + "/" + UUID.randomUUID()),
                        tokenGenerator.fetchTokenForRole());

        responseSpec.then().statusCode(400);
        ProblemAssertUtil.assertEquals(
                JobError.JOB_DOES_NOT_EXIST_OR_NOT_FOR_USER.getCode(), responseSpec);
    }
}
