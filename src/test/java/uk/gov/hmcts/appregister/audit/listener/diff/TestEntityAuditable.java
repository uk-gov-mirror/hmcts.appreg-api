package uk.gov.hmcts.appregister.audit.listener.diff;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.javers.core.metamodel.annotation.DiffIgnore;
import org.javers.core.metamodel.annotation.ValueObject;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea;
import uk.gov.hmcts.appregister.common.entity.base.Keyable;
import uk.gov.hmcts.appregister.common.enumeration.CrudEnum;

@Getter
@AuditEnabled(types = {CrudEnum.DELETE})
@Table(name = "test_entity")
@ValueObject
class TestEntityAuditable implements Keyable {
    @Id
    @Column(name = "adr_id", nullable = false, updatable = false)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "adr_gen")
    @SequenceGenerator(name = "adr_gen", sequenceName = "adr_id", allocationSize = 1)
    @EqualsAndHashCode.Include
    public Long id;

    @Column(name = "line1")
    @Size(max = 35)
    public CriminalJusticeArea criminalJusticeArea;

    @Column(name = "al_entry_resolution_wording", nullable = false)
    public String resolutionWording;

    @Column(name = "myname", nullable = false)
    @Audit(action = CrudEnum.DELETE)
    public String name;

    @Column(name = "entry", nullable = false)
    @Audit(action = CrudEnum.DELETE)
    @DiffIgnore
    public List<ListEntity> entry = new ArrayList<>();

    @Column(name = "entry2", nullable = false)
    public List<String> entryStrings = new ArrayList<>();
}
