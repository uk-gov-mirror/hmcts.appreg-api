package uk.gov.hmcts.appregister.common.mapper;

import org.springframework.stereotype.Component;

/** A mapped that allows us to between Spring and our custom pagination format */
@Component
public class PageMapper {
    /**
     * maps a spring page to our custom page object.
     * @param from The spring page object
     * @param to The custom page object
    */
    public void toPage(org.springframework.data.domain.Page<?> from,
                uk.gov.hmcts.appregister.generated.model.Page to) {
        to.totalPages(from.getTotalPages()).
        elementsOnPage(from.getNumberOfElements()).
        pageNumber(from.getPageable().getPageNumber()).
        pageNumber(from.getPageable().getPageSize());
    }
}
