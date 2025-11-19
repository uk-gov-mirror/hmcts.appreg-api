package uk.gov.hmcts.appregister.controller;

import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.http.HttpMethod;

import uk.gov.hmcts.appregister.common.security.RoleEnum;
import uk.gov.hmcts.appregister.generated.model.EntryPage;
import uk.gov.hmcts.appregister.testutils.controller.AbstractSecurityControllerTest;
import uk.gov.hmcts.appregister.testutils.controller.RestEndpointDescription;
import uk.gov.hmcts.appregister.testutils.token.TokenGenerator;
import uk.gov.hmcts.appregister.testutils.util.PagingAssertionUtil;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ApplicationEntryControllerTest extends AbstractSecurityControllerTest {
    private static final String WEB_CONTEXT = "application-list-entries";

    @Value("${spring.sql.init.schema-locations}")
    private String sqlInitSchemaLocations;

    @Value("${spring.data.web.pageable.default-page-size}")
    private Integer defaultPageSize;

    @Value("${spring.data.web.pageable.max-page-size}")
    private Integer maxPageSize;

    // The total app codes inserted by flyway scripts. See V6__InitialTestData.sql
    private static final int TOTAL_APP_ENTRY_COUNT = 2;

    @Test
    public void testGetApplicationEntriesSearch() throws Exception {
        // create the token
        TokenGenerator tokenGenerator =
            getATokenWithValidCredentials().roles(List.of(RoleEnum.ADMIN)).build();

        // test the functionality
        Response responseSpec =
            restAssuredClient.executeGetRequest(
                getLocalUrl(WEB_CONTEXT), tokenGenerator.fetchTokenForRole());

        // assert the response
        responseSpec.then().statusCode(200);

        EntryPage page = responseSpec.as(EntryPage.class);
        PagingAssertionUtil.assertPageDetails(page, defaultPageSize, 0, 1, TOTAL_APP_ENTRY_COUNT);
        assertEquals(defaultPageSize, page.getContent().size());
    }

    @Override
    protected Stream<RestEndpointDescription> getDescriptions() throws Exception {
        return Stream.of(
            RestEndpointDescription.builder()
                .url(getLocalUrl(WEB_CONTEXT))
                .method(HttpMethod.GET)
                .successRole(RoleEnum.USER)
                .successRole(RoleEnum.ADMIN)
                .build());
    }

    record ApplicationEntryFilter(Optional<LocalDate> date, Optional<String> code,
                            Optional<String> location, Optional<String> cjaCode,
                            Optional<String> applicantOrg, Optional<String> applicantSurname,
                            Optional<String> applicantCode, Optional<String> respondentOrg,
                            Optional<String> respondentSurname, Optional<String> respondentPostcode,
                            Optional<String> accountReference, Optional<String> status
        ) implements UnaryOperator<RequestSpecification> {

        @Override
        public io.restassured.specification.RequestSpecification apply(
            io.restassured.specification.RequestSpecification rs) {
            if (date.isPresent()) {
                rs = rs.queryParam("date", date.get());
            }
            if (code.isPresent()) {
                rs = rs.queryParam("code", code.get());
            }

            if (cjaCode.isPresent()) {
                rs = rs.queryParam("cjaCode", location.get());
            }

            if (applicantSurname.isPresent()) {
                rs = rs.queryParam("applicantOrg", applicantSurname.get());
            }

            if (applicantSurname.isPresent()) {
                rs = rs.queryParam("applicantSurname", applicantSurname.get());
            }
            if (applicantCode.isPresent()) {
                rs = rs.queryParam("applicantCode", applicantCode.get());
            }

            if (respondentOrg.isPresent()) {
                rs = rs.queryParam("respondentOrg", respondentOrg.get());
            }

            if (respondentSurname.isPresent()) {
                rs = rs.queryParam("respondentSurname", respondentSurname.get());
            }

            if (respondentPostcode.isPresent()) {
                rs = rs.queryParam("respondentPostcode", respondentPostcode.get());
            }

            if (accountReference.isPresent()) {
                rs = rs.queryParam("accountReference", accountReference.get());
            }

            if (status.isPresent()) {
                rs = rs.queryParam("status", status.get());
            }
            return rs;
        }
    }
}
