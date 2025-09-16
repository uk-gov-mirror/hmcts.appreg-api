package uk.gov.hmcts.appregister.audit.listener;

import uk.gov.hmcts.appregister.audit.event.AuditEvent;

/** Audit logger listener for various audit events. */
@FunctionalInterface
public interface AuditOperationLifecycleListener {

    /**
     * Handles the given audit event.
     *
     * @param event the audit event to be handled
     */
    void eventPerformed(AuditEvent event);
}
