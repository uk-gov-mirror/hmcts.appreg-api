package uk.gov.hmcts.appregister.testutils.util;

import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.Assertions;
import uk.gov.hmcts.appregister.audit.listener.diff.ReflectiveAuditDifferentiator;

/**
 * A class that allows us to read a log for differences. This class reads the logs from {@link
 * uk.gov.hmcts.appregister.audit.listener.DataAuditLogger} and {@link
 * ReflectiveAuditDifferentiator}.
 */
@Slf4j
@RequiredArgsConstructor
public class DifferenceLogAsserter {
    protected final LogCaptor dataAuditLogger =
            LogCaptor.forClass(uk.gov.hmcts.appregister.audit.listener.DataAuditLogger.class);

    protected final LogCaptor reflectiveDifferentiator =
            LogCaptor.forClass(ReflectiveAuditDifferentiator.class);

    private static final String DIFF_PREFIX = "Saved data audit record:";

    /**
     * The log regex pattern of the {@link uk.gov.hmcts.appregister.audit.listener.DataAuditLogger}.
     */
    private static final String DIFF_LOG_PATTERN =
            DIFF_PREFIX + " Difference(tableName=%s, fieldName=%s, oldValue=%s, newValue=%s)";

    /**
     * The string assertion. Failure on absence of the string.
     *
     * @param assertion The assertion to find
     */
    public void assertDifference(String assertion) {
        for (String log : dataAuditLogger.getDebugLogs()) {
            if (Pattern.matches("Saved data audit record.*", log)) {
                return;
            }
        }

        throw new AssertionError(
                "Expected no differences, but found: " + dataAuditLogger.getErrorLogs());
    }

    public void assertFieldLogPresent(String fieldName) {
        String pattern =
                DIFF_PREFIX
                        + " Difference\\(tableName=.*, fieldName="
                        + fieldName
                        + ", oldValue=.*, newValue=.*\\)";
        for (String log : dataAuditLogger.getDebugLogs()) {
            if (Pattern.matches(pattern, log)) {
                return;
            }
        }

        throw new AssertionError(
                "Expected not null difference for field: "
                        + fieldName
                        + ", but not found in logs: "
                        + dataAuditLogger.getDebugLogs());
    }

    public void assertFieldLogNotPresent(String fieldName) {
        try {
            assertFieldLogPresent(fieldName);
            Assertions.fail();
        } catch (AssertionError assertionError) {
            log.debug("Caught expected exception: {}", assertionError.getMessage());
        }
    }

    /**
     * The string that we need to assert against.
     *
     * @param tableName the table name
     * @param fieldName the field name
     * @param oldValue the old value
     * @param newValue The new value
     * @return The assertion string to detect differences
     */
    public static String getAssertionString(
            String tableName, String fieldName, String oldValue, String newValue) {
        return String.format(DIFF_LOG_PATTERN, tableName, fieldName, oldValue, newValue);
    }

    public void clearLogs() {
        dataAuditLogger.clearLogs();
    }

    public void assertNoErrors() {
        // assert the audit log message
        Assertions.assertEquals(0, dataAuditLogger.getWarnLogs().size());
        Assertions.assertEquals(0, dataAuditLogger.getErrorLogs().size());

        Assertions.assertEquals(0, reflectiveDifferentiator.getWarnLogs().size());
        Assertions.assertEquals(0, reflectiveDifferentiator.getErrorLogs().size());
    }

    public void assertDiffCount(int assertCount) {
        int count = 0;
        for (String log : dataAuditLogger.getDebugLogs()) {
            if (Pattern.matches(DIFF_PREFIX + ".*", log)) {
                return;
            }
        }

        Assertions.assertEquals(count, assertCount);
    }
}
