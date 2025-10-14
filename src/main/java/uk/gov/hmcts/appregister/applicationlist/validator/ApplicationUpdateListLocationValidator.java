package uk.gov.hmcts.appregister.applicationlist.validator;

import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.applicationlist.exception.ApplicationListError;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;
import uk.gov.hmcts.appregister.common.exception.AppRegistryException;
import uk.gov.hmcts.appregister.common.model.PayloadForUpdate;
import uk.gov.hmcts.appregister.generated.model.ApplicationListUpdateDto;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Validator component for {@link uk.gov.hmcts.appregister.generated.model.ApplicationListCreateDto} location fields.
 *
 * <p>Enforces the business rule that exactly one valid location option is provided: either a court
 * location code, or a combination of criminal justice area code and other location description.
 */
@Component
public class ApplicationUpdateListLocationValidator extends AbstractApplicationListLocationValidator<PayloadForUpdate<ApplicationListUpdateDto>, ListUpdateValidationSuccess> {

    @Override
    public void validate(PayloadForUpdate<ApplicationListUpdateDto> dto) {
        List<ApplicationList> applicationListList = applicationListRepository.findByUuid(dto.getId());

        // if there is more than one record with the same UUID, it is an error
        if (applicationListList.isEmpty()) {
            throw new AppRegistryException(
                    ApplicationListError.APPLICATION_LIST_NOT_FOUND,
                    "The application list id does not exist : %s".formatted(dto.getId()));
        }

        super.validate(dto);
    }

    @Override
    ListUpdateValidationSuccess getResult() {
        return new ListUpdateValidationSuccess();
    }

    @Override
    public <R> R validate(PayloadForUpdate<ApplicationListUpdateDto> dto, BiFunction<PayloadForUpdate<ApplicationListUpdateDto>, ListUpdateValidationSuccess, R> createApplicationSupplier) {
        List<ApplicationList> applicationListList = applicationListRepository.findByUuid(dto.getId());

        // if there is more than one record with the same UUID, it is an error
        if (applicationListList.isEmpty()) {
            throw new AppRegistryException(
                    ApplicationListError.APPLICATION_LIST_NOT_FOUND,
                    "The application list id does not exist : %s".formatted(dto.getId()));
        }

        //validate the location fields
        super.validate(dto, (payload, updateLocatedList) -> {
            updateLocatedList.setApplicationList(applicationListList.getFirst());
            return createApplicationSupplier.apply(payload, updateLocatedList);
        });

        return null;
    }

    @Override
    Function<PayloadForUpdate<ApplicationListUpdateDto>, String> getCourtLocation() {
        return (dto) -> dto.getData().getCourtLocation() != null ? dto.getData().getCourtLocation().getLocationCode() : null;
    }

    @Override
    Function<PayloadForUpdate<ApplicationListUpdateDto>, String> getCjaCode() {
        return (dto) -> dto.getData().getCriminalJusticeArea() != null ? dto.getData().getCriminalJusticeArea().getCode() : null;
    }

    @Override
    Function<PayloadForUpdate<ApplicationListUpdateDto>, String> getLocationDescription() {
        return (dto) -> dto.getData().getOtherLocationDescription();
    }
}
