package uk.gov.hmcts.appregister.courtlocation.dto;

import java.time.LocalDate;

public record CourtLocationDto(
        Long id,
        String name,
        String courtType,
        LocalDate startDate,
        LocalDate endDate,
        Long locationId,
        Long psaId,
        String courtLocationCode,
        String welshName,
        Long orgId) {}
