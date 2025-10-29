package uk.gov.hmcts.appregister.audit.listener.diff;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
@ToString
public class Difference {
    private final String tableName;
    private final String fieldName;
    private final String oldValue;
    private final String newValue;
}
