package uk.gov.hmcts.appregister.common.entity.base;

import jakarta.persistence.EntityListeners;

/**
 * This interface is to be implemented by entities that need to track the user who created them.
 */
@EntityListeners(PreCreateUpdateEntityListener.class)
public interface Accountable {
    String getCreatedUser();

    void setCreatedUser(String user);
}
