package uk.gov.hmcts.appregister.applicationlist.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.generated.model.ApplicationListCreateDto;
import uk.gov.hmcts.appregister.generated.model.ApplicationListGetDetailDto;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Validator component for {@link ApplicationListCreateDto} location fields.
 *
 * <p>Enforces the business rule that exactly one valid location option is provided: either a court
 * location code, or a combination of criminal justice area code and other location description.
 */
@Component
public class ApplicationCreateListLocationValidator extends AbstractApplicationListLocationValidator<ApplicationListCreateDto, ListLocationValidationSuccess> {

    @Override
    Function<ApplicationListCreateDto, String> getCourtLocation() {
        return ApplicationListCreateDto::getCourtLocationCode;
    }

    @Override
    Function<ApplicationListCreateDto, String> getCjaCode() {
        return ApplicationListCreateDto::getCjaCode;
    }

    @Override
    Function<ApplicationListCreateDto, String> getLocationDescription() {
        return ApplicationListCreateDto::getOtherLocationDescription;
    }

    @Override
    ListLocationValidationSuccess getResult() {
        return new ListLocationValidationSuccess();
    }
}
