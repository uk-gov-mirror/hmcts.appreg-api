package uk.gov.hmcts.appregister.courtlocation.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
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
        final List<CourtLocation> courtHouses = repository.findAll();

        return courtHouses.stream().map(mapper::toReadDto).toList();
    }

    @Override
    public CourtLocationDto findById(Long id) {
        CourtLocation courtHouse =
                repository
                        .findById(id)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.NOT_FOUND, "Courthouse not found"));

        return mapper.toReadDto(courtHouse);
    }
}
