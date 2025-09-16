package uk.gov.hmcts.appregister.audit.listener;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;
import uk.gov.hmcts.appregister.audit.model.BaseAuditPayload;

/** Logs the request and response of audit events using SLF4J. */
@Slf4j
public class AuditOperationSlf4jLogger extends AuditOperationLifecycleListenerAdapter {

    /** The message uuid. */
    private static final String MESSAGE_UUID = "p_messageuuid";

    /** The message status. */
    private static final String STATUS = "p_messagestatus";

    /** The message content. */
    private static final String CONTENT = "p_messagecontent";

    /** The action. */
    private static final String ACTION = "p_requestaction";

    @Override
    protected void started(AuditRequest request) {
        log.info("Start audit {}", getLog(request));
    }

    @Override
    protected void finished(AuditRequest request, AuditResponse response) {
        log.info("Completion audit {}", getLog(response));
    }

    @Override
    protected void finishFail(AuditRequest request, AuditResponse response) {
        log.info("Completion fail audit {}", getLog(response));
    }

    public static String getLog(BaseAuditPayload request) {
        return "\n"
                + "-"
                + ACTION
                + "="
                + request.getRequestAction().getEventName()
                + "\n"
                + "-"
                + MESSAGE_UUID
                + "="
                + request.getMessageUuid()
                + "\n"
                + "-"
                + STATUS
                + "="
                + request.getMessageStatus().getStatus()
                + "\n"
                + "-"
                + CONTENT
                + "="
                + request.getMessageContent();
    }
}
