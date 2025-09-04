package uk.gov.hmcts.appregister.common.entity;


import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.Changeable;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "app_list_entry_fee_status")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AppListEntryFeeStatus implements Changeable, Accountable {
    @Id
    @Column(name = "alefs_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "alefs_gen")
    @SequenceGenerator(name = "alefs_gen", sequenceName = "alefs_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alefs_ale_id", nullable = false)
    private ApplicationListEntry alefs_ale_id;

    @Column(name = "alefs_payment_reference")
    private String alefs_payment_reference;

    @Column(name = "alefs_fee_status")
    private String alefs_fee_status;

    @Column(name = "alefs_fee_status_date", nullable = false)
    private OffsetDateTime alefs_fee_status_date;

    @Column(name = "alefs_version", nullable = false)
    private BigDecimal alefs_version;

    @Column(name = "alefs_changed_by", nullable = false)
    private Long changedBy;

    @Column(name = "fee_changed_date", nullable = false)
    private OffsetDateTime changedDate;

    @Column(name = "alefs_user_name", nullable = false)
    private String createdUser;

    @Column(name = "alefs_status_creation_date", nullable = false)
    private OffsetDateTime alefs_status_creation_date;

}
