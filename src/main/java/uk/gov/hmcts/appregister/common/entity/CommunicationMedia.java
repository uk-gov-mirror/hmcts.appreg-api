package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "communication_media")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class CommunicationMedia extends BaseChangeableEntity {
    @Id
    @Column(name = "comm_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "comm_gen")
    @SequenceGenerator(name = "comm_gen", sequenceName = "comm_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "detail", nullable = false)
    private String detail;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime start_date;

    @Column(name = "end_date")
    private OffsetDateTime end_date;

    @Column(name = "version_number", nullable = false)
    private BigDecimal version_number;

}
