package uk.gov.hmcts.appregister.resultcode.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;

public interface ResultCodeService {
    List<ResultCodeDto> findAll();

    ResultCodeDto findByCode(String code);

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
