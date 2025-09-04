package uk.gov.hmcts.appregister.common.entity.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;

public interface ApplicationListRepository extends JpaRepository<ApplicationList, Long> {
    List<ApplicationList> findAllByUserName(String userId);

    Optional<ApplicationList> findByIdAndUserName(Long id, String userId);
}
