package uk.gov.hmcts.appregister.data.filter;

import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class AppListEntryFeeIdMixin {
    @JsonIgnore
    public abstract Long getId();
}
