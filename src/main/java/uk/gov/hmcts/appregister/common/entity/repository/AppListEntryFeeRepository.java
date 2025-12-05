package uk.gov.hmcts.appregister.common.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import uk.gov.hmcts.appregister.common.entity.AppListEntryFeeId;
import uk.gov.hmcts.appregister.common.entity.Fee;
import uk.gov.hmcts.appregister.common.entity.compositeid.AppListEntryFeeCompositeId;

import java.util.List;
import java.util.UUID;

public interface AppListEntryFeeRepository extends JpaRepository<AppListEntryFeeId, AppListEntryFeeCompositeId> {
    /**
     * deletes the fee mapping to the entry id
     * @param entryId the entry id
     */
    /**
     * deletes the official.
     *
     * @param entryId The entry id that the officials map to
     */
    @Modifying(clearAutomatically = true)
    @Query(
        """
    DELETE FROM AppListEntryFeeId fee
    WHERE fee.appListEntryId = :entryId
    """)
    void deleteForEntryId(Long entryId);

    @Query(
        """
    SELECT f FROM AppListEntryFeeId entryFee
    LEFT JOIN Fee f ON entryFee.feeId = f.id
    WHERE entryFee.appListEntryId = :id
    """)
    List<Fee> getFeeForEntryId(Long id);

    @Query(
        """
    SELECT fee FROM AppListEntryFeeId fee
    WHERE fee.appListEntryId = :id
    """)
    List<AppListEntryFeeId> getEntryFeesForEntry(Long id);
}
