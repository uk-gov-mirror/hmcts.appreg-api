package uk.gov.hmcts.appregister.audit;

import java.util.Optional;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.gov.hmcts.appregister.audit.event.AuditEvent;
import uk.gov.hmcts.appregister.audit.listener.AuditOperationSlf4jLogger;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;
import uk.gov.hmcts.appregister.audit.service.OperationStatus;

public class AuditOperationSlf4jLoggerTest {

    private LogCaptor logCaptor;

    @BeforeEach
    public void before() {
        logCaptor = LogCaptor.forClass(AuditOperationSlf4jLogger.class);
        logCaptor.clearLogs();
    }

    @Test
    public void testFailOperationLog() throws Exception {
        AuditRequest auditRequest =
                AuditRequest.builder()
                        .messageUuid("ID")
                        .requestAction(AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT)
                        .messageStatus(OperationStatus.FAILED)
                        .build();

        AuditResponse auditResponse =
                AuditResponse.builder()
                        .messageUuid("ID")
                        .requestAction(AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT)
                        .messageStatus(OperationStatus.FAILED)
                        .build();

        AuditEvent event = new AuditEvent(auditRequest, Optional.of(auditResponse));
        new AuditOperationSlf4jLogger().eventPerformed(event);

        Assertions.assertEquals(
                "Completion fail audit\s"
                        + getLogString(
                                AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT,
                                "ID",
                                OperationStatus.FAILED,
                                "NULL"),
                logCaptor.getInfoLogs().getFirst());
    }

    @Test
    public void testBeforeOperationLog() throws Exception {
        AuditRequest auditRequest =
                AuditRequest.builder()
                        .messageUuid("ID")
                        .requestAction(AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT)
                        .messageStatus(OperationStatus.STARTED)
                        .build();

        AuditEvent event = new AuditEvent(auditRequest, Optional.empty());
        new AuditOperationSlf4jLogger().eventPerformed(event);

        Assertions.assertEquals(
                "Start audit\s"
                        + getLogString(
                                AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT,
                                "ID",
                                OperationStatus.STARTED,
                                "NULL"),
                logCaptor.getInfoLogs().getFirst());
    }

    @Test
    public void testCompletedOperationLog() throws Exception {
        AuditRequest auditRequest =
                AuditRequest.builder()
                        .messageUuid("ID")
                        .requestAction(AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT)
                        .messageStatus(OperationStatus.COMPLETED)
                        .build();

        AuditResponse auditResponse =
                AuditResponse.builder()
                        .messageUuid("ID")
                        .requestAction(AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT)
                        .messageStatus(OperationStatus.COMPLETED)
                        .build();

        AuditEvent event = new AuditEvent(auditRequest, Optional.of(auditResponse));
        new AuditOperationSlf4jLogger().eventPerformed(event);

        Assertions.assertEquals(
                "Completion audit\s"
                        + getLogString(
                                AuditEventEnum.GET_APPLICATION_CODES_AUDIT_EVENT,
                                "ID",
                                OperationStatus.COMPLETED,
                                "NULL"),
                logCaptor.getInfoLogs().getFirst());
    }

    private static String getLogString(
            AuditEventEnum action, String messageuuid, OperationStatus status, String content) {
        AuditResponse response =
                AuditResponse.builder()
                        .requestAction(action)
                        .messageUuid(messageuuid)
                        .messageContent(content)
                        .messageStatus(status)
                        .build();
        return AuditOperationSlf4jLogger.getLog(response);
    }
}
