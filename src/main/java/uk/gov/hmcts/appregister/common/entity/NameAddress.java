package uk.gov.hmcts.appregister.common.entity;


import jakarta.persistence.*;
import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "name_address")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class NameAddress extends BaseChangeableEntity implements Accountable {
    @Id
    @Column(name = "na_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "na_gen")
    @SequenceGenerator(name = "na_gen", sequenceName = "na_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "code")
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String title;

    @Column(name = "forename_1")
    private String forename_1;

    @Column(name = "forename_2")
    private String forename_2;

    @Column(name = "forename_3")
    private String forename_3;

    @Column(name = "surname")
    private String surname;

    @Column(name = "address_l1")
    private String address_l1;

    @Column(name = "address_l2")
    private String address_l2;

    @Column(name = "address_l3")
    private String address_l3;

    @Column(name = "address_l4")
    private String address_l4;

    @Column(name = "address_l5")
    private String address_l5;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "email_address")
    private String email_address;

    @Column(name = "telephone_number")
    private String telephone_number;

    @Column(name = "mobile_number")
    private String mobile_number;

    @Column(name = "version", nullable = false)
    private BigDecimal version;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "date_of_birth")
    private OffsetDateTime date_of_birth;

    @Column(name = "dms_id")
    private Long dms_id;

    @Override
    public String getCreatedUser() {
        return userName;
    }

    @Override
    public void setCreatedUser(String user) {
        this.userName = user;
    }
}
