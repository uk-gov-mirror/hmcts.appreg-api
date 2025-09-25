package uk.gov.hmcts.appregister.criminaljusticearea.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea_;
import uk.gov.hmcts.appregister.common.validator.AbstractSortValidator;

@Component
public class CriminalJusticeAreaSortValidator extends AbstractSortValidator {
    public CriminalJusticeAreaSortValidator() {
        super(CriminalJusticeArea_.CODE, CriminalJusticeArea_.DESCRIPTION);
    }
}
