package uk.gov.hmcts.appregister.resolutioncode.repository;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.data.repository.query.Param;
import uk.gov.hmcts.appregister.resolutioncode.model.ResolutionCode;

/**
 * Repository interface for {@link ResolutionCode} entities.
 *
 * <p>This repository abstracts the persistence layer, leveraging Spring Data JPA to:
 *
 * <ul>
 *   <li>Provide standard CRUD operations and pagination via {@link JpaRepository}.
 *   <li>Enable dynamic filtering queries through {@link JpaSpecificationExecutor}.
 * </ul>
 *
 * <p>It also defines custom finder methods for application-specific queries.
 *
 * <p><strong>Notes:</strong>
 *
 * <ul>
 *   <li>Extending {@code JpaRepository<ResultCode, Long>} indicates that the entity primary key is
 *       a {@link Long}.
 *   <li>Extending {@code JpaSpecificationExecutor<ResultCode>} allows use of JPA Criteria API
 *       specifications for advanced searching and filtering.
 * </ul>
 */
public interface ResolutionCodeRepository
    extends PagingAndSortingRepository<ResolutionCode, Long>,
    JpaSpecificationExecutor<ResolutionCode> {

    /**
     * Finds a {@link ResolutionCode} by its short code value.
     *
     * <p>This is commonly used by controllers/services to retrieve a single result code record
     * given its business identifier (e.g., "RC123").
     *
     * @param code the result code string to search for (must match the {@code resolution_code}
     *             column)
     * @return an {@link Optional} containing the matching {@link ResolutionCode} if found,
     * otherwise empty
     */
    Optional<ResolutionCode> findByResultCode(String code);

    @Query("""
        SELECT r
        FROM ResolutionCode r
        WHERE (:code IS NULL OR LOWER(r.resultCode) LIKE LOWER(CONCAT('%', :code, '%')))
          AND (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')))
          AND (:startFrom IS NULL OR r.startDate >= :startFrom)
          AND (:startTo   IS NULL OR r.startDate <= :startTo)
          AND (:endFrom   IS NULL OR (r.endDate IS NULL OR r.endDate >= :endFrom))
          AND (:endTo     IS NULL OR r.endDate <= :endTo)
        """)
    Page<ResolutionCode> search(
        @Param("code") String code,
        @Param("title") String title,
        @Param("startFrom") LocalDate startFrom,
        @Param("startTo") LocalDate startTo,
        @Param("endFrom") LocalDate endFrom,
        @Param("endTo") LocalDate endTo,
        Pageable pageable
    );
}
