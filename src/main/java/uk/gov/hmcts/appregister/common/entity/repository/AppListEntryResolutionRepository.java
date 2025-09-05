package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import uk.gov.hmcts.appregister.common.entity.AppListEntryResolution;

public interface AppListEntryResolutionRepository
        extends JpaRepository<AppListEntryResolution, Long> {
    @Query(
            """
    SELECT r FROM AppListEntryResolution r
    WHERE r.id = :resultId
    AND r.applicationList.id = :applicationId
    AND r.applicationList.applicationList.id = :listId
    AND r.applicationList.applicationList.createdUser = :userId
        """)
    Optional<AppListEntryResolution> findByIdWithApplicationAndListAndCreatedUser(
            Long resultId, Long applicationId, Long listId, String userId);
}
