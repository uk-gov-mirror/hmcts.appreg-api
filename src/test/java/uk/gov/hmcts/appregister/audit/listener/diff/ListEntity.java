package uk.gov.hmcts.appregister.audit.listener.diff;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.javers.core.metamodel.annotation.ValueObject;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;

@Getter
@Setter
@Table(name = "random_list")
@ValueObject
class ListEntity implements Keyable {
    @Id
    @Column(name = "lst_adr_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "adr_gen")
    @SequenceGenerator(name = "adr_gen", sequenceName = "adr_id", allocationSize = 1)
    @EqualsAndHashCode.Include
    public Long id;

    @Column(name = "lst_entry", nullable = false)
    @Audit(action = CrudEnum.DELETE)
    public String name;

    // test recursion
    @Column(name = "lst_entity", nullable = false)
    public TestEntityAuditable entity;
}
