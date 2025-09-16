package uk.gov.hmcts.appregister.audit.event;

import java.util.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import uk.gov.hmcts.appregister.audit.model.AuditRequest;
import uk.gov.hmcts.appregister.audit.model.AuditResponse;

/** Encapsulates the audit request and response. */
@Getter
@RequiredArgsConstructor
@ToString
public class AuditEvent {
    private final AuditRequest request;
    private final Optional<AuditResponse> response;
}
