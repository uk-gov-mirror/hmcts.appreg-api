package uk.gov.hmcts.appregister.common.projection;

import java.time.LocalDate;

public interface StandardApplicantSummaryProjection {
    String getApplicantCode();

    String getApplicantName();

    String getFirstForename();

    String getSurname();

    String getSecondForename();

    String getThirdForename();

    String getTitle();

    String getAddressLine1();

    String getAddressLine2();

    String getAddressLine3();

    String getAddressLine4();

    String getAddressLine5();

    String getPostcode();

    String getPhone();

    String getMobile();

    String getEmail();

    LocalDate getApplicantStartDate();

    LocalDate getApplicantEndDate();

    String getEffectiveName();
}
