package uk.gov.hmcts.appregister.nationalcourthouse.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import uk.gov.hmcts.appregister.common.security.RoleNames;
import uk.gov.hmcts.appregister.generated.api.CourtLocationsApi;
import uk.gov.hmcts.appregister.generated.model.CourtLocationGetDetailDto;
import uk.gov.hmcts.appregister.generated.model.CourtLocationPage;
import uk.gov.hmcts.appregister.nationalcourthouse.service.CourtLocationService;

@RequiredArgsConstructor
@Controller
public class CourtLocationController implements CourtLocationsApi {
    private final CourtLocationService courtLocationService;

    // TODO - We may want to remove date here. Get all should always get most recent.
    @Override
    @PreAuthorize(RoleNames.USER_ROLE_OR_ADMIN_ROLE_RESTRICTION)
    public ResponseEntity<CourtLocationPage> getCourtLocations(
            String name,
            String code,
            LocalDate date,
            Integer page,
            Integer size,
            List<String> sort) {
        return ResponseEntity.ok().body(courtLocationService.getPageByCode(code, date));
    }

    @Override
    public ResponseEntity<CourtLocationGetDetailDto> getCourtLocationByCodeAndDate(
            String code, LocalDate date) {
        var courtLocationGetDetailDto = courtLocationService.findByCodeAndDate(code, date);
        return ResponseEntity.ok().body(courtLocationGetDetailDto);
    }
}
