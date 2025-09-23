package uk.gov.hmcts.appregister.common.mapper;

import org.springframework.stereotype.Component;

/** A mapper that allows us to between Spring and our custom pagination format. */
@Component
public class PageMapper {
    /**
     * maps a spring page to our custom page object.
     *
     * @param from The spring page object
     * @param to The custom page object
     */
    public void toPage(
            org.springframework.data.domain.Page<?> from,
            uk.gov.hmcts.appregister.generated.model.Page to) {
        to.setTotalElements(Long.valueOf(from.getTotalElements()));
        to.setTotalPages(from.getTotalPages());
        to.setPageNumber(from.getPageable().getPageNumber());
        to.setPageSize(from.getPageable().getPageSize());
        to.setFirst(from.isFirst());
        to.setFirst(from.isLast());
        to.setSort(toSort(from.getSort()));
    }

    /**
     * gets the sort object from spring and maps it to our custom sort object.
     *
     * @param from The spring sort object
     * @return The sort object
     */
    private uk.gov.hmcts.appregister.generated.model.Sort toSort(
            org.springframework.data.domain.Sort from) {
        uk.gov.hmcts.appregister.generated.model.Sort to =
                new uk.gov.hmcts.appregister.generated.model.Sort();
        ;
        if (from.isUnsorted()) {
            return null;
        }
        from.stream()
                .forEach(
                        order -> {
                            uk.gov.hmcts.appregister.generated.model.SortOrdersInner orderModel =
                                    new uk.gov.hmcts.appregister.generated.model.SortOrdersInner();
                            orderModel.setDirection(
                                    order.getDirection()
                                                    == org.springframework.data.domain.Sort
                                                            .Direction.ASC
                                            ? uk.gov.hmcts.appregister.generated.model
                                                    .SortOrdersInner.DirectionEnum.ASC
                                            : uk.gov.hmcts.appregister.generated.model
                                                    .SortOrdersInner.DirectionEnum.DESC);
                            orderModel.setProperty(order.getProperty());
                            to.addOrdersItem(orderModel);
                        });
        return to;
    }
}
