package uk.gov.hmcts.appregister.common.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import uk.gov.hmcts.appregister.common.entity.ApplicationList;
import uk.gov.hmcts.appregister.common.entity.repository.ApplicationListRepository;
import uk.gov.hmcts.appregister.testutils.BasePostgresIntegrationTest;

class ApplicationListRepositoryTest extends BasePostgresIntegrationTest {

    @Autowired private ApplicationListRepository repository;

    private ApplicationList buildEntity() {
        return ApplicationList.builder()
                .status("OPEN")
                .description("Smoke test list")
                .courtName("Cardiff Crown Court")
                .courtCode("CCC003")
                .date(LocalDateTime.of(2025, 1, 1, 9, 0))
                .time(LocalDateTime.of(2025, 1, 1, 9, 0))
                .durationHours((short) 1)
                .durationMinutes((short) 30)
                .createdUser("tester")
                .build();
    }

    @Test
    @DisplayName("save + reload yields generated PK, UUID, and version=0")
    void saveAndReload() {
        var saved = repository.saveAndFlush(buildEntity());

        assertThat(saved.getPk()).isNotNull();
        assertThat(saved.getUuid()).isNotNull();
        assertThat(saved.getVersion()).isZero();

        var reloaded = repository.findById(saved.getPk()).orElseThrow();
        assertThat(reloaded.getUuid()).isEqualTo(saved.getUuid());
        assertThat(reloaded.getDescription()).isEqualTo("Smoke test list");
    }
}
