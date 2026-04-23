package uk.gov.hmcts.appregister.filter.applicationlistentry;

import uk.gov.hmcts.appregister.common.entity.AppListEntryResolution;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry;
import uk.gov.hmcts.appregister.common.entity.NameAddress;
import uk.gov.hmcts.appregister.common.entity.ResolutionCode;
import uk.gov.hmcts.appregister.common.enumeration.NameAddressCodeType;
import uk.gov.hmcts.appregister.common.enumeration.YesOrNo;
import uk.gov.hmcts.appregister.data.AppListEntryResolutionTestData;
import uk.gov.hmcts.appregister.data.ApplicationCodeTestData;
import uk.gov.hmcts.appregister.data.NameAddressTestData;
import uk.gov.hmcts.appregister.data.ResolutionCodeTestData;
import uk.gov.hmcts.appregister.filter.FilterFieldData;
import uk.gov.hmcts.appregister.filter.FilterFieldValue;
import uk.gov.hmcts.appregister.filter.generator.FilterFieldDataGenerator;
import uk.gov.hmcts.appregister.filter.generator.PrimitiveDataGenerator;
import uk.gov.hmcts.appregister.filter.meta.FilterFieldDataMetaDescriptor;
import uk.gov.hmcts.appregister.filter.meta.FilterMetaDescriptorEnum;

/**
 * An enumeration that allows us to setup filter for the application list endpoint.
 */
public enum ApplicationListFilterForAppListIdEnum
        implements FilterMetaDescriptorEnum<ApplicationListEntry> {
    SEQUENCE(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("sequenceNumber")
                    .partialSupport(false)
                    .caseInsensitive(false)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        new FilterFieldData<>();
                                filterFieldData.setDescriptor(descriptor);

                                FilterFieldValue<ApplicationListEntry> filterFieldValue =
                                        new FilterFieldValue<>();
                                filterFieldValue.setKeyable(keyable);
                                filterFieldValue.setValue(count);
                                filterFieldValue.setKeyable(keyable);
                                keyable.setSequenceNumber((short) count);

                                filterFieldData.setKeyableValues(filterFieldValue);
                                return filterFieldData;
                            })
                    .build()),
    TITLE(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("applicationTitle")
                    .partialSupport(true)
                    .caseInsensitive(true)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        FilterFieldDataGenerator.getFieldDataWithString(
                                                count, descriptor, keyable, 10);
                                ApplicationCode applicationCodeTestData =
                                        new ApplicationCodeTestData().someComplete();
                                applicationCodeTestData.setTitle(
                                        filterFieldData.getKeyableValues().getValue().toString());
                                keyable.setApplicationCode(applicationCodeTestData);
                                return filterFieldData;
                            })
                    .build()),
    // TODO: We need to address this when we have corrected as part of
    // https://tools.hmcts.net/jira/browse/ARCPOC-1301.
    /*APPLICANT(
        FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
            .queryName("applicationTitle")
            .partialSupport(true)
            .caseInsensitive(true)
            .filterGenerator(
                (count, keyable, descriptor) -> {
                    FilterFieldData<ApplicationListEntry> filterFieldData =
                        FilterFieldDataGenerator.getFieldDataWithString(
                            count, descriptor, keyable, 10);
                    ApplicationCode applicationCodeTestData
                        = new ApplicationCodeTestData().someComplete();
                    applicationCodeTestData.setTitle(filterFieldData.getKeyableValues()
                                                         .getValue().toString());
                    keyable.setApplicationCode(applicationCodeTestData);
                    return filterFieldData;
                })
            .build()),
    RESPONDENT(
        FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
            .queryName("applicationTitle")
            .partialSupport(true)
            .caseInsensitive(true)
            .filterGenerator(
                (count, keyable, descriptor) -> {
                    FilterFieldData<ApplicationListEntry> filterFieldData =
                        FilterFieldDataGenerator.getFieldDataWithString(
                            count, descriptor, keyable, 10);
                    ApplicationCode applicationCodeTestData
                        = new ApplicationCodeTestData().someComplete();
                    applicationCodeTestData.setTitle(filterFieldData.getKeyableValues()
                                                         .getValue().toString());
                    keyable.setApplicationCode(applicationCodeTestData);
                    return filterFieldData;
                })
            .build()),*/
    RESPONDENT_POSTCODE(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("respondentPostcode")
                    .partialSupport(true)
                    .caseInsensitive(true)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        FilterFieldDataGenerator.getFieldDataWithString(
                                                count, descriptor, keyable, 8);

                                NameAddress respondent = new NameAddressTestData().someComplete();
                                respondent.setCode(NameAddressCodeType.RESPONDENT);
                                keyable.setRnameaddress(respondent);
                                respondent.setPostcode(
                                        filterFieldData.getKeyableValues().getValue().toString());
                                return filterFieldData;
                            })
                    .build()),
    ACCOUNT_REFERENCE(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("accountReference")
                    .partialSupport(true)
                    .caseInsensitive(true)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        FilterFieldDataGenerator.getFieldDataWithString(
                                                count, descriptor, keyable, 10);

                                keyable.setAccountNumber(
                                        filterFieldData.getKeyableValues().getValue().toString());
                                return filterFieldData;
                            })
                    .build()),
    RESULTED(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("resulted")
                    .partialSupport(false)
                    .caseInsensitive(false)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        FilterFieldDataGenerator.getFieldDataWithString(
                                                count, descriptor, keyable, 10);

                                ResolutionCode resolutionCode =
                                        new ResolutionCodeTestData().someComplete();
                                resolutionCode.setResultCode(
                                        filterFieldData.getKeyableValues().getValue().toString());

                                AppListEntryResolution resolution =
                                        new AppListEntryResolutionTestData().someComplete();
                                resolution.setResolutionCode(resolutionCode);
                                keyable.setResolutions(new java.util.ArrayList<>());
                                keyable.getResolutions().add(resolution);
                                keyable.setCaseReference(
                                        filterFieldData.getKeyableValues().getValue().toString());
                                return filterFieldData;
                            })
                    .build()),
    FEE(
            FilterFieldDataMetaDescriptor.<ApplicationListEntry>builder()
                    .queryName("feeRequired")
                    .partialSupport(false)
                    .caseInsensitive(false)
                    .filterGenerator(
                            (count, keyable, descriptor) -> {
                                Boolean fee = PrimitiveDataGenerator.getBoolean(count);
                                FilterFieldData<ApplicationListEntry> filterFieldData =
                                        new FilterFieldData<>();
                                FilterFieldValue<ApplicationListEntry> value =
                                        new FilterFieldValue<>();
                                value.setKeyable(keyable);
                                value.setValue(fee);
                                filterFieldData.setKeyableValues(value);
                                filterFieldData.setDescriptor(descriptor);
                                keyable.getApplicationCode()
                                        .setFeeDue(fee ? YesOrNo.YES : YesOrNo.NO);

                                return filterFieldData;
                            })
                    .build());

    private FilterFieldDataMetaDescriptor filterFieldDataDescriptor;

    ApplicationListFilterForAppListIdEnum(FilterFieldDataMetaDescriptor filterFieldDataDescriptor) {
        this.filterFieldDataDescriptor = filterFieldDataDescriptor;
    }

    @Override
    public FilterFieldDataMetaDescriptor getDescriptor() {
        return filterFieldDataDescriptor;
    }
}
