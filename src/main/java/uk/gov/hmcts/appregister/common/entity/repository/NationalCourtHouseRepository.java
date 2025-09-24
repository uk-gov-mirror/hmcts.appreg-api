package uk.gov.hmcts.appregister.common.entity.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.appregister.common.entity.NationalCourtHouse;

public interface NationalCourtHouseRepository extends JpaRepository<NationalCourtHouse, Long> {

    @Query(
            """
    SELECT n
    FROM NationalCourtHouse n
    WHERE n.courtLocationCode = :code
      AND (n.startDate <= :date)
      AND n.endDate is null
  """)
    List<NationalCourtHouse> findActiveCourt(
            @Param("code") String code, @Param("date") LocalDate date);

    @Query(
            """
            SELECT n
            FROM NationalCourtHouse n
            WHERE (:courtType IS NULL OR n.courtType = :courtType)
              AND (:name IS NULL OR lower(n.name) LIKE concat('%', lower(cast(:name as string)), '%'))
              AND (:startFrom IS NULL OR n.startDate >= :startFrom)
              AND (:startTo   IS NULL OR n.startDate <= :startTo)
              AND (:endFrom  IS NULL OR n.endDate IS NULL OR n.endDate >= :endFrom)
              AND (:endTo    IS NULL OR n.endDate <= :endTo)
            """)
    Page<NationalCourtHouse> search(
            @Param("name") String name,
            @Param("courtType") String courtType,
            @Param("startFrom") LocalDate startFrom,
            @Param("startTo") LocalDate startTo,
            @Param("endFrom") LocalDate endFrom,
            @Param("endTo") LocalDate endTo,
            Pageable pageable);

    /** Convenience method for single-row lookup. */
    Optional<NationalCourtHouse> findById(Long id);

    List<NationalCourtHouse> findByIdGreaterThanEqual(Integer value);
}
