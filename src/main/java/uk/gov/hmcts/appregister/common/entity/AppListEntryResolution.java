package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;

@Entity
@Table(name = "app_list_entry_resolutions")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AppListEntryResolution extends BaseChangeableEntity {
    @Id
    @Column(name = "aler_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "aler_gen")
    @SequenceGenerator(name = "aler_gen", sequenceName = "aler_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ale_ale_id", nullable = false)
    private ApplicationListEntry ale_ale_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "rc_rc_id", nullable = false)
    private ResolutionCode rc_rc_id;

    @Column(name = "al_entry_resolution_wording", nullable = false)
    private String al_entry_resolution_wording;

    @Column(name = "version", nullable = false)
    private BigDecimal version;

    @Column(name = "user_name", nullable = false)
    private BigDecimal userName;
}
