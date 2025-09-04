package uk.gov.hmcts.appregister.common.entity.compositeId;

import java.io.Serializable;

public class AppListEntryFeeCompositeId implements Serializable {

    private Long ale_ale_id;

    private Long fee_fee_id;

    // default constructor

    public AppListEntryFeeCompositeId(Long ale_ale_id, Long fee_fee_id) {
        this.ale_ale_id = ale_ale_id;
        this.fee_fee_id = fee_fee_id;
    }

    // equals() and hashCode()
}
