package uk.gov.hmcts.appregister.applicationlist.dto;

import java.time.LocalDate;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;

public record ApplicationListDto(
        Long id,
        String status,
        LocalDate date,
        String time,
        String description,
        CourtLocationDto courthouse,
        String changedBy,
        LocalDate changedDate,
        Integer version) {}
