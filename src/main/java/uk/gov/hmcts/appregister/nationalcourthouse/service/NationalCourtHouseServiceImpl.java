package uk.gov.hmcts.appregister.nationalcourthouse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.StreamSupport;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.appregister.nationalcourthouse.dto.NationalCourtHouseDto;
import uk.gov.hmcts.appregister.nationalcourthouse.mapper.NationalCourtHouseMapper;
import uk.gov.hmcts.appregister.nationalcourthouse.model.NationalCourtHouse;
import uk.gov.hmcts.appregister.nationalcourthouse.repository.NationalCourtHouseRepository;

/**
 * Service implementation for interacting with {@link NationalCourtHouse} data.
 *
 * <p>This class acts as the bridge between controllers and the repository layer: it applies
 * filtering, performs mapping into {@link NationalCourtHouseDto}, and raises appropriate HTTP-level
 * exceptions when required.
 *
 * <p>Responsibilities include:
 *
 * <ul>
 *   <li>Fetching all court locations as DTOs.
 *   <li>Looking up a specific court location by ID, throwing 404 if not found.
 *   <li>Searching for court locations with optional filters (name, court type, start/end dates) and
 *       paginated results.
 * </ul>
 */
@Service
@RequiredArgsConstructor
public class NationalCourtHouseServiceImpl implements NationalCourtHouseService {

    // Repository providing persistence access to {@link CourtLocation} entities.
    private final NationalCourtHouseRepository repository;

    // Mapper to convert entities into API-facing DTOs.
    private final NationalCourtHouseMapper mapper;

    /**
     * Fetch all court locations without filtering or pagination.
     *
     * @return list of {@link NationalCourtHouseDto} (possibly empty if no records exist)
     */
    @Override
    public List<NationalCourtHouseDto> findAll() {
        Iterable<NationalCourtHouse> all = repository.findAll(Sort.by("name").ascending());
        return StreamSupport.stream(all.spliterator(), false)
            .map(mapper::toReadDto)
            .toList();
    }

    /**
     * Find a single court location by ID.
     *
     * @param id the ID of the court location
     * @return the corresponding {@link NationalCourtHouseDto}
     * @throws ResponseStatusException with {@code 404 NOT_FOUND} if not present
     */
    @Override
    public NationalCourtHouseDto findById(Long id) {
        repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "CourtLocation not found"));
        return mapper.toReadDto(null);
    }

    /**
     * Search court locations with optional filters and pagination.
     *
     * <p>Filters applied:
     *
     * <ul>
     *   <li>{@code name} – case-insensitive substring filter.
     *   <li>{@code courtType} – exact match.
     *   <li>{@code startDateFrom}/{@code startDateTo} – inclusive range filter on {@code
     *       startDate}.
     *   <li>{@code endDateFrom}/{@code endDateTo} – inclusive range filter on {@code endDate}.
     * </ul>
     *
     * <p>Pagination and sorting are handled by {@link Pageable}.
     *
     * @return a {@link Page} of {@link NationalCourtHouseDto} matching the criteria
     */
    @Override
    public Page<NationalCourtHouseDto> search(
        String name,
        String courtType,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        Pageable pageable) {

        return repository.search(
                name, courtType, startDateFrom, startDateTo, endDateFrom, endDateTo, pageable)
            .map(mapper::toReadDto);
    }
}
