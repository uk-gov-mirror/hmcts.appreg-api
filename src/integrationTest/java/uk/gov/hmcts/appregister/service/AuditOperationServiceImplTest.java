package uk.gov.hmcts.appregister.service;

import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import uk.gov.hmcts.appregister.applicationlist.audit.AppListAuditOperation;
import uk.gov.hmcts.appregister.audit.listener.DataAuditLogger;
import uk.gov.hmcts.appregister.audit.model.AuditableResult;
import uk.gov.hmcts.appregister.audit.service.AuditOperationService;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea;
import uk.gov.hmcts.appregister.common.security.UserProvider;
import uk.gov.hmcts.appregister.data.CriminalJusticeTestData;
import uk.gov.hmcts.appregister.entity.TestEntity2;
import uk.gov.hmcts.appregister.entity.TestEntityAuditable;
import uk.gov.hmcts.appregister.testutils.BaseIntegration;
import uk.gov.hmcts.appregister.testutils.util.DifferenceLogAsserter;

public class AuditOperationServiceImplTest extends BaseIntegration {

    @MockitoBean private UserProvider provider;

    @Autowired private DataAuditLogger dataAuditLogger;

    @Autowired private AuditOperationService auditOperationService;

    @Test
    public void testGetApplicationEntryAudit() {
        when(provider.getUserId()).thenReturn("user");
        when(provider.getEmail()).thenReturn("email");
        when(provider.getRoles()).thenReturn(new String[] {"role"});

        TestEntityAuditable test = new TestEntityAuditable();

        CriminalJusticeArea criminalJusticeTestData = new CriminalJusticeTestData().someComplete();
        criminalJusticeTestData.setId(999L);
        test.setCriminalJusticeArea(criminalJusticeTestData);
        test.setId(123L);

        TestEntity2 listEntity2 = new TestEntity2();
        listEntity2.setName("e8");
        listEntity2.setId(3L);

        test.getEntry().add(listEntity2);
        test.getEntryStrings().addAll(List.of("teststring", "teststring2"));

        Object content =
                auditOperationService.processAudit(
                        AppListAuditOperation.CREATE_APP_LIST,
                        (event) ->
                                Optional.of(
                                        AuditableResult.builder()
                                                .newEntity(test)
                                                .resultingValue("response")
                                                .build()),
                        dataAuditLogger);

        differenceLogAsserter.assertNoErrors();

        // make sure the processing was successful and lists and complex objects were diffed
        Assert.assertEquals("response", content);
        differenceLogAsserter.assertDiffCount(2);
        differenceLogAsserter.assertDifferenceOrDataAuditChange(
                DifferenceLogAsserter.getDataAuditAssertion(
                        "test_entity",
                        "adr_id",
                        "null",
                        "123",
                        "CREATE",
                        "Create Application List"));
        differenceLogAsserter.assertDifferenceOrDataAuditChange(
                DifferenceLogAsserter.getDataAuditAssertion(
                        "criminal_justice_area",
                        "cja_id",
                        "null",
                        "999",
                        "CREATE",
                        "Create Application List"));
    }
}
