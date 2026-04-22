package uk.gov.hmcts.appregister.data.filter.applicationlistentry;

import uk.gov.hmcts.appregister.applicationentry.api.ApplicationEntryByListIdSortFieldEnum;
import uk.gov.hmcts.appregister.applicationlist.api.ApplicationListSortFieldEnum;
import uk.gov.hmcts.appregister.common.entity.*;
import uk.gov.hmcts.appregister.common.enumeration.Status;
import uk.gov.hmcts.appregister.common.enumeration.YesOrNo;
import uk.gov.hmcts.appregister.data.AppListEntryResolutionTestData;
import uk.gov.hmcts.appregister.data.AppListEntryTestData;
import uk.gov.hmcts.appregister.data.ApplicationCodeTestData;
import uk.gov.hmcts.appregister.data.NameAddressTestData;
import uk.gov.hmcts.appregister.data.ResolutionCodeTestData;
import uk.gov.hmcts.appregister.data.StandardApplicantTestData;
import uk.gov.hmcts.appregister.data.filter.FilterFieldData;
import uk.gov.hmcts.appregister.data.filter.FilterFieldValue;
import uk.gov.hmcts.appregister.data.filter.generator.FilterFieldDataGenerator;
import uk.gov.hmcts.appregister.data.filter.generator.PrimitiveDataGenerator;
import uk.gov.hmcts.appregister.data.filter.meta.GenerateAccordingToSort;
import uk.gov.hmcts.appregister.data.filter.meta.SortMetaDataDescriptor;
import uk.gov.hmcts.appregister.data.filter.meta.SortMetaDescriptorEnum;

import java.util.ArrayList;
import java.util.List;

/**
 * An enumeration that allows us to setup sort for the application list endpoint.
 */
public enum ApplicationListSortForAppListIdEnum implements SortMetaDescriptorEnum<ApplicationListEntry> {
    SEQUENCE_NUMBER(
            SortMetaDataDescriptor.<ApplicationListEntry>builder()
                    .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.SEQUENCE_NUMBER)
                    .sortableValueFunction((ale) -> Integer.valueOf(ale.getSequenceNumber().intValue()))
                    .defaultSort(true)
                    .sortGenerator(
                            new GenerateAccordingToSort<ApplicationListEntry>() {
                                @Override
                                public void apply(
                                        int count,
                                        ApplicationListEntry keyable,
                                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                                    keyable.setSequenceNumber((short)count);
                                }
                            })
                    .build()),
    APPLICATION_TITLE(
            SortMetaDataDescriptor.<ApplicationListEntry>builder()
                    .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.APPLICATION_TITLE)
                    .sortableValueFunction((keyable) -> keyable.getApplicationCode().getTitle())
                    .sortGenerator(
                            new GenerateAccordingToSort<ApplicationListEntry>() {
                                @Override
                                public void apply(
                                        int count,
                                        ApplicationListEntry keyable,
                                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                                    ApplicationCode applicationCodeTestData
                                        = new ApplicationCodeTestData().someComplete();
                                    applicationCodeTestData.setTitle((PrimitiveDataGenerator.generate(count, 10)));
                                    applicationCodeTestData.setCode((PrimitiveDataGenerator.generate(count, 10)));
                                    keyable.setApplicationCode(applicationCodeTestData);
                                }
                            })
                    .build()),
    RESPONDENT_POSTCODE(
            SortMetaDataDescriptor.<ApplicationListEntry>builder()
                    .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.RESPONDENT_POSTCODE)
                    .sortableValueFunction(keyable -> keyable.getRnameaddress().getPostcode())
                    .sortGenerator(
                            new GenerateAccordingToSort<ApplicationListEntry>() {
                                @Override
                                public void apply(
                                        int count,
                                        ApplicationListEntry keyable,
                                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                                    NameAddress respondent = new NameAddressTestData().someComplete();
                                    keyable.setRnameaddress(respondent);
                                    respondent.setPostcode(PrimitiveDataGenerator.generate(count, 8));
                                }
                            })
                    .build()),
    ACCOUNT_REFERENCE(
        SortMetaDataDescriptor.<ApplicationListEntry>builder()
            .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.ACCOUNT_REFERENCE)
            .sortableValueFunction(keyable -> keyable.getAccountNumber())
            .sortGenerator(
                new GenerateAccordingToSort<ApplicationListEntry>() {
                    @Override
                    public void apply(
                        int count,
                        ApplicationListEntry keyable,
                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                        keyable.setAccountNumber(PrimitiveDataGenerator.generate(count, 10));
                    }
                })
            .build()),
    RESULTED(
        SortMetaDataDescriptor.<ApplicationListEntry>builder()
            .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.RESULTED)
            .sortableValueFunction(keyable -> keyable.getResolutions().get(0).getResolutionCode().getResultCode())
            .sortGenerator(
                new GenerateAccordingToSort<ApplicationListEntry>() {
                    @Override
                    public void apply(
                        int count,
                        ApplicationListEntry keyable,
                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                        ResolutionCode resolutionCode = new ResolutionCodeTestData().someComplete();
                        resolutionCode.setResultCode(PrimitiveDataGenerator.generate(count, 10));

                        AppListEntryResolution resolution = new AppListEntryResolutionTestData().someComplete();
                        resolution.setResolutionCode(resolutionCode);
                        keyable.setResolutions(new ArrayList<>());
                        keyable.getResolutions().add(resolution);
                    }
                })
            .build()),
    FEE(
        SortMetaDataDescriptor.<ApplicationListEntry>builder()
            .sortableOperationEnum(ApplicationEntryByListIdSortFieldEnum.FEE_REQUIRED)
            .sortableValueFunction(keyable -> keyable.getApplicationCode().getFeeDue().isYes())
            .sortGenerator(
                new GenerateAccordingToSort<ApplicationListEntry>() {
                    @Override
                    public void apply(
                        int count,
                        ApplicationListEntry keyable,
                        SortMetaDataDescriptor<ApplicationListEntry> descriptor) {
                        Boolean fee = PrimitiveDataGenerator.getBoolean(count);
                        keyable.getApplicationCode().setFeeDue(fee ? YesOrNo.YES : YesOrNo.NO);
                    }
                })
            .build());

    private SortMetaDataDescriptor<ApplicationListEntry> sortDataDescriptor;

    ApplicationListSortForAppListIdEnum(SortMetaDataDescriptor<ApplicationListEntry> sortDataDescriptor) {
        this.sortDataDescriptor = sortDataDescriptor;
    }

    @Override
    public SortMetaDataDescriptor<ApplicationListEntry> getDescriptor() {
        return sortDataDescriptor;
    }
}
