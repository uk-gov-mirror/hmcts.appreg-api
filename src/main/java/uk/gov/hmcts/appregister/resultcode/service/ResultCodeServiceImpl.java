package uk.gov.hmcts.appregister.resultcode.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;
import uk.gov.hmcts.appregister.resultcode.mapper.ResultCodeMapper;
import uk.gov.hmcts.appregister.resultcode.model.ResultCode;
import uk.gov.hmcts.appregister.resultcode.repository.ResultCodeRepository;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class ResultCodeServiceImpl implements ResultCodeService {

    private final ResultCodeRepository repository;
    private final ResultCodeMapper mapper;

    @Override
    public List<ResultCodeDto> findAll() {
        final List<ResultCode> resultCodes = repository.findAll();

        return resultCodes.stream().map(mapper::toReadDto).toList();
    }

    @Override
    public ResultCodeDto findByCode(String code) {

        final ResultCode resultCode =
            repository
                .findByResultCode(code)
                .orElseThrow(
                    () ->
                        new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "ResultCode not found"));

        return mapper.toReadDto(resultCode);
    }

    @Override
    public Page<ResultCodeListItemDto> search(
        String code,
        String title,
        LocalDate startDateFrom,
        LocalDate startDateTo,
        LocalDate endDateFrom,
        LocalDate endDateTo,
        Pageable pageable) {

        Specification<ResultCode> spec = Specification.allOf(
            codeLikeSpec(code),
            titleLikeSpec(title),
            startDateFromSpec(startDateFrom),
            startDateToSpec(startDateTo),
            endDateFromSpec(endDateFrom),
            endDateToSpec(endDateTo)
        );

        return repository.findAll(spec, pageable).map(mapper::toListItem);
    }

    // code ILIKE %code%
    private Specification<ResultCode> codeLikeSpec(String code) {
        if (code == null || code.isBlank()) return null;
        final String needle = "%" + code.toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("resultCode")), needle);
    }

    // title ILIKE %title%
    private Specification<ResultCode> titleLikeSpec(String title) {
        if (title == null || title.isBlank()) return null;
        final String needle = "%" + title.toLowerCase() + "%";
        return (root, q, cb) -> cb.like(cb.lower(root.get("title")), needle);
    }

    // start_date >= from
    private Specification<ResultCode> startDateFromSpec(LocalDate from) {
        return nonNull(from) ? (r, q, cb) -> cb.greaterThanOrEqualTo(r.get("startDate"), from) : null;
    }

    // start_date <= to
    private Specification<ResultCode> startDateToSpec(LocalDate to) {
        return nonNull(to) ? (r, q, cb) -> cb.lessThanOrEqualTo(r.get("startDate"), to) : null;
    }

    // end_date >= from  OR end_date IS NULL (treat open-ended as ongoing)
    private Specification<ResultCode> endDateFromSpec(LocalDate from) {
        return nonNull(from)
            ? (r, q, cb) -> cb.or(cb.isNull(r.get("endDate")), cb.greaterThanOrEqualTo(r.get("endDate"), from))
            : null;
    }

    // end_date <= to  (NULLs excluded by default)
    private Specification<ResultCode> endDateToSpec(LocalDate to) {
        return nonNull(to) ? (r, q, cb) -> cb.lessThanOrEqualTo(r.get("endDate"), to) : null;
    }
}
