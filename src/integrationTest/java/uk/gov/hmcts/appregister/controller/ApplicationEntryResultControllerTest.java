package uk.gov.hmcts.appregister.controller;

import io.restassured.response.Response;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import uk.gov.hmcts.appregister.applicationentry.exception.ApplicationListEntryError;
import uk.gov.hmcts.appregister.applicationentryresult.exception.ApplicationListEntryResultError;
import uk.gov.hmcts.appregister.applicationlist.exception.ApplicationListError;
import uk.gov.hmcts.appregister.common.entity.AppListEntryResolution;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry;
import uk.gov.hmcts.appregister.common.entity.ResolutionCode;
import uk.gov.hmcts.appregister.common.enumeration.Status;
import uk.gov.hmcts.appregister.common.security.RoleEnum;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.data.AppListEntryResolutionTestData;
import uk.gov.hmcts.appregister.data.AppListEntryTestData;
import uk.gov.hmcts.appregister.data.AppListTestData;
import uk.gov.hmcts.appregister.data.ResolutionCodeTestData;
import uk.gov.hmcts.appregister.generated.model.MoveEntriesDto;
import uk.gov.hmcts.appregister.testutils.controller.AbstractSecurityControllerTest;
import uk.gov.hmcts.appregister.testutils.controller.RestEndpointDescription;
import uk.gov.hmcts.appregister.testutils.token.TokenAndJwksKey;

import java.net.MalformedURLException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import com.nimbusds.jose.JOSEException;

import static org.mockito.Mockito.when;
import static uk.gov.hmcts.appregister.common.enumeration.Status.CLOSED;
import static uk.gov.hmcts.appregister.common.enumeration.Status.OPEN;
import static uk.gov.hmcts.appregister.data.AppListEntryResolutionTestData.WORDING_1;

public class ApplicationEntryResultControllerTest extends AbstractSecurityControllerTest {
    @MockitoBean private UserProvider provider;
    private static final String WEB_CONTEXT = "application-lists";
    private static final String VND_JSON_V1 = "application/vnd.hmcts.appreg.v1+json";

    @BeforeEach
    public void before() {
        when(provider.getUserId()).thenReturn("user");
        when(provider.getEmail()).thenReturn("email");
        when(provider.getRoles()).thenReturn(new String[] {"role"});
    }

    @Test
    @DisplayName("Delete Application List Entry Result: 204 when valid IDs")
    void givenValidIds_whenDelete_then204() throws Exception {
        ApplicationList list =
            new AppListTestData()
                .someMinimal()
                .status(OPEN)
                .build();
        persistance.save(list);

        ApplicationListEntry entry =
            new AppListEntryTestData()
                .someMinimal()
                .applicationList(list)
                .build();

        ResolutionCode resolutionCode = new ResolutionCodeTestData().someComplete();
        AppListEntryResolution entryResult =
            new AppListEntryResolutionTestData()
                .someMinimal()
                .resolutionWording(WORDING_1)
                .applicationList(entry)
                .resolutionCode(resolutionCode)
                .build();
        AppListEntryResolution savedEntryResult = persistance.save(entryResult);

        UUID listId = list.getUuid();
        UUID entryId = entry.getUuid();
        UUID resultId = savedEntryResult.getUuid();
        var token = getToken();

        // fire test
        Response resp = deleteApplicationListEntryResultResponse(listId, entryId, resultId, token);

        // assert success
        resp.then().statusCode(HttpStatus.NO_CONTENT.value());
    }

    @Test
    @DisplayName("Delete Application List Entry Result: 404 when list unknown")
    void givenUnknownList_whenDelete_then404() throws Exception {
        UUID listId = UUID.randomUUID();
        UUID entryId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        var token = getToken();

        // fire test
        Response resp = deleteApplicationListEntryResultResponse(listId, entryId, resultId, token);

        // assert success
        resp.then().statusCode(HttpStatus.NOT_FOUND.value());

        ProblemDetail problemDetail = resp.as(ProblemDetail.class);
        Assertions.assertEquals(
            ApplicationListError.ENTRY_RESULT_LIST_NOT_FOUND.getCode().getAppCode(),
            problemDetail.getType().toString());
    }

    @Test
    @DisplayName("Delete Application List Entry Result: 400 when list closed")
    void givenClosedList_whenDelete_then400() throws Exception {
        ApplicationList list =
            new AppListTestData()
                .someMinimal()
                .status(CLOSED)
                .build();
        persistance.save(list);

        UUID listId = list.getUuid();
        UUID entryId = UUID.randomUUID();
        UUID resultId = UUID.randomUUID();
        var token = getToken();

        // fire test
        Response resp = deleteApplicationListEntryResultResponse(listId, entryId, resultId, token);

        // assert success
        resp.then().statusCode(HttpStatus.BAD_REQUEST.value());

        ProblemDetail problemDetail = resp.as(ProblemDetail.class);
        Assertions.assertEquals(
            ApplicationListError.INVALID_ENTRY_RESULT_LIST_STATUS.getCode().getAppCode(),
            problemDetail.getType().toString());
    }

    @Test
    @DisplayName("Delete Application List Entry Result: 400 when entry not in list")
    void givenEntryNotInList_whenDelete_then400() throws Exception {
        ApplicationList list =
            new AppListTestData()
                .someMinimal()
                .status(OPEN)
                .build();
        persistance.save(list);

        ApplicationListEntry entry =
            new AppListEntryTestData()
                .someMinimal()
                .applicationList(list)
                .build();
        persistance.save(entry);

        UUID listId = list.getUuid();
        UUID entryId = entry.getUuid();
        UUID resultId = UUID.randomUUID();
        var token = getToken();

        // fire test
        Response resp = deleteApplicationListEntryResultResponse(listId, entryId, resultId, token);

        // assert success
        resp.then().statusCode(HttpStatus.BAD_REQUEST.value());

        ProblemDetail problemDetail = resp.as(ProblemDetail.class);
        Assertions.assertEquals(
            ApplicationListEntryError.LIST_ENTRY_NOT_FOUND.getCode().getAppCode(),
            problemDetail.getType().toString());
    }

    @Test
    @DisplayName("Delete Application List Entry Result: 400 when entry result not related to entry")
    void givenEntryResultNotRelatedToEntry_whenDelete_then400() throws Exception {
        ApplicationList list =
            new AppListTestData()
                .someMinimal()
                .status(OPEN)
                .build();
        persistance.save(list);

        ApplicationListEntry entry =
            new AppListEntryTestData()
                .someMinimal()
                .applicationList(list)
                .build();
        persistance.save(entry);

        ResolutionCode resolutionCode = new ResolutionCodeTestData().someComplete();
        AppListEntryResolution entryResult =
            new AppListEntryResolutionTestData()
                .someMinimal()
                .resolutionWording(WORDING_1)
                .resolutionCode(resolutionCode)
                .build();
        AppListEntryResolution savedEntryResult = persistance.save(entryResult);

        UUID listId = list.getUuid();
        UUID entryId = entry.getUuid();
        UUID resultId = savedEntryResult.getUuid();
        var token = getToken();

        // fire test
        Response resp = deleteApplicationListEntryResultResponse(listId, entryId, resultId, token);

        // assert success
        resp.then().statusCode(HttpStatus.BAD_REQUEST.value());

        ProblemDetail problemDetail = resp.as(ProblemDetail.class);
        Assertions.assertEquals(
            ApplicationListEntryResultError.LIST_ENTRY_RESULT_NOT_FOUND.getCode().getAppCode(),
            problemDetail.getType().toString());
    }

    @Override
    protected Stream<RestEndpointDescription> getDescriptions() throws Exception {
        Set<UUID> entryIds = new HashSet<>();
        entryIds.add(UUID.randomUUID());

        var validPayload = new MoveEntriesDto().targetListId(UUID.randomUUID()).entryIds(entryIds);

        return Stream.of(
                RestEndpointDescription.builder()
                        .url(getLocalUrl(WEB_CONTEXT + "/" + UUID.randomUUID() + "/entries/move"))
                        .method(HttpMethod.POST)
                        .payload(validPayload)
                        .successRole(RoleEnum.USER)
                        .successRole(RoleEnum.ADMIN)
                        .build());
    }

    private TokenAndJwksKey getToken() throws JOSEException {
        return getATokenWithValidCredentials()
                .roles(List.of(RoleEnum.USER))
                .build()
                .fetchTokenForRole();
    }

    private Response deleteApplicationListEntryResultResponse(
            UUID listId, UUID entryId, UUID resultId, TokenAndJwksKey token)
            throws MalformedURLException {
        return restAssuredClient.executeDeleteRequest(
                getLocalUrl(WEB_CONTEXT + "/" + listId + "/entries/" + entryId + "/results/" + resultId), token);
    }
}
