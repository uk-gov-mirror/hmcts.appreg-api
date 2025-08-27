package uk.gov.hmcts.appregister.resultcode.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;

/**
 * Service layer contract for interacting with {@link uk.gov.hmcts.appregister.resultcode.model.ResultCode} data.
 *
 * <p>This interface defines operations available for reading and searching result codes, providing
 * an abstraction between controllers and the persistence layer. Implementations are responsible for:
 * <ul>
 *   <li>Fetching all result codes as full DTOs.</li>
 *   <li>Looking up a single result code by its short code.</li>
 *   <li>Searching for result codes with optional filters and pagination support.</li>
 * </ul>
 *
 * <p>Controllers depend on this interface rather than directly on repositories. Implementations
 * will typically delegate to {@code ResultCodeRepository} and use {@code ResultCodeMapper} for
 * entity-to-DTO conversion.</p>
 */
public interface ResultCodeService {

    /**
     * Fetches all result codes without filtering or pagination.
     *
     * <p>Intended for scenarios where a full list is needed, such as admin data exports or
     * initialisation tasks. For user-facing APIs, prefer the {@link #search} method which
     * provides pagination and filtering.</p>
     *
     * @return list of all {@link ResultCodeDto} records; may be empty if no records exist
     */
    List<ResultCodeDto> findAll();

    /**
     * Finds a single result code by its business identifier (short code).
     *
     * @param code the short code string to search for (must match the {@code resolution_code} column)
     * @return the corresponding {@link ResultCodeDto}
     * @throws org.springframework.web.server.ResponseStatusException with status 404 (NOT_FOUND)
     *         if no result code exists for the given code
     */
    ResultCodeDto findByCode(String code);

    /**
     * Searches for result codes using optional filters and pagination.
     *
     * <p>Filters applied:
     * <ul>
     *   <li>{@code code} – case-insensitive substring filter on the result code.</li>
     *   <li>{@code title} – case-insensitive substring filter on the title.</li>
     *   <li>{@code startDateFrom}/{@code startDateTo} – inclusive range on {@code startDate}.</li>
     *   <li>{@code endDateFrom}/{@code endDateTo} – inclusive range on {@code endDate}.</li>
     * </ul>
     * </p>
     *
     * <p><strong>Pagination:</strong> Provided via {@link Pageable}, which allows the caller
     * to specify page number, size, and sort order. Default sorting is typically by title ASC.</p>
     *
     * @param code optional substring filter for the result code value
     * @param title optional substring filter for the result code title
     * @param startDateFrom optional lower bound (inclusive) for {@code startDate}
     * @param startDateTo optional upper bound (inclusive) for {@code startDate}
     * @param endDateFrom optional lower bound (inclusive) for {@code endDate}
     * @param endDateTo optional upper bound (inclusive) for {@code endDate}
     * @param pageable pagination and sorting information
     * @return a {@link Page} of {@link ResultCodeListItemDto} records matching the criteria;
     *         will include total count and pagination metadata
     */
    Page<ResultCodeListItemDto> search(
        String code,
        String title,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        Pageable pageable
    );
}
