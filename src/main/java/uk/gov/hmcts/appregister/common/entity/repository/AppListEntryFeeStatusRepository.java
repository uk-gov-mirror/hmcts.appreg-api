package uk.gov.hmcts.appregister.common.entity.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.appregister.common.entity.AppListEntryFeeStatus;

public interface AppListEntryFeeStatusRepository
        extends JpaRepository<AppListEntryFeeStatus, Long> {}
