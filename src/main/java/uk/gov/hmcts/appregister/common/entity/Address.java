package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "addresses")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class Address extends BaseChangeableEntity {
    @Id
    @Column(name = "adr_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "adr_gen")
    @SequenceGenerator(name = "adr_gen", sequenceName = "adr_id", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "line1")
    private String line1;

    @Column(name = "line2")
    private String line2;


    @Column(name = "line3")
    private String line3;

    @Column(name = "line4")
    private String line4;

    @Column(name = "line5")
    private String line5;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;

    @Column(name = "end_date")
    private OffsetDateTime endDate;

    @Column(name = "version_number")
    private BigDecimal version;

    @Column(name = "mcc_mcc_id")
    private Long cja;
}
