package uk.gov.hmcts.appregister.filter.standardapplicant;

import uk.gov.hmcts.appregister.common.entity.StandardApplicant;
import uk.gov.hmcts.appregister.filter.generator.PrimitiveDataGenerator;
import uk.gov.hmcts.appregister.filter.meta.GenerateAccordingToSort;
import uk.gov.hmcts.appregister.filter.meta.SortMetaDataDescriptor;
import uk.gov.hmcts.appregister.filter.meta.SortMetaDescriptorEnum;
import uk.gov.hmcts.appregister.standardapplicant.api.StandardApplicantSortFieldEnum;

/**
 * An enumeration that allows us to setup sort for the standard applicant.
 */
public enum StandardApplicantSortEnum implements SortMetaDescriptorEnum<StandardApplicant> {
    CODE(
            SortMetaDataDescriptor.<StandardApplicant>builder()
                    .sortableOperationEnum(StandardApplicantSortFieldEnum.CODE)
                    .sortableValueFunction(StandardApplicant::getApplicantCode)
                    .defaultSort(true)
                    .sortGenerator(
                            new GenerateAccordingToSort<StandardApplicant>() {
                                @Override
                                public void apply(
                                        int count,
                                        StandardApplicant keyable,
                                        SortMetaDataDescriptor<StandardApplicant> descriptor) {
                                    keyable.setApplicantCode(
                                            PrimitiveDataGenerator.generate(count, 10));
                                }
                            })
                    .build()),
    ADDRESS_LINE_1(
            SortMetaDataDescriptor.<StandardApplicant>builder()
                    .sortableOperationEnum(StandardApplicantSortFieldEnum.ADDRESS_LINE_1)
                    .sortableValueFunction(StandardApplicant::getAddressLine1)
                    .sortGenerator(
                            new GenerateAccordingToSort<StandardApplicant>() {
                                @Override
                                public void apply(
                                        int count,
                                        StandardApplicant keyable,
                                        SortMetaDataDescriptor<StandardApplicant> descriptor) {
                                    keyable.setAddressLine1(
                                            PrimitiveDataGenerator.generate(count, 35));
                                }
                            })
                    .build()),
    FROM(
            SortMetaDataDescriptor.<StandardApplicant>builder()
                    .sortableOperationEnum(StandardApplicantSortFieldEnum.FROM)
                    .sortableValueFunction(StandardApplicant::getApplicantStartDate)
                    .sortGenerator(
                            new GenerateAccordingToSort<StandardApplicant>() {
                                @Override
                                public void apply(
                                        int count,
                                        StandardApplicant keyable,
                                        SortMetaDataDescriptor<StandardApplicant> descriptor) {
                                    keyable.setApplicantStartDate(
                                            PrimitiveDataGenerator.getDateBefore(count));
                                }
                            })
                    .build()),
    TO(
            SortMetaDataDescriptor.<StandardApplicant>builder()
                    .sortableOperationEnum(StandardApplicantSortFieldEnum.TO)
                    .sortableValueFunction(StandardApplicant::getApplicantEndDate)
                    .sortGenerator(
                            new GenerateAccordingToSort<StandardApplicant>() {
                                @Override
                                public void apply(
                                        int count,
                                        StandardApplicant keyable,
                                        SortMetaDataDescriptor<StandardApplicant> descriptor) {
                                    keyable.setApplicantEndDate(
                                            PrimitiveDataGenerator.getDate(count));
                                }
                            })
                    .build());

    private SortMetaDataDescriptor<StandardApplicant> sortDataDescriptor;

    StandardApplicantSortEnum(SortMetaDataDescriptor<StandardApplicant> sortDataDescriptor) {
        this.sortDataDescriptor = sortDataDescriptor;
    }

    @Override
    public SortMetaDataDescriptor<StandardApplicant> getDescriptor() {
        return sortDataDescriptor;
    }
}
