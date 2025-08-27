package uk.gov.hmcts.appregister.courtlocation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;
import uk.gov.hmcts.appregister.courtlocation.mapper.CourtLocationMapper;
import uk.gov.hmcts.appregister.courtlocation.model.CourtLocation;
import uk.gov.hmcts.appregister.courtlocation.repository.CourtLocationRepository;

@Service
@RequiredArgsConstructor
public class CourtLocationServiceImpl implements CourtLocationService {

    private final CourtLocationRepository repository;
    private final CourtLocationMapper mapper;

    @Override
    public List<CourtLocationDto> findAll() {
        final List<CourtLocation> courtLocations = repository.findAll();

        return courtLocations.stream().map(mapper::toReadDto).toList();
    }

    @Override
    public CourtLocationDto findById(Long id) {
        CourtLocation courtLocation =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "CourtLocation not found"));

        return mapper.toReadDto(courtLocation);
    }

    @Override
    public Page<CourtLocationDto> searchCourtLocations(
            String name, String courtType, Pageable pageable) {
        Specification<CourtLocation> spec =
                Specification.allOf(nameSpec(name), courtTypeSpec(courtType));
        return repository.findAll(spec, pageable).map(mapper::toReadDto);
    }

    private Specification<CourtLocation> nameSpec(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        return (root, q, cb) -> cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }

    private Specification<CourtLocation> courtTypeSpec(String ct) {
        if (ct == null || ct.isBlank()) {
            return null;
        }
        return (root, q, cb) -> cb.equal(root.get("courtType"), ct);
    }
}
