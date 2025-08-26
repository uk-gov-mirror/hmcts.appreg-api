package uk.gov.hmcts.appregister.courtlocation.dto;

import java.util.List;

public record CourtLocationPageResponse(
    List<CourtLocationDto> results,
    long totalCount,
    int page,
    int pageSize
) {}
