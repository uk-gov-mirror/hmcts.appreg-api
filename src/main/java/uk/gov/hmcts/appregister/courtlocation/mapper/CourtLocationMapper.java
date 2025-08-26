package uk.gov.hmcts.appregister.courtlocation.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;
import uk.gov.hmcts.appregister.courtlocation.model.CourtLocation;

@Component
public class CourtLocationMapper {

    public CourtLocationDto toReadDto(CourtLocation entity) {
        if (entity == null) {
            return null;
        }

        return new CourtLocationDto(
                entity.getId(),
                entity.getName(),
                entity.getCourtType(),
                entity.getStartDate(),
                entity.getEndDate(),
                entity.getLocationId(),
                entity.getPsaId(),
                entity.getCourtLocationCode(),
                entity.getWelshName(),
                entity.getOrgId());
    }
}
