package uk.gov.hmcts.appregister.audit.event;

import java.util.Optional;
import uk.gov.hmcts.appregister.audit.operation.AuditOperation;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;

/**
 * Represents the start of an operation audit event.
 */
public class StartEvent extends AuditEvent {
    public StartEvent(
            AuditOperation requestAction, String messageUuid, Optional<Keyable> oldValue) {
        super(
                requestAction,
                OperationStatus.STARTED,
                NO_VALUE,
                messageUuid,
                Optional.empty(),
                oldValue);
    }
}
