package uk.gov.hmcts.appregister.resultcode.mapper;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;
import uk.gov.hmcts.appregister.resultcode.model.ResultCode;

/**
 * Mapper component responsible for converting between JPA {@link ResultCode} entities
 * and their corresponding API-facing DTOs.
 *
 * <p>This class ensures that persistence-layer entities (with JPA annotations and internal
 * schema concerns) are not leaked to API clients. It also provides smaller DTOs for
 * specific use cases such as list views.</p>
 *
 * <p><strong>Responsibilities:</strong>
 * <ul>
 *   <li>Map a full {@link ResultCode} entity into a complete {@link ResultCodeDto}.</li>
 *   <li>Reconstruct a {@link ResultCode} entity from a {@link ResultCodeDto}, e.g. for
 *       creating/updating in service or test code.</li>
 *   <li>Map a {@link ResultCode} entity into a lightweight {@link ResultCodeListItemDto}
 *       containing only fields relevant for list views.</li>
 * </ul>
 */
@Component
public class ResultCodeMapper {

    /**
     * Converts a {@link ResultCode} entity into a {@link ResultCodeDto}.
     *
     * @param entity the entity to map; may be {@code null}
     * @return a populated {@link ResultCodeDto}, or {@code null} if the input was null
     */
    public ResultCodeDto toReadDto(ResultCode entity) {
        if (entity == null) {
            // Defensive: return null if no entity provided.
            return null;
        }

        // Map all fields from the entity into the full DTO.
        return new ResultCodeDto(
            entity.getId(),
            entity.getResultCode(),
            entity.getTitle(),
            entity.getWording(),
            entity.getLegislation(),
            entity.getDestinationEmail1(),
            entity.getDestinationEmail2(),
            entity.getStartDate(),
            entity.getEndDate()
        );
    }

    /**
     * Converts a {@link ResultCodeDto} back into a {@link ResultCode} entity.
     *
     * <p>This is primarily useful in test code, or where you need to persist a DTO
     * back to the database via the repository.</p>
     *
     * @param dto the DTO to map; may be {@code null}
     * @return a new {@link ResultCode} entity built from the DTO fields, or {@code null} if input was null
     */
    public ResultCode toEntityFromReadDto(ResultCodeDto dto) {
        if (dto == null) {
            // Defensive: return null if no DTO provided.
            return null;
        }

        // Use Lombok-generated builder on the entity to map fields.
        return ResultCode.builder()
            .id(dto.id())
            .resultCode(dto.resultCode())
            .title(dto.title())
            .wording(dto.wording())
            .legislation(dto.legislation())
            .destinationEmail1(dto.destinationEmail1())
            .destinationEmail2(dto.destinationEmail2())
            .startDate(dto.startDate())
            .endDate(dto.endDate())
            .build();
    }

    /**
     * Converts a {@link ResultCode} entity into a lightweight {@link ResultCodeListItemDto}.
     *
     * <p>This variant is intended for paginated list/search responses, where only
     * ID, code, and title are required.</p>
     *
     * @param entity the entity to map; may be {@code null}
     * @return a {@link ResultCodeListItemDto} containing only ID, code, and title,
     * or {@code null} if input was null
     */
    public ResultCodeListItemDto toListItem(ResultCode entity) {
        if (entity == null) {
            // Defensive: return null if no entity provided.
            return null;
        }

        // Map only a subset of fields.
        return new ResultCodeListItemDto(
            entity.getId(),
            entity.getResultCode(),
            entity.getTitle()
        );
    }
}
