package uk.gov.hmcts.appregister.resultcode.dto;


public record ResultCodeListItemDto(
    Long id,
    String code,
    String title
) {
}
