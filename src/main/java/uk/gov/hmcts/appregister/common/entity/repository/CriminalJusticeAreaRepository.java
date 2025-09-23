package uk.gov.hmcts.appregister.common.entity.repository;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import uk.gov.hmcts.appregister.common.entity.ApplicationCode;
import uk.gov.hmcts.appregister.common.entity.CriminalJusticeArea;

/** Repository interface for managing ApplicationCode entities. */
@Repository
public interface CriminalJusticeAreaRepository extends JpaRepository<CriminalJusticeArea, Long> {
    /**
     * gets possibly many criminal justice areas by code.
     *
     * @param code The code to search for
     * @return one or many criminal justice areas
     */
    List<CriminalJusticeArea> findByCode(String code);

    /**
     * Searches for criminal justice area based on filter criteria.
     *
     * @param code The code to find
     * @param description The title
     */
    @Query(
        """
        SELECT c
        FROM CriminalJusticeArea c
        WHERE (:code IS NULL OR c.code = :code)
        AND (:description IS NULL OR c.description = :description)
        """)
    Page<CriminalJusticeArea> search(
            @Param("code") String code,
            @Param("description") String description,
            Pageable pageable);
}
