package uk.gov.hmcts.appregister.applicationfee.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for {@link FeePair}.
 *
 * This class provides **minimal test coverage** to satisfy the SonarQube quality gate,
 * since {@code FeePair} is a Java record with no additional logic.
 *
 * Why this exists:
 * - Sonar considers FeePair "new code" → requires test coverage >= 80%.
 *
 * Notes for future maintainers:
 * - These tests are intentionally lightweight; they are not business-critical.
 * - When FeePair gains behaviour or validation logic, expand these tests accordingly.
 * - If FeePair is temporary (proof-of-concept), these tests can be removed along with the record.
 */
public class FeePairTest {
    @Test
    void constructorAndAccessors_workWithNulls() {
        FeePair pair = new FeePair(null, null);

        assertNull(pair.mainFee());
        assertNull(pair.offsetFee());
    }
}
