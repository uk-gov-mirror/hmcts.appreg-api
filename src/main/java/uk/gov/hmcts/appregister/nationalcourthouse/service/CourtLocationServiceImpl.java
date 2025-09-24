package uk.gov.hmcts.appregister.nationalcourthouse.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.audit.AuditEventEnum;
import uk.gov.hmcts.appregister.audit.listener.AuditOperationLifecycleListener;
import uk.gov.hmcts.appregister.audit.service.AuditOperationService;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.common.entity.repository.NationalCourtHouseRepository;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.generated.model.CourtLocationGetDetailDto;
import uk.gov.hmcts.appregister.generated.model.CourtLocationPage;
import uk.gov.hmcts.appregister.nationalcourthouse.exception.CourtLocationError;
import uk.gov.hmcts.appregister.nationalcourthouse.mapper.CourtLocationMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourtLocationServiceImpl implements CourtLocationService {
    private final AuditOperationService auditService;
    private final List<AuditOperationLifecycleListener> auditLifecycleListeners;
    private final NationalCourtHouseRepository repository;
    private final CourtLocationMapper mapper;

    // TODO - Transactional?
    @Override
    public CourtLocationGetDetailDto findByCodeAndDate(String code, LocalDate date) {
        return auditService.processAudit(
                AuditEventEnum.GET_APPLICATION_CODE_AUDIT_EVENT,
                req -> {
                    final List<NationalCourtHouse> rows = repository.findActiveCourt(code, date);

                    // TODO - Why are we adding a message in here and the CourtLocationErrorEnum?
                    if (rows.isEmpty()) {
                        throw new AppRegistryException(
                                CourtLocationError.COURT_NOT_FOUND,
                                " No court found for this code %s".formatted(code));
                    } else if (rows.size() > 1) {
                        throw new AppRegistryException(
                                CourtLocationError.DUPLICATE_COURT_FOUND,
                                " Multiple courts found for this code %s".formatted(code));
                    }

                    return Optional.of(mapper.toDto(rows.getFirst()));
                },
                auditLifecycleListeners.toArray(new AuditOperationLifecycleListener[0]));
    }

    @Override
    public CourtLocationPage getPageByCode(String code, LocalDate date) {
        return null;
    }
}
