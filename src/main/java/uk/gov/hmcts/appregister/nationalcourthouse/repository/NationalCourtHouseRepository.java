package uk.gov.hmcts.appregister.nationalcourthouse.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.appregister.nationalcourthouse.model.NationalCourtHouse;

import java.time.LocalDate;
import java.util.Optional;

/**
 * Repository interface for accessing {@link NationalCourtHouse} entities.
 *
 * <p>This interface combines:
 *
 * <ul>
 *   <li>{@link JpaRepository} – provides basic CRUD operations and pagination methods for {@code
 *       CourtLocation} entities using their {@code Long} ID.
 *   <li>{@link JpaSpecificationExecutor} – enables execution of dynamic JPA {@link
 *       org.springframework.data.jpa.domain.Specification} queries, allowing advanced filtering
 *       (e.g. by name, type, date ranges).
 * </ul>
 *
 * <p>Because it extends Spring Data interfaces, no implementation is required: Spring generates a
 * proxy at runtime. You can add custom query methods here if more complex lookups are needed (e.g.
 * {@code findByNameContainingIgnoreCase}).
 *
 * <p>Typical usage:
 *
 * <pre>
 *   List&lt;CourtLocation&gt; results = courtLocationRepository.findAll();
 *   Page&lt;CourtLocation&gt; page = courtLocationRepository.findAll(spec, pageable);
 * </pre>
 */
public interface NationalCourtHouseRepository extends PagingAndSortingRepository<NationalCourtHouse, Long>, JpaSpecificationExecutor<NationalCourtHouse> {

    @Query("""
        select n
          from NationalCourtHouse n
         where (:name is null or lower(n.name) like lower(concat('%', :name, '%')))
           and (:courtType is null or n.courtType = :courtType)
           and (:startFrom is null or n.startDate >= :startFrom)
           and (:startTo   is null or n.startDate <= :startTo)
           and (:endFrom   is null or (n.endDate is null or n.endDate >= :endFrom))
           and (:endTo     is null or n.endDate <= :endTo)
        """)
    Page<NationalCourtHouse> search(
        @Param("name") String name,
        @Param("courtType") String courtType,
        @Param("startFrom") LocalDate startFrom,
        @Param("startTo") LocalDate startTo,
        @Param("endFrom") LocalDate endFrom,
        @Param("endTo") LocalDate endTo,
        Pageable pageable
    );

    Optional<NationalCourtHouse> findById(Long id);
}
