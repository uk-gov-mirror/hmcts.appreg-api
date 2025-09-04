package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

@Entity
@Table(name = "application_register")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class ApplicationRegister extends BaseChangeableEntity implements Accountable {
    @Id
    @Column(name = "ar_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ar_gen")
    @SequenceGenerator(name = "ar_gen", sequenceName = "ar_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "text", nullable = false, updatable = false)
    private String text;

    @Column(name = "user_name", nullable = false, updatable = false)
    private String userName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "al_al_id")
    private ApplicationList applicationList;

    @Override
    public String getCreatedUser() {
        return userName;
    }

    @Override
    public void setCreatedUser(String user) {
        this.userName = user;
    }
}
