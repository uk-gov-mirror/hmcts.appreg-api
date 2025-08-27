package uk.gov.hmcts.appregister.courtlocation.service;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;

public interface CourtLocationService {
    List<CourtLocationDto> findAll();

    CourtLocationDto findById(Long id);

    Page<CourtLocationDto> searchCourtLocations(String name, String courtType, Pageable pageable);
}
