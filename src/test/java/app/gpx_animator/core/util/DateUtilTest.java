package app.gpx_animator.core.util;

import org.junit.jupiter.api.Test;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DateUtilTest {

    @Test
    void parseZonedDateTime() {
        final var expected = ZonedDateTime.of(2020, 12, 3, 12, 20, 59, 0,
                ZoneId.of("+2"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020:12:03 12:20:59 +02:00"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020:12:03 12:20:59 +0200"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020:12:03 12:20:59 +02"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020-12-03 12:20:59 +02:00"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020-12-03 12:20:59 +0200"));
        assertEquals(expected, DateUtil.parseZonedDateTime("2020-12-03 12:20:59 +02"));
    }

    @Test
    void parseZonedDateTimeException() {
        assertThrows(DateTimeParseException.class, () -> DateUtil.parseZonedDateTime("2020-12-03 12:20:59"));
    }

}
