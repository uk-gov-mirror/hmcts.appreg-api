package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

@Entity
@Table(name = "application_lists")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ApplicationList extends BaseChangeableEntity implements Accountable {
    @Id
    @Column(name = "al_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "al_gen")
    @SequenceGenerator(name = "al_gen", sequenceName = "al_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "application_list_status")
    private String status;

    @Column(name = "application_list_date", nullable = false)
    private OffsetDateTime date;

    @Column(name = "application_list_time", nullable = false)
    private OffsetDateTime time;

    @Column(name = "courthouse_code")
    private String courthouse_code;

    @Column(name = "other_courthouse")
    private String description;

    @Column(name = "list_description", nullable = false)
    private LocalDate listDescription;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "courthouse_name")
    private String courthouseName;

    @Column(name = "version")
    private BigDecimal version;

    @Column(name = "duration_hour")
    private short duration_hour;

    @Column(name = "duration_minute")
    private short durationMinute;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cja_cja_id")
    private CriminalJusticeArea cja;

    @OneToMany(mappedBy="al_al_id")
    private List<ApplicationListEntry> entries;

    @Override
    public String getCreatedUser() {
        return userName;
    }

    @Override
    public void setCreatedUser(String user) {
        this.userName = user;
    }

    @OneToMany(mappedBy="applicationList")
    private List<ApplicationRegister> registers;
}
