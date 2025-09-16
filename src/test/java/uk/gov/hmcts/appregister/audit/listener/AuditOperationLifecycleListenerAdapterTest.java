package uk.gov.hmcts.appregister.audit.listener;

import static org.mockito.Mockito.times;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.Mockito;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.event.AuditEvent;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;
import uk.gov.hmcts.appregister.audit.service.OperationStatus;

public class AuditOperationLifecycleListenerAdapterTest {

    @Test
    public void testStart() {
        AuditOperationLifecycleListenerAdapter my =
                Mockito.mock(
                        AuditOperationLifecycleListenerAdapter.class, Answers.CALLS_REAL_METHODS);
        my.eventPerformed(
                new AuditEvent(
                        AuditRequest.builder()
                                .requestAction(AuditEventEnum.GET_APPLICATION_CODE_AUDIT_EVENT)
                                .messageStatus(OperationStatus.STARTED)
                                .build(),
                        Optional.empty()));
        Mockito.verify(my, times(1)).started(Mockito.notNull());
    }

    @Test
    public void testComplete() {
        AuditOperationLifecycleListenerAdapter my =
                Mockito.mock(
                        AuditOperationLifecycleListenerAdapter.class, Answers.CALLS_REAL_METHODS);
        my.eventPerformed(
                new AuditEvent(
                        AuditRequest.builder()
                                .requestAction(AuditEventEnum.GET_APPLICATION_CODE_AUDIT_EVENT)
                                .messageStatus(OperationStatus.COMPLETED)
                                .build(),
                        Optional.of(AuditResponse.builder().build())));
        Mockito.verify(my, times(1)).finished(Mockito.notNull(), Mockito.notNull());
    }

    @Test
    public void testFail() {
        AuditOperationLifecycleListenerAdapter my =
                Mockito.mock(
                        AuditOperationLifecycleListenerAdapter.class, Answers.CALLS_REAL_METHODS);
        my.eventPerformed(
                new AuditEvent(
                        AuditRequest.builder()
                                .requestAction(AuditEventEnum.GET_APPLICATION_CODE_AUDIT_EVENT)
                                .messageStatus(OperationStatus.FAILED)
                                .build(),
                        Optional.of(AuditResponse.builder().build())));
        Mockito.verify(my, times(1)).finishFail(Mockito.notNull(), Mockito.notNull());
    }
}
