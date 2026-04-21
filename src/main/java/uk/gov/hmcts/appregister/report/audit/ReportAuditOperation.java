package uk.gov.hmcts.appregister.report.audit;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import uk.gov.hmcts.appregister.audit.operation.AuditOperation;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;

@RequiredArgsConstructor
@Getter
public enum ReportAuditOperation implements AuditOperation {
    DOWNLOAD_REPORT_AUDIT_EVENT("Download Report", CrudEnum.READ);

    private final String eventName;

    private final CrudEnum type;
}
