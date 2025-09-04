package uk.gov.hmcts.appregister.applicationfee.service;

import uk.gov.hmcts.appregister.common.entity.FeePair;

public interface ApplicationFeeService {
  FeePair resolveFeePair(String feeReference);
}
