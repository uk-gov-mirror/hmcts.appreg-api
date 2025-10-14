package uk.gov.hmcts.appregister.applicationlist.validator;

import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import uk.gov.hmcts.appregister.applicationlist.exception.ApplicationListError;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationListRepository;
import uk.gov.hmcts.appregister.common.entity.repository.CriminalJusticeAreaRepository;
import uk.gov.hmcts.appregister.common.entity.repository.NationalCourtHouseRepository;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.validator.Validator;

import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Validator component for location fields.
 *
 * <p>Enforces the business rule that exactly one valid location option is provided: either a court
 * location code, or a combination of criminal justice area code and other location description.
 *
 * This class should be provided with method references to the relevant getters of the DTO to be validated.
 */
@RequiredArgsConstructor
public abstract class AbstractApplicationListLocationValidator<T, O extends ListLocationValidationSuccess> implements Validator<T, O> {
    abstract Function<T, String> getCourtLocation();
    abstract Function<T, String> getCjaCode();
    abstract Function<T, String> getLocationDescription();

    /**
     *
     */
    abstract O getResult();
    protected ApplicationListRepository applicationListRepository;

    protected NationalCourtHouseRepository courtHouseRepository;

    protected CriminalJusticeAreaRepository criminalJusticeAreaRepository;
    protected static final int SINGLE_RECORD = 1;

    /**
     * Validates the location fields of the given {@link uk.gov.hmcts.appregister.generated.model.ApplicationListCreateDto}.
     *
     * <p>A DTO is considered valid if and only if one of the following holds (exclusive OR):
     *
     * <ul>
     *   <li>{@code courtLocationCode} is non-null and non-blank
     *   <li>both {@code cjaCode} and {@code otherLocationDescription} are non-null and non-blank
     * </ul>
     *
     * <p>Supplying neither option, or supplying both options at the same time, is invalid.
     *
     * @param dto the DTO to validate (must not be {@code null})
     * @throws uk.gov.hmcts.appregister.common.exception.AppRegistryException if the location combination does not satisfy the XOR rule
     */
    @Override
    public void validate(T dto) {
        validate(dto, null);
    }

    public <R> R validate(T dto, BiFunction<T, O, R> createApplicationSupplier) {
        String court = getCourtLocation().apply(dto);
        String cja = getCjaCode().apply(dto);
        String other = getLocationDescription().apply(dto);

        boolean hasCourt = StringUtils.hasText(court);
        boolean hasCja = StringUtils.hasText(cja);
        boolean hasOther = StringUtils.hasText(other);

        // XOR rule: either courtLocationCode OR (cjaCode AND otherLocationDescription)
        boolean valid = hasCourt ^ (hasCja && hasOther);

        if (!valid) {
            throw new AppRegistryException(
                    ApplicationListError.INVALID_LOCATION_COMBINATION,
                    "Provide either 'courtLocation', or both a 'cja code' and a 'otherLocationDescription'.");
        }

        O createApplication = getResult();
        // validate the court and justice area
        if (hasCourt) {
            validateCourt(dto, createApplication);
        } else {
            validateCja(dto, createApplication);
        }

        if (createApplicationSupplier != null) {
            return createApplicationSupplier.apply(dto, createApplication);
        }

        return null;
    }

    /**
     * validates the court
     * @param dto The dto type top validate
     */
    private void validateCourt(T dto, O createApplication) {
        var courtCode = getCourtLocation().apply(dto).trim();
        final List<NationalCourtHouse> courts = courtHouseRepository.findActiveCourts(courtCode);

        if (courts.isEmpty()) {
            throw new AppRegistryException(
                    ApplicationListError.COURT_NOT_FOUND,
                    "No court found for code '%s'".formatted(courtCode));
        } else if (courts.size() > SINGLE_RECORD) {
            throw new AppRegistryException(
                    ApplicationListError.DUPLICATE_COURT_FOUND,
                    "Multiple courts found for code '%s'".formatted(courtCode));
        }

        createApplication.setNationalCourtHouse(courts.getFirst());
    }

    /**
     * Validate the court justice area
     * @param dto The dto being validated
     */
    private void validateCja(T dto, O createApplication) {
        var cjaCode =getCjaCode().apply(dto).trim();
        final List<CriminalJusticeArea> criminalJusticeAreas = criminalJusticeAreaRepository.findByCode(cjaCode);

        if (criminalJusticeAreas.isEmpty()) {
            throw new AppRegistryException(
                    ApplicationListError.CJA_NOT_FOUND,
                    "No Criminal Justice Areas found for code '%s'".formatted(cjaCode));
        } else if (criminalJusticeAreas.size() > SINGLE_RECORD) {
            throw new AppRegistryException(
                    ApplicationListError.DUPLICATE_CJA_FOUND,
                    "Multiple Criminal Justice Areas found for code '%s'".formatted(cjaCode));
        }

        createApplication.setCriminalJusticeArea(criminalJusticeAreas.getFirst());
    }
}
