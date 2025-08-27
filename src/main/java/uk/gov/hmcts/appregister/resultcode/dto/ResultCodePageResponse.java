package uk.gov.hmcts.appregister.resultcode.dto;

import java.util.List;

public record ResultCodePageResponse(
    List<ResultCodeListItemDto> results,
    long totalCount,
    int page,
    int pageSize
) {
}
