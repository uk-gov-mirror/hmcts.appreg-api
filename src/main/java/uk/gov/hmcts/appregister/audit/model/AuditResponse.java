package uk.gov.hmcts.appregister.audit.model;

import lombok.Builder;
import lombok.Getter;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.service.OperationStatus;

@Getter
@Builder
public class AuditResponse implements BaseAuditPayload {
    private final AuditEventEnum requestAction;

    @Builder.Default private final String messageUuid = NO_VALUE;
    private final OperationStatus messageStatus;

    @Builder.Default private final String messageContent = NO_VALUE;
}
