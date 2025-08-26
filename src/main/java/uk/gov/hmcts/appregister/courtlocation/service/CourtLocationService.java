package uk.gov.hmcts.appregister.courtlocation.service;

import java.util.List;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;

public interface CourtLocationService {
    List<CourtLocationDto> findAll();

    CourtLocationDto findById(Long id);
}
