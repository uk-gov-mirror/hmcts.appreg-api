package uk.gov.hmcts.appregister.audit.listener;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.appregister.audit.event.AuditEvent;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;
import uk.gov.hmcts.appregister.audit.service.OperationStatus;

/**
 * An abstract class that breaks down the {@link AuditOperationLifecycleListener} into seperate well
 * defined methods.
 */
@Slf4j
public abstract class AuditOperationLifecycleListenerAdapter
        implements AuditOperationLifecycleListener {

    @Override
    public void eventPerformed(AuditEvent event) {
        if (event.getRequest().getMessageStatus() == OperationStatus.STARTED) {
            started(event.getRequest());
        } else if (event.getRequest().getMessageStatus() == OperationStatus.COMPLETED) {
            if (event.getResponse().isPresent()) {
                finished(event.getRequest(), event.getResponse().get());
            } else {
                log.warn("No response present when status is COMPLETED");
                finished(event.getRequest(), null);
            }
        } else if (event.getRequest().getMessageStatus() == OperationStatus.FAILED) {
            if (event.getResponse().isPresent()) {
                finishFail(event.getRequest(), event.getResponse().get());
            } else {
                log.warn("No response present when status is DID_NOT_COMPLETE");
                finishFail(event.getRequest(), null);
            }
        }
    }

    /**
     * audit before operation has started.
     *
     * @param request The request for the audit
     */
    protected abstract void started(AuditRequest request);

    /**
     * audit when operation has finished.
     *
     * @param request The request for the audit
     * @param response The response for the audit
     */
    protected abstract void finished(AuditRequest request, AuditResponse response);

    /**
     * audit when operation has failed.
     *
     * @param request The request for the audit
     * @param response The response for the audit
     */
    protected abstract void finishFail(AuditRequest request, AuditResponse response);
}
