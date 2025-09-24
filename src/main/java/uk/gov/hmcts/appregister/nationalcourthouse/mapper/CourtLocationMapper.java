package uk.gov.hmcts.appregister.nationalcourthouse.mapper;

import java.time.LocalDate;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.generated.model.CourtLocationGetDetailDto;

@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CourtLocationMapper {
    @Mapping(target = "name", source = "name")
    @Mapping(target = "locationCode", source = "courtLocationCode")
    @Mapping(target = "startDate", source = "startDate")
    @Mapping(target = "endDate", source = "endDate")
    CourtLocationGetDetailDto toDto(NationalCourtHouse entity);

    default JsonNullable<LocalDate> map(LocalDate date) {
        return (date != null) ? JsonNullable.of(date) : JsonNullable.of(null);
    }
}
