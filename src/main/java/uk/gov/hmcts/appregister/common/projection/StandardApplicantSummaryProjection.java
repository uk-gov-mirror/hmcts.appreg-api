package uk.gov.hmcts.appregister.common.projection;

import uk.gov.hmcts.appregister.common.entity.StandardApplicant;

public interface StandardApplicantProjection {
    StandardApplicant getStandardApplicant();

    String getEffectiveName();
}
