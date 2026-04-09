package uk.gov.hmcts.appregister.common.projection;

import uk.gov.hmcts.appregister.common.entity.StandardApplicant;

public interface StandardApplicantSummaryProjection {
    StandardApplicant getStandardApplicant();

    String getEffectiveName();
}
