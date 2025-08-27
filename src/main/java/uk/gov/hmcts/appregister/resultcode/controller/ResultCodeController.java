package uk.gov.hmcts.appregister.resultcode.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodeListItemDto;
import uk.gov.hmcts.appregister.resultcode.dto.ResultCodePageResponse;
import uk.gov.hmcts.appregister.resultcode.service.ResultCodeService;

import static java.util.Objects.requireNonNullElse;

@RestController
@RequestMapping("/result-codes")
@RequiredArgsConstructor
public class ResultCodeController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final ResultCodeService service;

    @Operation(summary = "Get result codes (paginated, filterable)")
    @ApiResponse(responseCode = "200", description = "List of result codes retrieved successfully")
    @GetMapping
    public ResponseEntity<ResultCodePageResponse> list(
        @RequestParam(required = false) String code,
        @RequestParam(required = false) String title,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDateTo,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateFrom,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDateTo,
        @RequestParam(required = false) Integer page,
        @RequestParam(required = false) Integer pageSize) {

        final int p = requireNonNullElse(page, DEFAULT_PAGE);
        final int s = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);

        // Basic input validation
        if (p < 1 || s < 1 || s > MAX_PAGE_SIZE) {
            return ResponseEntity.badRequest().build();
        }
        if (startDateFrom != null && startDateTo != null && startDateFrom.isAfter(startDateTo)) {
            return ResponseEntity.badRequest().build();
        }
        if (endDateFrom != null && endDateTo != null && endDateFrom.isAfter(endDateTo)) {
            return ResponseEntity.badRequest().build();
        }

        Pageable pageable = PageRequest.of(p - 1, s, Sort.by("title").ascending());

        Page<ResultCodeListItemDto> pageDto = service.search(
            code, title, startDateFrom, startDateTo, endDateFrom, endDateTo, pageable);

        ResultCodePageResponse body = new ResultCodePageResponse(
            pageDto.getContent(), pageDto.getTotalElements(), p, s);

        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Get a result code by code", operationId = "getResultCodeByCode")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Result code found"),
        @ApiResponse(responseCode = "404", description = "Result code not found")
    })
    @GetMapping("/{code}")
    public ResponseEntity<ResultCodeDto> getByCode(@PathVariable String code) {
        return ResponseEntity.ok(service.findByCode(code));
    }
}
