package uk.gov.hmcts.appregister.applicationentry.service;

import java.util.List;
import uk.gov.hmcts.appregister.applicationentry.dto.ApplicationListEntryDto;
import uk.gov.hmcts.appregister.applicationentry.dto.ApplicationWriteDto;

/** Service interface for managing applications within application lists. */
public interface ApplicationListEntryService {
    List<ApplicationListEntryDto> getAllByListId(Long listId, String userId);

    ApplicationListEntryDto getByIdForUser(Long listId, Long appId, String userId);

    ApplicationListEntryDto create(Long listId, ApplicationWriteDto appWriteDto, String userId);

    ApplicationListEntryDto update(
            Long listId, Long appId, ApplicationWriteDto appWriteDto, String userId);

    void delete(Long listId, Long appId, String userId);
}
