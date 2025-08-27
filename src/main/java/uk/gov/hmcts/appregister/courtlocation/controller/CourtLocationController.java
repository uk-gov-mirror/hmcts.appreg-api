package uk.gov.hmcts.appregister.courtlocation.controller;

import static java.util.Objects.requireNonNullElse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationDto;
import uk.gov.hmcts.appregister.courtlocation.dto.CourtLocationPageResponse;
import uk.gov.hmcts.appregister.courtlocation.service.CourtLocationService;

@RestController
@RequestMapping("/court-locations")
@RequiredArgsConstructor
public class CourtLocationController {

    private static final int DEFAULT_PAGE = 1;
    private static final int DEFAULT_PAGE_SIZE = 10;
    private static final int MAX_PAGE_SIZE = 100;

    private final CourtLocationService service;

    @Operation(summary = "Get all courthouses", operationId = "getAllCourthouses")
    @ApiResponse(responseCode = "200", description = "List of courthouses retrieved successfully")
    @GetMapping
    public ResponseEntity<CourtLocationPageResponse> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String courtType,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize) {
        int p = requireNonNullElse(page, DEFAULT_PAGE);
        int s = requireNonNullElse(pageSize, DEFAULT_PAGE_SIZE);
        if (p < 1 || s < 1 || s > MAX_PAGE_SIZE) {
            return ResponseEntity.badRequest().build(); // 400
        }

        Pageable pageable = PageRequest.of(p - 1, s, Sort.by("name").ascending());

        Page<CourtLocationDto> pageDto = service.searchCourtLocations(name, courtType, pageable);

        CourtLocationPageResponse body =
                new CourtLocationPageResponse(
                        pageDto.getContent(), pageDto.getTotalElements(), p, s);

        return ResponseEntity.ok(body);
    }

    @Operation(summary = "Get a specific courthouse by ID", operationId = "getCourtHouseById")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Courthouse found"),
        @ApiResponse(responseCode = "404", description = "Courthouse not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<CourtLocationDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }
}
