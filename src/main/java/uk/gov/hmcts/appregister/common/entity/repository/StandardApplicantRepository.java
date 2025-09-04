package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.StandardApplicant;

@Repository
public interface StandardApplicantRepository extends JpaRepository<StandardApplicant, Long> {
    Optional<StandardApplicant> findByApplicantCode(String applicantCode);

    List<ApplicationCode> findByIdGreaterThanEqual(Integer value);
}
