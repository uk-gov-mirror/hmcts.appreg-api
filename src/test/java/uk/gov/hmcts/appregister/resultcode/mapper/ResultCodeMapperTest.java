package uk.gov.hmcts.appregister.resultcode.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;
import uk.gov.hmcts.appregister.resultcode.model.ResultCode;

class ResultCodeMapperTest {

    private final ResultCodeMapper mapper = new ResultCodeMapper();

    // -------- toReadDto --------

    @Test
    void toReadDto_whenNull_returnsNull() {
        // Defensive: mapper should handle nulls gracefully
        assertThat(mapper.toReadDto(null)).isNull();
    }

    @Test
    void toReadDto_mapsAllFields() {
        // Given: a fully-populated entity
        LocalDate start = LocalDate.of(2024, 1, 1);
        LocalDate end   = LocalDate.of(2025, 12, 31);

        ResultCode entity = ResultCode.builder()
            .id(42L)
            .resultCode("RC123")
            .title("Successful Appeal")
            .wording("The appeal is successful for {APPLICANT}.")
            .legislation("Some Act 1999 s.1")
            .destinationEmail1("primary@example.com")
            .destinationEmail2("secondary@example.com")
            .startDate(start)
            .endDate(end)
            .build();

        // When: we map to a read DTO
        ResultCodeDto dto = mapper.toReadDto(entity);

        // Then: every field should be copied 1:1
        assertThat(dto.id()).isEqualTo(42L);
        assertThat(dto.resultCode()).isEqualTo("RC123");
        assertThat(dto.title()).isEqualTo("Successful Appeal");
        assertThat(dto.wording()).isEqualTo("The appeal is successful for {APPLICANT}.");
        assertThat(dto.legislation()).isEqualTo("Some Act 1999 s.1");
        assertThat(dto.destinationEmail1()).isEqualTo("primary@example.com");
        assertThat(dto.destinationEmail2()).isEqualTo("secondary@example.com");
        assertThat(dto.startDate()).isEqualTo(start);
        assertThat(dto.endDate()).isEqualTo(end);
    }

    // -------- toEntityFromReadDto --------

    @Test
    void toEntityFromReadDto_whenNull_returnsNull() {
        // Defensive: mapper should handle nulls gracefully
        assertThat(mapper.toEntityFromReadDto(null)).isNull();
    }

    @Test
    void toEntityFromReadDto_mapsAllFields() {
        // Given: a fully-populated DTO
        LocalDate start = LocalDate.of(2023, 2, 2);
        LocalDate end   = LocalDate.of(2024, 3, 3);

        ResultCodeDto dto = new ResultCodeDto(
            7L,
            "RC777",
            "Conviction Recorded",
            "The court records a conviction against {DEFENDANT}.",
            "Criminal Justice Act 2003",
            "dest1@hmcts.net",
            "dest2@hmcts.net",
            start,
            end
        );

        // When: we map back to entity
        ResultCode entity = mapper.toEntityFromReadDto(dto);

        // Then: builder-produced entity should mirror the DTO
        assertThat(entity.getId()).isEqualTo(7L);
        assertThat(entity.getResultCode()).isEqualTo("RC777");
        assertThat(entity.getTitle()).isEqualTo("Conviction Recorded");
        assertThat(entity.getWording()).isEqualTo("The court records a conviction against {DEFENDANT}.");
        assertThat(entity.getLegislation()).isEqualTo("Criminal Justice Act 2003");
        assertThat(entity.getDestinationEmail1()).isEqualTo("dest1@hmcts.net");
        assertThat(entity.getDestinationEmail2()).isEqualTo("dest2@hmcts.net");
        assertThat(entity.getStartDate()).isEqualTo(start);
        assertThat(entity.getEndDate()).isEqualTo(end);
    }

    // -------- toListItem --------

    @Test
    void toListItem_whenNull_returnsNull() {
        // Defensive: mapper should handle nulls gracefully
        assertThat(mapper.toListItem(null)).isNull();
    }

    @Test
    void toListItem_mapsSubsetOfFields() {
        // Given: an entity with many fields
        ResultCode entity = ResultCode.builder()
            .id(99L)
            .resultCode("RC999")
            .title("Case Dismissed")
            .wording("Dismissed wording…")
            .legislation("Some Other Act")
            .destinationEmail1("one@example.com")
            .destinationEmail2("two@example.com")
            .startDate(LocalDate.of(2020, 1, 1))
            .endDate(null)
            .build();

        // When: we map to list item
        ResultCodeListItemDto listItem = mapper.toListItem(entity);

        // Then: only id, code, title should be set
        assertThat(listItem.id()).isEqualTo(99L);
        assertThat(listItem.code()).isEqualTo("RC999");
        assertThat(listItem.title()).isEqualTo("Case Dismissed");
    }
}
