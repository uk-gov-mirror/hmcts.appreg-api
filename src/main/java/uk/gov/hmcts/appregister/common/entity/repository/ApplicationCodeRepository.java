package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;

@Repository
public interface ApplicationCodeRepository extends JpaRepository<ApplicationCode, Long> {
    Optional<ApplicationCode> findByApplicationCode(String applicationCode);

    List<ApplicationCode> findByIdGreaterThanEqual(Integer value);
}
