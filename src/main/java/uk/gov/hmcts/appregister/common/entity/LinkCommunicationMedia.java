package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "link_communication_media")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class LinkCommunicationMedia extends BaseChangeableEntity {

    @Id
    @Column(name = "lcm_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "lcm_gen")
    @SequenceGenerator(name = "lcm_gen", sequenceName = "lcm_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "lcm_type", nullable = false)
    private String lcm_type;

    @Column(name = "start_date")
    private OffsetDateTime start_date;

    @Column(name = "end_date")
    private OffsetDateTime end_date;

    @Column(name = "version_number")
    private BigDecimal version_number;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "comm_comm_id")
    private CommunicationMedia comm_comm_id;

    @Column(name = "loc_loc_id")
    private Long loc_loc_id;

    @Column(name = "er_er_id")
    private Long er_er_id;

    @Column(name = "bu_bu_id")
    private Long bu_bu_id;
}
