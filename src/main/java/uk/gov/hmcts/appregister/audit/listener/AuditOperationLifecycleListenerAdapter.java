package uk.gov.hmcts.appregister.audit.listener;

import lombok.extern.slf4j.Slf4j;
import uk.gov.hmcts.appregister.audit.event.*;
import uk.gov.hmcts.appregister.audit.event.OperationStatus;

/**
 * An abstract class that breaks down the {@link AuditOperationLifecycleListener} into seperate well
 * defined methods.
 */
@Slf4j
public abstract class AuditOperationLifecycleListenerAdapter
        implements AuditOperationLifecycleListener {

    @Override
    public void eventPerformed(BaseAuditEvent event) {
        if (event.getMessageStatus() == OperationStatus.STARTED) {
            started((StartEvent) event);
        } else if (event.getMessageStatus() == OperationStatus.COMPLETED) {
            finished((CompleteEvent) event);
        } else if (event.getMessageStatus() == OperationStatus.FAILED) {
            finishFail((FailEvent) event);
        }
    }

    /**
     * audit before operation has started.
     *
     * @param event The request for the audit
     */
    protected abstract void started(StartEvent event);

    /**
     * audit when operation has finished.
     *
     * @param event The request for the audit
     */
    protected abstract void finished(CompleteEvent event);

    /**
     * audit when operation has failed.
     *
     * @param event The request for the audit
     */
    protected abstract void finishFail(FailEvent event);
}
