package uk.gov.hmcts.appregister.applicationfee.service;

import java.util.Optional;
import uk.gov.hmcts.appregister.common.entity.Fee;
import uk.gov.hmcts.appregister.common.entity.FeePair;

public interface ApplicationFeeService {
    FeePair resolveFeePair(String feeReference);
}
