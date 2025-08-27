package uk.gov.hmcts.appregister.resultcode.dto;

import java.util.List;

/**
 * Wrapper DTO representing a page of {@link ResultCodeListItemDto} results.
 *
 * <p>This is the standard paginated response structure returned by
 * {@code GET /result-codes}. It ensures frontend clients receive both the
 * result set and metadata required for pagination controls.</p>
 *
 * <p><strong>Fields:</strong>
 * <ul>
 *   <li>{@code results} – the list of result code list items for the current page.</li>
 *   <li>{@code totalCount} – total number of matching result codes across all pages.</li>
 *   <li>{@code page} – current page number (1-based, aligned with API contract).</li>
 *   <li>{@code pageSize} – number of results per page (must respect max limits).</li>
 * </ul>
 *
 * <p>All fields are immutable since this is a Java {@code record}.</p>
 */
public record ResultCodePageResponse(

    /** List of result code items for this page. May be empty if no results match. */
    List<ResultCodeListItemDto> results,

    /** Total number of result codes matching the filter across all pages. */
    long totalCount,

    /** Current page number (1-based, consistent with API semantics). */
    int page,

    /** Number of items requested per page (subject to max size constraints). */
    int pageSize
) {
}
