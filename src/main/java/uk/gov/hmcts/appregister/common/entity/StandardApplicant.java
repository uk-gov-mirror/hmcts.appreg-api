package uk.gov.hmcts.appregister.common.entity;

import jakarta.persistence.*;

import java.time.LocalDate;

import lombok.*;
import uk.gov.hmcts.appregister.common.entity.base.Accountable;
import uk.gov.hmcts.appregister.common.entity.base.BaseChangeableEntity;

@Entity
@Table(name = "standard_applicants")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode(callSuper = true, onlyExplicitlyIncluded = true)
public class StandardApplicant extends BaseChangeableEntity implements Accountable {
    @Id
    @Column(name = "sa_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sa_gen")
    @SequenceGenerator(name = "sa_gen", sequenceName = "sa_seq", allocationSize = 1)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "standard_applicant_code", nullable = false)
    private String applicantCode;

    @Column(name = "standard_applicant_start_date", nullable = false)
    private LocalDate applicantStartDate;

    @Column(name = "standard_applicant_end_date")
    private LocalDate applicantEndDate;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "user_name", length = 250, nullable = false)
    private String userName;

    @Column(name = "name")
    private String name;

    @Column(name = "title")
    private String applicantTitle;

    @Column(name = "forename_1")
    private String applicantForename1;

    @Column(name = "forename_2")
    private String applicantForename2;

    @Column(name = "forename_3")
    private String applicantForename3;

    @Column(name = "surname")
    private String applicantSurname;

    @Column(name = "address_l1")
    private String addressLine1;

    @Column(name = "address_l2")
    private String addressLine2;

    @Column(name = "address_l3")
    private String addressLine3;

    @Column(name = "address_l4")
    private String addressLine4;

    @Column(name = "address_l5")
    private String addressLine5;

    @Column(name = "postcode")
    private String postcode;

    @Column(name = "email_address")
    private String emailAddress;

    @Column(name = "telephone_number")
    private String telephoneNumber;

    @Column(name = "mobile_number")
    private String mobileNumber;


    @Override
    public String getCreatedUser() {
        return userName;
    }

    @Override
    public void setCreatedUser(String user) {
        this.userName = user;
    }
}
