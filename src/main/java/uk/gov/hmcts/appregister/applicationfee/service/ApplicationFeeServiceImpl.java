package uk.gov.hmcts.appregister.applicationfee.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import uk.gov.hmcts.appregister.applicationfee.service.exception.ApplicationFeeCode;
import uk.gov.hmcts.appregister.common.entity.Fee;
import uk.gov.hmcts.appregister.common.entity.FeePair;
import uk.gov.hmcts.appregister.common.entity.repository.FeeRepository;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;

/** Service to handle application fee operations. */
@Service
@RequiredArgsConstructor
public class ApplicationFeeServiceImpl implements ApplicationFeeService {
    private final FeeRepository feeRepository;

    @SuppressWarnings("java:S1135")
    public FeePair resolveFeePair(String feeReference) {
        List<Fee> fee = feeRepository.findByReferenceBetweenDate(feeReference, OffsetDateTime.now(Clock.systemUTC()));

        List<Fee> main = fee.stream().filter(e -> e.isOffsite()).toList();
        List<Fee> offsite = fee.stream().filter(e -> e.isOffsite()).toList();

        // if we have more than one main or offset then we have an issue
        if (main.size() > 1 || offsite.size() > 1) {
            throw new AppRegistryException(ApplicationFeeCode.AMBIGUOUS_FEE, "Tooo many fees", null);
        }

        Fee mainFee = fees.stream().findFirst().orElse(null);

        // TODO: No offset fee in the DB yet so second fee will always be offset
        Fee offsetFee = fees.size() > 1 ? fees.get(1) : null;
        return new FeePair(mainFee, offsetFee);
    }
}
