package uk.gov.hmcts.appregister.audit.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;

/** A listener that sets a default request action for audit requests. */
@Slf4j
@RequiredArgsConstructor
public class ApplyRequestActionAuditListener extends AuditOperationLifecycleListenerAdapter {

    private final AuditEventEnum requestAction;

    @Override
    protected void started(AuditRequest request) {
        request.setRequestAction(requestAction);
        log.debug("Set request action to: {}", requestAction);
    }

    @Override
    protected void finished(AuditRequest request, AuditResponse response) {
        log.debug("Not processing audit finished");
    }

    @Override
    protected void finishFail(AuditRequest request, AuditResponse response) {
        log.debug("Not processing finish fail");
    }
}
