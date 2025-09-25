package uk.gov.hmcts.appregister.courtlocation.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse_;
import uk.gov.hmcts.appregister.common.validator.AbstractSortValidator;

/**
 * Sort validator for Court Location queries.
 *
 * <p>Restricts sorting to a predefined set of allowed properties to prevent invalid or unsafe
 * database access through arbitrary sort fields.
 */
@Component
public class CourtLocationsSortValidator extends AbstractSortValidator {

    /**
     * Creates a validator with allowed sort properties for Court Locations.
     *
     * <p>Currently limited to "name" and "code".
     */
    public CourtLocationsSortValidator() {
        super(NationalCourtHouse_.NAME, NationalCourtHouse_.COURT_LOCATION_CODE);
    }
}
