package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "link_addresses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LinkAddress extends BaseChangeableEntity {
    @Id
    @Column(name = "la_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "la_gen")
    @SequenceGenerator(name = "la_gen", sequenceName = "la_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "no_fixed_abode", nullable = false)
    private String noFixedAbode;

    @Column(name = "la_type", nullable = false)
    private String laType;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "version_number", nullable = false)
    private BigDecimal versionNumber;


    @Column(name = "head_office_indicator")
    private BigDecimal head_OfficeIndicator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adr_adr_id")
    private Address addresses;

    @Column(name = "bu_bu_id")
    private Long bu_bu_id;

    @Column(name = "er_er_id")
    private Long er_er_id;

    @Column(name = "loc_loc_id")
    private Long loc_loc_id;
}
