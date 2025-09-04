package uk.gov.hmcts.appregister.common.entity.compositeid;

import java.io.Serializable;

/** A composite primary key id containing both the app list id and fee id. */
public class AppListEntryFeeCompositeId implements Serializable {

  private Long entryId;

  private Long feeId;

  /**
   * Create composite id.
   *
   * @param entryId entryId
   * @param feeId feeId
   */
  public AppListEntryFeeCompositeId(Long entryId, Long feeId) {
    this.entryId = entryId;
    this.feeId = feeId;
  }
}
