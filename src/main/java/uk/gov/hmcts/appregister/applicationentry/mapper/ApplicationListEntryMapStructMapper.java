package uk.gov.hmcts.appregister.applicationentry.mapper;

import java.util.List;
import java.util.UUID;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;
import uk.gov.hmcts.appregister.common.enumeration.YesOrNo;
import uk.gov.hmcts.appregister.common.projection.ApplicationListEntrySummaryProjection;
import uk.gov.hmcts.appregister.generated.model.ApplicationListCreateDto;
import uk.gov.hmcts.appregister.generated.model.ApplicationListEntrySummary;
import uk.gov.hmcts.appregister.generated.model.ApplicationListStatus;
import uk.gov.hmcts.appregister.generated.model.EntryGetSummaryDto;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public abstract class ApplicationListEntryMapStructMapper {

    public abstract ApplicationListEntrySummary toSummaryDto(
            ApplicationListEntrySummaryProjection summaryProjection);

    public abstract List<ApplicationListEntrySummary> toSummaryDtoList(
            List<ApplicationListEntrySummaryProjection> summaryProjections);

    /**
     * Utility mapping method to wrap a {@link String} in a {@link JsonNullable}.
     *
     * <p>This allows optional String fields (e.g. {@code accountNumber}) to be properly represented
     * in generated OpenAPI models where {@code null} and "undefined" must be distinguished.
     *
     * @param string the String value
     * @return a JsonNullable wrapper containing the value or null
     */
    public JsonNullable<String> map(String string) {
        return (string != null) ? JsonNullable.of(string) : JsonNullable.of(null);
    }

    @Mapping(target = "id", source = "applicationListEntry.uuid")
    @Mapping(target = "applicantName", expression = "java(toApplicantName(applicationListEntry))")
    @Mapping(target = "respondentName", expression = "java(toRespondentName(applicationListEntry))")
    @Mapping(target = "applicationTitle", source = "applicationListEntry.applicationList.description")
    @Mapping(target = "isFeeRequired", expression = "java(toFee(applicationListEntry.getApplicationCode().getFeeDue()))")
    @Mapping(target = "isResulted", expression = "java(toResulted(applicationListEntry))")
    @Mapping(target = "status", source = "applicationListEntry.applicationList.status")
    abstract EntryGetSummaryDto toEntrySummary(ApplicationListEntry applicationListEntry);

    public String toApplicantName(ApplicationListEntry applicationListEntry) {
        if (applicationListEntry.getAnamedaddress() != null && applicationListEntry.getAnamedaddress().getName() == null) {
            return applicationListEntry.getAnamedaddress().getName();
        } else if (applicationListEntry.getAnamedaddress() != null) {
            return applicationListEntry.getAnamedaddress().getForename1() + " " + applicationListEntry.getAnamedaddress().getSurname();
        }

        return null;
    }

    public String toRespondentName(ApplicationListEntry applicationListEntry) {
        if (applicationListEntry.getRnameaddress() != null && applicationListEntry.getRnameaddress().getName() == null) {
            return applicationListEntry.getRnameaddress().getName();
        } else if (applicationListEntry.getRnameaddress() != null) {
            return applicationListEntry.getRnameaddress().getForename1() + " " + applicationListEntry.getRnameaddress().getSurname();
        }
        return null;
    }

    public boolean toFee(YesOrNo applicationListEntry) {
        return applicationListEntry.isYes();
    }

    public boolean toResulted(ApplicationListEntry applicationListEntry) {
        return !applicationListEntry.getResolutions().isEmpty();
    }
}
