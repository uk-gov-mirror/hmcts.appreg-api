package uk.gov.hmcts.appregister.applicationentryresult.model;

import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class PayloadGetEntryResultInList {
    private final UUID entryId;
    private final UUID listId;
}
