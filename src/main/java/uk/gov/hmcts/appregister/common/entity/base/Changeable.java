package uk.gov.hmcts.appregister.common.entity.base;

import jakarta.persistence.EntityListeners;

import java.time.OffsetDateTime;

/**
 * This interface should be implemented by entities that need to track changes.
 */
@EntityListeners(PreCreateUpdateEntityListener.class)
public interface Changeable {
    Long getChangedBy();

    OffsetDateTime getChangedDate();

    void setChangedBy(Long changedBy);

    void setChangedDate(OffsetDateTime changedDate);
}
