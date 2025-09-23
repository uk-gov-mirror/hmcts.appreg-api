package uk.gov.hmcts.appregister.common.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A parser class that alllows parsing of pageable parameters from a web request.
 */
@Component
@Getter
@Setter
public class Pageable {
    public Pageable() {}

    @Value("${spring.data.web.pageable.default-page-size}")
    private Integer defaultPageSize;

    @Value("${spring.data.web.pageable.max-page-size}")
    private Integer maxPageSize;

    public org.springframework.data.domain.Pageable from(Integer page, Integer size, List<String> sort, String defaultSortProperty,
                                                         Sort.Direction defaultDirection) {

        if (size != null && size > maxPageSize) {
            size = maxPageSize;
        }
        int p = (page == null || page < 0) ? 0 : page;           // Spring pages are 0-based
        int s = (size == null || size < 1) ? defaultPageSize : size;          // pick your default
        Sort sortSpec;

        if (sort!=null && !sort.isEmpty()) {
            sortSpec = parseSort(sort);
        } else {
            sortSpec = (defaultSortProperty == null || defaultSortProperty.isBlank())
                    ? Sort.unsorted()
                    : Sort.by(defaultDirection == null ? Sort.Direction.ASC : defaultDirection, defaultSortProperty);
        }

        return (sortSpec.isUnsorted())
                ? PageRequest.of(p, s)
                : PageRequest.of(p, s, sortSpec);
    }

    private static Sort parseSort(List<String> sort) {
        if (sort == null || sort.isEmpty()) return Sort.unsorted();

        List<Sort.Order> orders = new ArrayList<>();
        Set<String> seen = new HashSet<>(); // optional: avoid duplicate properties

        for (String raw : sort) {
            if (raw == null) continue;
            String token = raw.trim();
            if (token.isEmpty()) continue;

            Sort.Direction dir = Sort.Direction.ASC;
            String prop = token;

            // "-field" / "+field"
            if (token.startsWith("-") || token.startsWith("+")) {
                dir = token.startsWith("-") ? Sort.Direction.DESC : Sort.Direction.ASC;
                prop = token.substring(1).trim();

                // "field,desc" (Spring standard) or "field:desc"
            } else if (token.contains(",") || token.contains(":")) {
                String[] parts = token.split(token.contains(",") ? "," : ":", 2);
                prop = parts[0].trim();
                if (parts.length > 1) {
                    dir = Sort.Direction.fromOptionalString(parts[1].trim()).orElse(Sort.Direction.ASC);
                }
            }

            if (!prop.isEmpty() && seen.add(prop + "|" + dir.name())) {
                orders.add(new Sort.Order(dir, prop));
            }
        }

        return orders.isEmpty() ? Sort.unsorted() : Sort.by(orders);
    }
}