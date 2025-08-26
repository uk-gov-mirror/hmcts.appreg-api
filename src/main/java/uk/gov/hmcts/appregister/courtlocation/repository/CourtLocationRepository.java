package uk.gov.hmcts.appregister.courtlocation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uk.gov.hmcts.appregister.courtlocation.model.CourtLocation;

public interface CourtLocationRepository extends JpaRepository<CourtLocation, Long> {}
