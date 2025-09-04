package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;

@Entity
@Table(name = "application_list_entries")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ApplicationListEntry extends BaseChangeableEntity implements Accountable {

    @Id
    @Column(name = "ale_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ale_gen")
    @SequenceGenerator(name = "ale_gen", sequenceName = "ale_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "al_al_id")
    private ApplicationList al_al_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sa_sa_id")
    private StandardApplicant sa_sa_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ac_ac_id")
    private ApplicationCode ac_ac_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "a_na_id")
    private NameAddress a_na_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "r_na_id")
    private NameAddress r_na_id;

    @Column(name = "number_of_bulk_respondents")
    private Short number_of_bulk_respondents;

    @Column(name = "application_list_entry_wording", nullable = false)
    private String application_list_entry_wording;

    @Column(name = "case_reference")
    private String case_reference;

    @Column(name = "account_number")
    private String account_number;

    @Column(name = "entry_rescheduled")
    private Boolean entry_rescheduled;

    @Column(name = "notes")
    private Boolean notes;

    @Column(name = "version", nullable = false)
    private BigDecimal version;

    @Column(name = "bulk_upload")
    private String bulk_upload;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "sequence_number", nullable = false)
    private Short sequence_number;

    @Column(name = "tcep_status")
    private Short tcep_status;

    @Column(name = "message_uuid")
    private Short message_uuid;

    @Column(name = "retry_count")
    private Short retry_count;

    @Column(name = "lodgement_date", nullable = false)
    private OffsetDateTime lodgement_date;

    @Override
    public String getCreatedUser() {
        return userName;
    }


    @Override
    public void setCreatedUser(String user) {
        this.userName = user;
    }

    @OneToMany(mappedBy="ale_ale_id")
    private List<AppListEntryResolution> resolutions;

    @OneToMany(mappedBy="ale_ale_id")
    private List<AppListEntryOfficial> officials;

    @OneToMany(mappedBy="alefs_ale_id")
    private List<AppListEntryFeeStatus> entryFeeStatuses;

    @OneToMany(mappedBy="ale_ale_id")
    private List<AppListEntryFeeId> entryFeeIds;
}
