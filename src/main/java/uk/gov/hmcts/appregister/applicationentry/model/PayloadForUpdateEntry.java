package uk.gov.hmcts.appregister.applicationentry.model;

import lombok.Getter;
import uk.gov.hmcts.appregister.common.model.PayloadForUpdate;
import uk.gov.hmcts.appregister.generated.model.EntryUpdateDto;

import java.util.UUID;

/**
 * A payload that represents both the list id as well as the entry id for an update to
 * take place.
 */
@Getter
public class PayloadForUpdateEntry extends PayloadForUpdate<EntryUpdateDto> {
    private final UUID entryId;
    public PayloadForUpdateEntry(EntryUpdateDto data, UUID listId, UUID entryId) {
        super(data, listId);
        this.entryId = entryId;
    }
}
