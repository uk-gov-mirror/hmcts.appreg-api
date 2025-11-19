package uk.gov.hmcts.appregister.applicationentry.api;

import lombok.Getter;
import uk.gov.hmcts.appregister.common.api.SortableOperationEnum;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry_;
import uk.gov.hmcts.appregister.common.entity.ApplicationList_;
import uk.gov.hmcts.appregister.common.entity.TableNames;
import uk.gov.hmcts.appregister.common.enumeration.Status;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Getter
public enum ApplicationEntrySortFieldEnum implements SortableOperationEnum {
    CODE("courtCode", "courtCode"),
    LOCATION("otherLocationDescription", "otherLocationDescription"),
    LEGISLATION("legislation", "legislation"),
    CJA_CODE("cjaCode", "cjaCode"),
    ACCOUNT_REFERENCE("accountReference", "accountReference"),
    STATUS("status", "status"),
    DATE("hearingDate", "dateOfAl");
    private final String apiValue;
    private final String entityValue;

    ApplicationEntrySortFieldEnum(String apiValue, String entityValue) {
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
