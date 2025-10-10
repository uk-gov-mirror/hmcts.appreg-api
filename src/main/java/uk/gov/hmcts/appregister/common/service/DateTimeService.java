package uk.gov.hmcts.appregister.common.service;

import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.mapstruct.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

@Component
public class DateTimeService {

    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("[HH:mm[:ss]]");
    private static final LocalDate ANCHOR_DATE = LocalDate.of(1970, 1, 1);

    /** LocalDate -> LocalDateTime at 00:00:00 for DATE column stored as TIMESTAMP. */
    @Named("normalizeDate")
    public LocalDateTime normalizeDate(LocalDate date) {
        return date == null ? null : date.atStartOfDay();
    }

    /** String "HH:mm[:ss]" -> LocalDateTime anchored to a fixed date for TIME-as-TIMESTAMP. */
    @Named("normalizeTime")
    public LocalDateTime normalizeTime(String time) {
        if (!StringUtils.hasText(time)) return null;
        return ANCHOR_DATE.atTime(LocalTime.parse(time, TIME_FMT));
    }

    /** LocalDateTime -> "HH:mm" (omit seconds when zero) or "HH:mm:ss". */
    @Named("toTimeString")
    public String toTimeString(LocalDateTime ldt) {
        if (ldt == null) return null;
        LocalTime t = ldt.toLocalTime();
        return t.getSecond() == 0
            ? t.truncatedTo(ChronoUnit.MINUTES).toString()
            : t.toString();
    }

    /** Start of day for given LocalDateTime (inclusive bound). */
    public LocalDateTime startOfDay(LocalDateTime ldt) {
        return (ldt == null) ? null : ldt.toLocalDate().atStartOfDay();
    }

    /** Start of next day (exclusive upper bound for date filtering). */
    public LocalDateTime nextDayStart(LocalDateTime ldt) {
        return (ldt == null) ? null : ldt.toLocalDate().plusDays(1).atStartOfDay();
    }

    /** Extract hour from LocalDateTime safely. */
    public Integer hour(LocalDateTime ldt) {
        return (ldt == null) ? null : ldt.getHour();
    }

    /** Extract minute from LocalDateTime safely (truncated to minute). */
    public Integer minute(LocalDateTime ldt) {
        return (ldt == null) ? null : ldt.getMinute();
    }
}
