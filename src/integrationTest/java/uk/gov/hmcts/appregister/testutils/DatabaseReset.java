package uk.gov.hmcts.appregister.testutils;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Query;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationCodeRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseReset {
    private final EntityManagerFactory entityManagerFactory;

    @Autowired
    ApplicationCodeRepository applicationCodeRepository;

    /** A bit crude but sits beyond all of the baseline data sequence numbers so can target any additional data added by the tests */
    private static final int SEQUENCE_START_VALUE = 321364044;

    /** The sequences confrom to the seuqences that reside in the flyway scripts */
    private static final List<String> SEQUENCES_RESET_FROM = List.of(
        "ac_seq",
        "adr_seq",
        "alefs_seq",
        "aleo_seq",
        "aleo_seq",
        "aler_seq",
        "al_seq",
        "ar_seq",
        "cja_seq",
        "fee_seq",
        "la_seq",
        "lcm_seq",
        "na_seq",
        "psa_seq",
        "rc_seq",
        "sa_seq",
        "ale_seq",
        "nch_seq"
    );

    @Transactional
    public void resetDbData() {
        resetSequences();
        applicationCodeRepository.deleteAll(
            applicationCodeRepository.findByIdGreaterThanEqual(SEQUENCE_START_VALUE)
        );
    }

    @Transactional
    public void resetSequences() {
        try (EntityManager em = entityManagerFactory.createEntityManager()) {
            em.getTransaction().begin();
            final Query query = em.createNativeQuery("SELECT sequence_name FROM information_schema.sequences WHERE sequence_schema = 'darts'");
            final List sequences = query.getResultList();
            for (Object seqName : sequences) {
                em.createNativeQuery("ALTER SEQUENCE darts." + seqName + " RESTART WITH " + SEQUENCE_START_VALUE).executeUpdate();

            }
            em.getTransaction().commit();
        }
    }

}
