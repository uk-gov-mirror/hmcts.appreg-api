package uk.gov.hmcts.appregister.common.service;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class DateTimeServiceTest {

    private final DateTimeService service = new DateTimeService();

    @Nested
    class NormalizeDateTests {

        @Test
        void normalizeDate_null_returnsNull() {
            assertNull(service.normalizeDate(null));
        }

        @Test
        void normalizeDate_validDate_returnsAtStartOfDay() {
            LocalDate d = LocalDate.of(2025, 10, 7);
            LocalDateTime result = service.normalizeDate(d);
            assertEquals(LocalDateTime.of(2025, 10, 7, 0, 0, 0), result);
        }
    }

    @Nested
    class NormalizeTimeTests {

        @Test
        void normalizeTime_null_returnsNull() {
            assertNull(service.normalizeTime(null));
        }

        @Test
        void normalizeTime_blank_returnsNull() {
            assertNull(service.normalizeTime("   "));
        }

        @Test
        void normalizeTime_HHmm_returnsAnchoredLdt() {
            LocalDateTime result = service.normalizeTime("10:30");
            assertEquals(LocalDateTime.of(1970, 1, 1, 10, 30, 0), result);
        }

        @Test
        void normalizeTime_HHmmss_returnsAnchoredLdt() {
            LocalDateTime result = service.normalizeTime("23:59:58");
            assertEquals(LocalDateTime.of(1970, 1, 1, 23, 59, 58), result);
        }

        @Test
        void normalizeTime_invalidFormat_throwsDateTimeParseException() {
            // HH requires a leading zero; single-digit hour will fail the pattern
            assertThrows(DateTimeParseException.class, () -> service.normalizeTime("8:05"));
            assertThrows(DateTimeParseException.class, () -> service.normalizeTime("25:00"));
            assertThrows(DateTimeParseException.class, () -> service.normalizeTime("10:60"));
            assertThrows(DateTimeParseException.class, () -> service.normalizeTime("10"));
            assertThrows(DateTimeParseException.class, () -> service.normalizeTime("10:30:30:10"));
        }
    }

    @Nested
    class ToTimeStringTests {

        @Test
        void toTimeString_null_returnsNull() {
            assertNull(service.toTimeString(null));
        }

        @Test
        void toTimeString_secondsZero_emitsHHmm() {
            LocalDateTime ldt = LocalDateTime.of(2025, 9, 17, 8, 5, 0);
            assertEquals("08:05", service.toTimeString(ldt));
        }

        @Test
        void toTimeString_secondsPresent_emitsHHmmss() {
            LocalDateTime ldt = LocalDateTime.of(2025, 9, 17, 8, 5, 7);
            assertEquals("08:05:07", service.toTimeString(ldt));
        }
    }
}
