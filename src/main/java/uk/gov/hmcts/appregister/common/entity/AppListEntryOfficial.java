package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

@Entity
@Table(name = "app_list_entry_official")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class AppListEntryOfficial extends BaseChangeableEntity implements Accountable {
    @Id
    @Column(name = "aleo_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = " aleo_gen")
    @SequenceGenerator(name = " aleo_gen", sequenceName = " aleo_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ale_ale_id", nullable = false)
    private ApplicationListEntry ale_ale_id;

    @Column(name = "title")
    private String title;

    @Column(name = "forename")
    private String forename;

    @Column(name = "surname")
    private String surname;

    @Column(name = "official_type", nullable = false)
    private String official_type;

    @Column(name = "user_name", nullable = false)
    private String createdUser;
}
