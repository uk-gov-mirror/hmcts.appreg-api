package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;
import uk.gov.hmcts.appregister.common.entity.compositeId.AppListEntryFeeCompositeId;

import java.math.BigDecimal;

@Entity
@Table(name = "app_list_entry_fee_id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@IdClass(AppListEntryFeeCompositeId.class)
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AppListEntryFeeId extends BaseChangeableEntity {
    @Id
    @Column(name = "ale_ale_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long ale_ale_id;

    @Id
    @Column(name = "fee_fee_id", nullable = false)
    @EqualsAndHashCode.Include
    private Long fee_fee_id;

    @ManyToOne()
    @JoinColumn(name = "ale_ale_id", nullable = false)
    private ApplicationListEntry entry;

    @ManyToOne()
    @JoinColumn(name = "fee_fee_id", nullable = false)
    private Fee fee;

    @Column(name = "version", nullable = false)
    private BigDecimal version;

    @Column(name = "user_name", nullable = false)
    private String user_name;
}
