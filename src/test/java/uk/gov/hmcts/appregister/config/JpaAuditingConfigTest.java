package uk.gov.hmcts.appregister.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.AuditorAware;
import uk.gov.hmcts.appregister.common.security.UserProvider;

public class JpaAuditingConfigTest {
    @Test
    void shouldReturnAuditorAwareWithUserId() {
        // Arrange
        UserProvider mockUserProvider = mock(UserProvider.class);
        when(mockUserProvider.getUserId()).thenReturn("tenant123:user456");

        JpaAuditingConfig config = new JpaAuditingConfig();

        AuditorAware<String> auditorAware = config.auditorAware(mockUserProvider);
        Optional<String> auditor = auditorAware.getCurrentAuditor();

        assertEquals(Optional.of("tenant123:user456"), auditor);
        verify(mockUserProvider, times(1)).getUserId();
    }
}
