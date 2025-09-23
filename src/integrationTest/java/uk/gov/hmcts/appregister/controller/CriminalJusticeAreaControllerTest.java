package uk.gov.hmcts.appregister.controller;

import io.restassured.response.Response;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import io.restassured.specification.RequestSpecification;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import uk.gov.hmcts.appregister.applicationcode.dto.ApplicationCodeDto;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.event.OperationStatus;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea;
import uk.gov.hmcts.appregister.criminaljusticearea.exception.CriminalJusticeAreaError;
import uk.gov.hmcts.appregister.generated.model.CriminalJusticeAreaDto;
import uk.gov.hmcts.appregister.generated.model.CriminalJusticeAreaPage;
import uk.gov.hmcts.appregister.testutils.client.RoleEnum;
import uk.gov.hmcts.appregister.testutils.controller.AbstractSecurityControllerTest;
import uk.gov.hmcts.appregister.testutils.controller.RestEndpointDescription;
import uk.gov.hmcts.appregister.testutils.token.TokenGenerator;
import uk.gov.hmcts.appregister.testutils.util.AuditAssertUtil;
import uk.gov.hmcts.appregister.testutils.util.PagingAssertUtil;
import uk.gov.hmcts.appregister.testutils.util.PagingAssertionUtil;
import uk.gov.hmcts.appregister.testutils.util.ProblemAssertUtil;

public class CriminalJusticeAreaControllerTest extends AbstractSecurityControllerTest {
    private static final String WEB_CONTEXT = "criminal-justice-areas";

    // expectations based on the flyway test data
    private static final String EXPECTED_CODE = "CJ";
    private static final String EXPECTED_DESCRIPTION = "CJA_DESCRIPTION";
    private static final String EXPECTED_GET_CRIMINAL_JUSTICE_AREA_AUDIT_ACTION =
            AuditEventEnum.GET_CRIMINAL_JUSTICE_AUDIT_EVENT.getEventName();
    private static final String EXPECTED_GET_CRIMINAL_JUSTICE_AREAS_AUDIT_ACTION =
            AuditEventEnum.GET_CRIMINAL_JUSTICE_AUDITS_EVENT.getEventName();

    @Value("${spring.data.web.pageable.default-page-size}")
    private Integer defaultPageSize;

    @Value("${spring.data.web.pageable.max-page-size}")
    private Integer maxPageSize;

    /** The total criminal justice area inserted by flyway scripts. See V6__InitialTestData.sql */
    private static final int TOTAL_CJA_COUNT = 3;

    @Test
    public void givenValidRequest_whenGetCriminalJusticeAreaWithValidCode_thenReturn200()
            throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
                getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        // test the functionality
        Response responseSpec =
                restAssuredClient.executeGetRequest(
                        getLocalUrl(WEB_CONTEXT + "/" + EXPECTED_CODE),
                        tokenGenerator.fetchTokenForRole());

        // assert the response
        responseSpec.then().statusCode(200);

        CriminalJusticeAreaDto responseContent = responseSpec.as(CriminalJusticeAreaDto.class);

        // assert the core payload
        CriminalJusticeAreaDto criminalJusticeAreaDto =
                generateDefaultCriminalJusticeDtoAssertionPayload(
                        EXPECTED_CODE, EXPECTED_DESCRIPTION);

        // our generated dtos contain an implemented logical equivalence
        Assertions.assertEquals(criminalJusticeAreaDto, responseContent);

        // assert the audit log message
        AuditAssertUtil.assertStart(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREA_AUDIT_ACTION, logCaptor.getInfoLogs().get(0));
        AuditAssertUtil.assertCompleted(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREA_AUDIT_ACTION, logCaptor.getInfoLogs().get(1));
    }

    @Test
    public void givenValidRequest_whenGetCriminalJusticeAreaWithInvalidCode_thenReturn400()
            throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
                getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        String invalidCode = "invalidCode";

        // test the functionality
        Response responseSpec =
                restAssuredClient.executeGetRequest(
                        getLocalUrl(WEB_CONTEXT + "/" + invalidCode),
                        tokenGenerator.fetchTokenForRole());

        // assert the response
        responseSpec.then().statusCode(404);
        ProblemAssertUtil.assertEquals(
                CriminalJusticeAreaError.CODE_NOT_FOUND.getCode(), responseSpec);

        // assert the audit log message
        AuditAssertUtil.assertStart(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREA_AUDIT_ACTION, logCaptor.getInfoLogs().get(0));
        AuditAssertUtil.assertFailCompleted(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREA_AUDIT_ACTION, logCaptor.getInfoLogs().get(1));
    }

    @Test
    public void
    givenValidRequest_whenGetCriminalJusticeArea_thenReturn200()
            throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
                getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        // test the functionaity
        Response responseSpec =
                restAssuredClient.executeGetRequest(
                        getLocalUrl(WEB_CONTEXT), tokenGenerator.fetchTokenForRole());

        // assert the response
        responseSpec.then().statusCode(200);

        CriminalJusticeAreaPage responseContent = responseSpec.as(CriminalJusticeAreaPage.class);
        PagingAssertionUtil.assertPageDetails(
                responseContent, defaultPageSize, 0, 1, TOTAL_CJA_COUNT);

        // assert the expected default order and content
        CriminalJusticeAreaDto expectedCriminalJusticeArea =
                generateDefaultCriminalJusticeDtoAssertionPayload(
                        EXPECTED_CODE,
                        EXPECTED_DESCRIPTION);

        CriminalJusticeAreaDto expectedCriminalJusticeArea2 =
                generateDefaultCriminalJusticeDtoAssertionPayload(
                        EXPECTED_CODE,
                        EXPECTED_DESCRIPTION);

        CriminalJusticeAreaPage actualResponse = responseSpec.as(CriminalJusticeAreaPage.class);
        Assertions.assertEquals(actualResponse.getContent().get(0), expectedCriminalJusticeArea);
        Assertions.assertEquals(actualResponse.getContent().get(2), expectedCriminalJusticeArea2);

        // assert the log
        AuditAssertUtil.assertStart(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREAS_AUDIT_ACTION, logCaptor.getInfoLogs().get(0));
        AuditAssertUtil.assertFailCompleted(
                EXPECTED_GET_CRIMINAL_JUSTICE_AREAS_AUDIT_ACTION, logCaptor.getInfoLogs().get(1));
    }

    private CriminalJusticeAreaDto generateDefaultCriminalJusticeDtoAssertionPayload(
            String code, String description) {
        return new CriminalJusticeAreaDto(code, description);
    }

    @Override
    protected Stream<RestEndpointDescription> getDescriptions() throws Exception {
        return Stream.of(
                RestEndpointDescription.builder()
                        .url(
                                getLocalUrlWithDate(
                                        WEB_CONTEXT + "/" + EXPECTED_CODE, OffsetDateTime.now()))
                        .method(HttpMethod.GET)
                        .successRole(RoleEnum.USER)
                        .successRole(RoleEnum.ADMIN)
                        .build());
    }

    /**
     * A request specification that knows what query filters can be applied to get criminal justice areas.
     */
    @RequiredArgsConstructor
    static class CriminalJusticeAreaFilter
            implements Function<RequestSpecification, RequestSpecification> {
        private final Optional<String> code;
        private final Optional<String> description;

        @Override
        public RequestSpecification apply(RequestSpecification rs) {
            if (code.isPresent()) {
                rs = rs.queryParam("code", code.get());
            }

            if (description.isPresent()) {
                rs = rs.queryParam("description", description.get());
            }

            return rs;
        }
    }
}
