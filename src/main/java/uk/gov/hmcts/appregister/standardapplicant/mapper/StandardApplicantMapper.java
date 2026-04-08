package uk.gov.hmcts.appregister.standardapplicant.mapper;

import java.time.LocalDate;
import lombok.Setter;
import org.mapstruct.InjectionStrategy;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.common.entity.StandardApplicant;
import uk.gov.hmcts.appregister.common.mapper.ApplicantMapper;
import uk.gov.hmcts.appregister.common.projection.StandardApplicantSummaryProjection;
import uk.gov.hmcts.appregister.generated.model.Applicant;
import uk.gov.hmcts.appregister.generated.model.ContactDetails;
import uk.gov.hmcts.appregister.generated.model.FullName;
import uk.gov.hmcts.appregister.generated.model.Organisation;
import uk.gov.hmcts.appregister.generated.model.Person;
import uk.gov.hmcts.appregister.generated.model.StandardApplicantGetDetailDto;
import uk.gov.hmcts.appregister.generated.model.StandardApplicantGetSummaryDto;

/**
 * Mapper for StandardApplicant entity to StandardApplicantDto.
 */
@Component
@Mapper(
        componentModel = "spring",
        injectionStrategy = InjectionStrategy.CONSTRUCTOR,
        nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS,
        nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
@Setter
public abstract class StandardApplicantMapper {

    @Autowired ApplicantMapper applicantMapper;

    @Mapping(target = "code", source = "applicantCode")
    @Mapping(
            target = "applicant",
            expression = "java(mapApplicantFromProjection(projection))")
    @Mapping(target = "startDate", source = "applicantStartDate")
    @Mapping(target = "endDate", source = "applicantEndDate", qualifiedByName = "toEndDate")
    public abstract StandardApplicantGetSummaryDto toReadGetSummaryDto(StandardApplicantSummaryProjection projection);

    @Mapping(target = "code", source = "applicantCode")
    @Mapping(
            target = "applicant",
            expression =
                    "java(applicantMapper.toApplicant(applicantMapper.toApplicantEntity(entity)))")
    @Mapping(target = "startDate", source = "applicantStartDate")
    @Mapping(target = "endDate", source = "applicantEndDate", qualifiedByName = "toEndDate")
    public abstract StandardApplicantGetDetailDto toReadGetDto(StandardApplicant entity);

    @Mapping(target = "id", constant = "0L")
    @Mapping(target = "applicantCode", source = "code")
    @Mapping(target = "name", source = "name")
    @Mapping(target = "applicantStartDate", ignore = true)
    @Mapping(target = "applicantEndDate", ignore = true)
    @Mapping(target = "version", constant = "0L")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "applicantTitle", ignore = true)
    @Mapping(target = "applicantForename1", ignore = true)
    @Mapping(target = "applicantForename2", ignore = true)
    @Mapping(target = "applicantForename3", ignore = true)
    @Mapping(target = "applicantSurname", ignore = true)
    @Mapping(target = "addressLine1", ignore = true)
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "addressLine3", ignore = true)
    @Mapping(target = "addressLine4", ignore = true)
    @Mapping(target = "addressLine5", ignore = true)
    @Mapping(target = "postcode", ignore = true)
    @Mapping(target = "emailAddress", ignore = true)
    @Mapping(target = "telephoneNumber", ignore = true)
    @Mapping(target = "mobileNumber", ignore = true)
    public abstract StandardApplicant toEntity(CodeAndName codeAndName);

    @Mapping(target = "id", constant = "0L")
    @Mapping(target = "applicantCode", source = "code")
    @Mapping(target = "applicantStartDate", source = "date")
    @Mapping(target = "applicantEndDate", ignore = true)
    @Mapping(target = "name", ignore = true)
    @Mapping(target = "version", constant = "0L")
    @Mapping(target = "createdUser", ignore = true)
    @Mapping(target = "applicantTitle", ignore = true)
    @Mapping(target = "applicantForename1", ignore = true)
    @Mapping(target = "applicantForename2", ignore = true)
    @Mapping(target = "applicantForename3", ignore = true)
    @Mapping(target = "applicantSurname", ignore = true)
    @Mapping(target = "addressLine1", ignore = true)
    @Mapping(target = "addressLine2", ignore = true)
    @Mapping(target = "addressLine3", ignore = true)
    @Mapping(target = "addressLine4", ignore = true)
    @Mapping(target = "addressLine5", ignore = true)
    @Mapping(target = "postcode", ignore = true)
    @Mapping(target = "emailAddress", ignore = true)
    @Mapping(target = "telephoneNumber", ignore = true)
    @Mapping(target = "mobileNumber", ignore = true)
    public abstract StandardApplicant toEntity(String code, LocalDate date);

    @Named("toEndDate")
    static JsonNullable<LocalDate> toEndDate(LocalDate date) {
        if (date != null) {
            return JsonNullable.of(date);
        } else {
            return JsonNullable.undefined();
        }
    }

    protected Applicant mapApplicantFromProjection(StandardApplicantSummaryProjection projection) {
        Applicant applicant = new Applicant();

        if (projection.getApplicantName() != null) {
            Organisation organisation = new Organisation();
            organisation.setName(projection.getApplicantName());
            organisation.setContactDetails(mapContactDetailsFromProjection(projection));
            applicant.setOrganisation(organisation);
        } else if (projection.getFirstForename() != null && projection.getSurname() != null) {
            FullName name = getFullName(projection);

            Person person = new Person();
            person.setName(name);
            person.setContactDetails(mapContactDetailsFromProjection(projection));

            applicant.setPerson(person);
        }

        return applicant;
    }

    private static FullName getFullName(StandardApplicantSummaryProjection projection) {
        FullName name = new FullName();
        name.setTitle(projection.getTitle());
        name.setFirstForename(projection.getFirstForename());
        name.setSecondForename(
            projection.getSecondForename() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getSecondForename()));
        name.setThirdForename(
            projection.getThirdForename() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getThirdForename()));
        name.setSurname(projection.getSurname());
        return name;
    }

    protected ContactDetails mapContactDetailsFromProjection(
        StandardApplicantSummaryProjection projection) {

        ContactDetails contactDetails = new ContactDetails();

        contactDetails.setAddressLine1(projection.getAddressLine1());

        contactDetails.setAddressLine2(
            projection.getAddressLine2() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getAddressLine2()));

        contactDetails.setAddressLine3(
            projection.getAddressLine3() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getAddressLine3()));

        contactDetails.setAddressLine4(
            projection.getAddressLine4() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getAddressLine4()));

        contactDetails.setAddressLine5(
            projection.getAddressLine5() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getAddressLine5()));

        contactDetails.setPostcode(projection.getPostcode());

        contactDetails.setPhone(
            projection.getPhone() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getPhone()));

        contactDetails.setMobile(
            projection.getMobile() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getMobile()));

        contactDetails.setEmail(
            projection.getEmail() == null
                ? JsonNullable.undefined()
                : JsonNullable.of(projection.getEmail()));

        return contactDetails;
    }
}
