package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "petty_sessional_areas")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class PettySessionalArea extends BaseChangeableEntity {
    @Id
    @Column(name = "psa_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "psa_gen")
    @SequenceGenerator(name = "psa_gen", sequenceName = "psa_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "psa_name")
    private String name;

    @Column(name = "short_name")
    private String short_name;

    @Column(name = "version_number", nullable = false)
    private BigDecimal version;

    @Column(name = "cma_cma_id")
    private Long cma_cma_id;

    @Column(name = "psa_code", nullable = false)
    private String psa_code;


    @Column(name = "start_date")
    private OffsetDateTime start_date;

    @Column(name = "end_date")
    private OffsetDateTime end_date;

    @Column(name = "jc_name")
    private String jc_name;

    @Column(name = "court_type", nullable = false)
    private String court_type;

    @Column(name = "crime_cases_loc_id")
    private Long crime_cases_loc_id;

    @Column(name = "fine_accounts_loc_id")
    private Long fine_accounts_loc_id;

    @Column(name = "maintenance_enforcement_loc_id")
    private Long maintenance_enforcement_loc_id;

    @Column(name = "family_cases_loc_id")
    private Long family_cases_loc_id;


    @Column(name = "court_location_code")
    private String court_location_code;

    @Column(name = "central_finance_loc_id")
    private Long central_finance_loc_id;

    @Column(name = "sl_psa_name")
    private String sl_psa_name;

    @Column(name = "norg_id")
    private Long norg_id;

}
