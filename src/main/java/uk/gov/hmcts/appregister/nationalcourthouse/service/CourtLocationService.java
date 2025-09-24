package uk.gov.hmcts.appregister.nationalcourthouse.service;

import java.time.LocalDate;
import uk.gov.hmcts.appregister.generated.model.CourtLocationGetDetailDto;
import uk.gov.hmcts.appregister.generated.model.CourtLocationPage;

public interface CourtLocationService {
    CourtLocationGetDetailDto findByCodeAndDate(String code, LocalDate date);

    CourtLocationPage getPageByCode(String code, LocalDate date);
}
