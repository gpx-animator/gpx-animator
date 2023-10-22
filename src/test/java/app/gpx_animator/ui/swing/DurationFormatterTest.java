package app.gpx_animator.ui.swing;

import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DurationFormatterTest {

    @NonNls
    private static final String TEST_DATA_GENERATOR = "generateTestData";

    // used via reflection to generate test data
    private static Stream<Arguments> generateTestData() {
        return Stream.of(
            Arguments.of(Long.MIN_VALUE, "-106751991167d -7h -12m -55s -808ms"), //NON-NLS
            Arguments.of(Long.MAX_VALUE, "106751991167d 7h 12m 55s 807ms"), //NON-NLS
            Arguments.of(-1L, "0d 0h 0m 0s -1ms"), //NON-NLS
            Arguments.of(0L, "0d 0h 0m 0s 0ms"), //NON-NLS
            Arguments.of(100L, "0d 0h 0m 0s 100ms"), //NON-NLS
            Arguments.of(-100L, "0d 0h 0m 0s -100ms"), //NON-NLS
            Arguments.of(null, ""));
    }

    @ParameterizedTest
    @MethodSource(TEST_DATA_GENERATOR) //NON-NLS
    void stringToValue(final Long timestamp, final String timeText) {
        final var testee = new DurationFormatter();
        try {
            assertEquals(timestamp, testee.stringToValue(timeText));
        } catch (ParseException e) {
            assert (false);
        }
    }

    @ParameterizedTest
    @MethodSource(TEST_DATA_GENERATOR) //NON-NLS
    void valueToString(final Long timestamp, final String timeText) {
        final var testee = new DurationFormatter();
        assertEquals(timeText, testee.valueToString(timestamp));
    }
}
