package uk.gov.hmcts.appregister.common.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedBy;

import java.time.OffsetDateTime;


/**
 * Define a standard class for the changed columns
 */
@MappedSuperclass
@Getter
@Setter
@EntityListeners(PreCreateUpdateEntityListener.class)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class BaseChangeableEntity implements Changeable {
    @Column(name = "changed_by", nullable = false)
    private Long changedBy;

    @CreatedBy
    @UpdateTimestamp
    @Column(name = "changed_date", nullable = false)
    private OffsetDateTime changedDate;
}
