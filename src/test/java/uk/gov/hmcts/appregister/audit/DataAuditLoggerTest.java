package uk.gov.hmcts.appregister.audit;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.appregister.applicationcode.audit.AppCodeAuditOperation;
import uk.gov.hmcts.appregister.applicationlist.audit.AppListAuditOperation;
import uk.gov.hmcts.appregister.audit.event.CompleteEvent;
import uk.gov.hmcts.appregister.audit.event.FailEvent;
import uk.gov.hmcts.appregister.audit.event.StartEvent;
import uk.gov.hmcts.appregister.audit.listener.DataAuditLogger;
import uk.gov.hmcts.appregister.audit.listener.diff.AuditDifferentiable;
import uk.gov.hmcts.appregister.audit.listener.diff.AuditDifferentiator;
import uk.gov.hmcts.appregister.audit.listener.diff.Difference;
import uk.gov.hmcts.appregister.audit.listener.diff.ReflectiveAuditDifferentiator;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.DataAudit;
import uk.gov.hmcts.appregister.common.entity.TableNames;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.common.entity.repository.DataAuditRepository;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;
import uk.gov.hmcts.appregister.data.ApplicationCodeTestData;

@ExtendWith(MockitoExtension.class)
public class DataAuditLoggerTest {
    @Mock private AuditDifferentiator auditDifferentiator;

    @Mock private DataAuditRepository dataAuditRepository;

    @InjectMocks private DataAuditLogger dataAuditLogger;

    private ArgumentCaptor<DataAudit> auditCaptor = ArgumentCaptor.forClass(DataAudit.class);

    private ArgumentCaptor<DataAudit> auditCaptor2 = ArgumentCaptor.forClass(DataAudit.class);

    @Test
    public void testStartOperationTest() {
        StartEvent startEvent =
                new StartEvent(AppCodeAuditOperation.GET_APPLICATION_CODES_AUDIT_EVENT, "ID", null);

        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(startEvent);

        verify(dataAuditRepository, never()).save(any());
    }

    @Test
    public void testFailOperationTest() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        StartEvent startEvent =
                new StartEvent(
                        AppCodeAuditOperation.GET_APPLICATION_CODES_AUDIT_EVENT,
                        "ID",
                        testData.someComplete());
        FailEvent auditRequest = new FailEvent(startEvent);

        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        verify(dataAuditRepository, never()).save(any());
    }

    @Test
    public void testSuccessOperationForGetWithoutDataAuditSaveTest() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        ApplicationCode oldCode = testData.someComplete();
        ApplicationCode newCode = testData.someComplete();

        StartEvent startEvent =
                new StartEvent(
                        AppCodeAuditOperation.GET_APPLICATION_CODES_AUDIT_EVENT, "ID", oldCode);
        CompleteEvent auditRequest = new CompleteEvent(startEvent, null, newCode);
        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        // repo was not called as this is a get operation
        verify(dataAuditRepository, never()).save(any());
    }

    @Test
    public void testSuccessOperationForCreateTest() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        ApplicationCode newCode = testData.someComplete();
        Long id = 123L;
        newCode.setId(id);

        StartEvent startEvent = new StartEvent(AppListAuditOperation.CREATE_APP_LIST, "ID", null);
        CompleteEvent auditRequest = new CompleteEvent(startEvent, null, newCode);

        String tableName = TableNames.APPLICATION_CODES;
        String field = "field";
        String newValue = "value";

        String field1 = "field1";
        String newValue2 = "value2";

        when(auditDifferentiator.diff(CrudEnum.CREATE, newCode))
                .thenReturn(
                        List.of(
                                new Difference(tableName, field, "null", newValue),
                                new Difference(tableName, field1, "null", newValue2)));
        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        // repo was not called as this is a get operation
        verify(dataAuditRepository, times(2)).save(auditCaptor.capture());

        DataAudit dataAudit = auditCaptor.getAllValues().get(0);
        Assertions.assertEquals(id, dataAudit.getRelatedKey());
        Assertions.assertEquals(field, dataAudit.getColumnName());
        Assertions.assertEquals(newValue, dataAudit.getNewValue());
        Assertions.assertEquals("null", dataAudit.getOldValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit.getTableName());
        Assertions.assertEquals(CrudEnum.CREATE, dataAudit.getUpdateType());

        DataAudit dataAudit1 = auditCaptor.getAllValues().get(1);
        Assertions.assertEquals(id, dataAudit1.getRelatedKey());
        Assertions.assertEquals(field1, dataAudit1.getColumnName());
        Assertions.assertEquals(newValue2, dataAudit1.getNewValue());
        Assertions.assertEquals("null", dataAudit1.getOldValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit1.getTableName());
        Assertions.assertEquals(CrudEnum.CREATE, dataAudit1.getUpdateType());
    }

    @Test
    public void testSuccessOperationForUpdateTest() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        ApplicationCode newCode = testData.someComplete();
        ApplicationCode oldCode = testData.someComplete();

        Long id = 123L;
        newCode.setId(id);

        Long id1 = 123L;
        oldCode.setId(id1);

        StartEvent startEvent = new StartEvent(TestAuditOperation.UPDATE, "ID", oldCode);
        CompleteEvent auditRequest = new CompleteEvent(startEvent, null, newCode);

        String tableName = TableNames.APPLICATION_CODES;
        String field = "field";
        String newValue = "value";
        String oldValue = "value old";

        String field1 = "field1";
        String newValue2 = "value2";
        String oldValue2 = "value old2 ";

        when(auditDifferentiator.diff(CrudEnum.UPDATE, oldCode, newCode))
                .thenReturn(
                        List.of(
                                new Difference(tableName, field, oldValue, newValue),
                                new Difference(tableName, field1, oldValue2, newValue2)));
        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        // repo was not called as this is a get operation
        verify(dataAuditRepository, times(2)).save(auditCaptor.capture());
        verify(auditDifferentiator, never()).diff(any(), any());

        DataAudit dataAudit = auditCaptor.getAllValues().get(0);
        Assertions.assertEquals(id, dataAudit.getRelatedKey());
        Assertions.assertEquals(field, dataAudit.getColumnName());
        Assertions.assertEquals(newValue, dataAudit.getNewValue());
        Assertions.assertEquals(oldValue, dataAudit.getOldValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit.getTableName());
        Assertions.assertEquals(CrudEnum.UPDATE, dataAudit.getUpdateType());

        DataAudit dataAudit1 = auditCaptor.getAllValues().get(1);
        Assertions.assertEquals(id, dataAudit1.getRelatedKey());
        Assertions.assertEquals(field1, dataAudit1.getColumnName());
        Assertions.assertEquals(newValue2, dataAudit1.getNewValue());
        Assertions.assertEquals(oldValue2, dataAudit1.getOldValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit1.getTableName());
        Assertions.assertEquals(CrudEnum.UPDATE, dataAudit1.getUpdateType());
    }

    @Test
    public void testSuccessOperationForSoftDeleteTest() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        ApplicationCode newCode = testData.someComplete();
        ApplicationCode oldCode = testData.someComplete();

        Long id = 123L;
        newCode.setId(id);

        Long id1 = 123L;
        oldCode.setId(id1);

        StartEvent startEvent = new StartEvent(TestAuditOperation.DELETE, "ID", oldCode);
        CompleteEvent auditRequest = new CompleteEvent(startEvent, null, null);

        String tableName = TableNames.APPLICATION_CODES;
        String field = "field";
        String newValue = "value";
        String oldValue = "value old";

        String field1 = "field1";
        String newValue2 = "value2";
        String oldValue2 = "value old2 ";

        when(auditDifferentiator.diff(CrudEnum.DELETE, oldCode, null))
                .thenReturn(
                        List.of(
                                new Difference(tableName, field, oldValue, newValue),
                                new Difference(
                                        tableName,
                                        field1,
                                        oldValue2,
                                        ReflectiveAuditDifferentiator.EMPTY_VALUE)));
        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        // repo was not called as this is a get operation
        verify(dataAuditRepository, times(2)).save(auditCaptor.capture());
        verify(auditDifferentiator, times(1)).diff(any(), any(), any());

        DataAudit dataAudit1 = auditCaptor.getAllValues().get(0);
        Assertions.assertEquals(-1, dataAudit1.getRelatedKey());
        Assertions.assertEquals(field, dataAudit1.getColumnName());
        Assertions.assertEquals(newValue, dataAudit1.getNewValue());
        Assertions.assertEquals(oldValue, dataAudit1.getOldValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit1.getTableName());
        Assertions.assertEquals(CrudEnum.DELETE, dataAudit1.getUpdateType());
    }

    @Test
    public void testSuccessOperationForSoftDeleteTestWithDifferentiatorEntity() {
        ApplicationCodeTestData testData = new ApplicationCodeTestData();
        ApplicationCode newCode = testData.someComplete();
        ApplicationCode oldCode = testData.someComplete();

        Long id = 123L;
        newCode.setId(id);

        Long id1 = 123L;
        oldCode.setId(id1);

        String tableName = TableNames.APPLICATION_CODES;
        String field = "field";
        String newValue = "value";
        String oldValue = "value old";

        String field1 = "field1";
        String oldValue2 = "value old2 ";

        Keyable mockOld =
                Mockito.mock(
                        Keyable.class, withSettings().extraInterfaces(AuditDifferentiable.class));
        AuditDifferentiable differentiableOld = (AuditDifferentiable) mockOld;

        Keyable mockNew =
                Mockito.mock(
                        Keyable.class, withSettings().extraInterfaces(AuditDifferentiable.class));
        AuditDifferentiable differentiableNew = (AuditDifferentiable) mockNew;
        when(mockNew.getId()).thenReturn(id1);

        StartEvent startEvent = new StartEvent(TestAuditOperation.DELETE, "ID", differentiableOld);
        CompleteEvent auditRequest = new CompleteEvent(startEvent, null, differentiableNew);

        when(differentiableNew.diff(TestAuditOperation.DELETE.getType(), mockOld))
                .thenReturn(
                        List.of(
                                new Difference(tableName, field, oldValue, newValue),
                                new Difference(
                                        tableName,
                                        field1,
                                        oldValue2,
                                        ReflectiveAuditDifferentiator.EMPTY_VALUE)));
        new DataAuditLogger(auditDifferentiator, dataAuditRepository).eventPerformed(auditRequest);

        // repo was not called as this is a get operation
        verify(dataAuditRepository, times(2)).save(auditCaptor.capture());
        verify(auditDifferentiator, never()).diff(any(), any());

        DataAudit dataAudit1 = auditCaptor.getAllValues().get(0);
        Assertions.assertEquals(id, dataAudit1.getRelatedKey());
        Assertions.assertEquals(field, dataAudit1.getColumnName());
        Assertions.assertEquals(newValue, dataAudit1.getNewValue());
        Assertions.assertEquals(oldValue, dataAudit1.getOldValue());
        Assertions.assertEquals(newValue, dataAudit1.getNewValue());
        Assertions.assertEquals(TableNames.APPLICATION_CODES, dataAudit1.getTableName());
        Assertions.assertEquals(CrudEnum.DELETE, dataAudit1.getUpdateType());
    }
}
