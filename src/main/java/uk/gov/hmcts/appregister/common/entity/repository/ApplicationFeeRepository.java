package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.Fee;

public interface ApplicationFeeRepository extends JpaRepository<Fee, Long> {
    List<Fee> findByReference(String reference);

    //TODO:No offset in schema
    //List<Fee> findByReferenceAndIsOffset(String reference, boolean isOffset);

    List<ApplicationCode> findByIdGreaterThanEqual(Integer value);
}
