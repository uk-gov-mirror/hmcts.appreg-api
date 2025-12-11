package uk.gov.hmcts.appregister.applicationentryresult.service;

import java.util.UUID;

/**
 * Service interface for managing application list entry results.
 */
public interface ApplicationEntryResultService {
    void delete(UUID listId, UUID entryId, UUID resultId);
}
