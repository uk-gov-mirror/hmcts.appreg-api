package uk.gov.hmcts.appregister.audit.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.service.OperationStatus;

@Builder
@Getter
@ToString
public class AuditRequest implements BaseAuditPayload {
    @Setter private AuditEventEnum requestAction;
    @Builder.Default private final String messageUuid = NO_VALUE;
    private final OperationStatus messageStatus;

    @Override
    public String getMessageContent() {
        return NO_VALUE;
    }
}
