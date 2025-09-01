package uk.gov.hmcts.appregister.nationalcourthouse.dto;

import java.time.LocalDate;

/**
 * Immutable Data Transfer Object (DTO) for National Court House records.
 *
 * <p>This record represents the data exposed by the National Court House API endpoints.
 * It decouples the API response contract from the JPA entity model, ensuring that
 * persistence-layer details are hidden from consumers.
 *
 * <p><strong>Key characteristics:</strong>
 * <ul>
 *   <li>Declared as a Java {@code record}, making it inherently immutable and
 *       suitable for safe API exposure.</li>
 *   <li>Field names are normalised for frontend consumption, while still mapping
 *       closely to the underlying {@code national_court_houses} table.</li>
 *   <li>Used directly in paginated Spring Data {@link org.springframework.data.domain.Page}
 *       results, so clients receive both content and pagination metadata.</li>
 * </ul>
 *
 * <p><strong>Field details:</strong>
 * <ul>
 *   <li>{@code id} – unique identifier of the courthouse record.</li>
 *   <li>{@code name} – courthouse name (e.g. "Cardiff Crown Court").</li>
 *   <li>{@code courtType} – type of court (e.g. "CROWN", "MAGISTRATES").</li>
 *   <li>{@code startDate} – date the record became effective (inclusive).</li>
 *   <li>{@code endDate} – date the record ended (inclusive), or {@code null} if still active.</li>
 *   <li>{@code locationId} – foreign key reference to a linked location record.</li>
 *   <li>{@code psaId} – optional foreign key to a Petty Sessional Area (PSA).</li>
 *   <li>{@code courtLocationCode} – business reference code used for integrations
 *       and reference data.</li>
 *   <li>{@code welshName} – Welsh-language courthouse name, if applicable.</li>
 *   <li>{@code orgId} – organisational identifier linking the courthouse to its parent organisation.</li>
 * </ul>
 */
public record NationalCourtHouseDto(
    Long id,
    String name,
    String courtType,
    LocalDate startDate,
    LocalDate endDate,
    Long locationId,
    Long psaId,
    String courtLocationCode,
    String welshName,
    Long orgId) {
}
