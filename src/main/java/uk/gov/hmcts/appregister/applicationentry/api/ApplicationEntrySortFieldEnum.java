package uk.gov.hmcts.appregister.applicationentry.api;

import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import uk.gov.hmcts.appregister.common.api.SortableOperationEnum;

@Getter
public enum ApplicationEntrySortFieldEnum implements SortableOperationEnum {
    CODE("courtCode", "courtCode", "title"),
    LOCATION("otherLocationDescription", "otherLocationDescription", "title"),
    LEGISLATION("legislation", "legislation", "title"),
    CJA_CODE("cjaCode", "cjaCode", "title"),
    ACCOUNT_REFERENCE("accountReference", "accountReference", "title"),
    STATUS("status", "status", "title"),
    DATE("hearingDate", "date", "title");
    private final String apiValue;
    private final String[] entityValue;

    ApplicationEntrySortFieldEnum(String apiValue, String... entityValue) {
        this.apiValue = apiValue;
        this.entityValue = entityValue;
    }

    private static final Map<String, SortableOperationEnum> MAPPINGS = new HashMap<>();

    static {
        for (SortableOperationEnum status : values()) {
            MAPPINGS.put(status.getApiValue(), status);
        }
    }

    public static SortableOperationEnum getEntityValue(String apiValue) {
        return MAPPINGS.get(apiValue);
    }
}
