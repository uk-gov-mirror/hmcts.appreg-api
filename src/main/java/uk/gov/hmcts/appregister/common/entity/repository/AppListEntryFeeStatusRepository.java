package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.appregister.common.entity.AppListEntryFeeStatus;
import uk.gov.hmcts.appregister.common.entity.ApplicationListEntry;

public interface AppListEntryFeeStatusRepository
        extends JpaRepository<AppListEntryFeeStatus, Long> {
    /**
     * Finds all ApplicationCode entities with an ID greater than or equal to the specified value.
     *
     * @param value the minimum ID value (inclusive)
     * @return a list of ApplicationCode entities with IDs greater than or equal to the specified
     *     value
     */
    List<AppListEntryFeeStatus> findByIdGreaterThanEqual(Integer value);

    /**
     * Finds a single application list entries by list ID, ensuring it belongs to the specified application list and
     * that the list is owned by the given user.
     *
     * @param listId The ID of the application list the application is expected to belong to
     * @return The application, if found and accessible
     */
    List<AppListEntryFeeStatus> findByAppListEntryId(Long listId);

}
