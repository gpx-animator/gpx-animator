package sk.freemap.gpxAnimator.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.text.ParseException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class DurationFormatterTest {

    private static Stream<Arguments> generateTestData() {
        return Stream.of(
                Arguments.of(Long.MIN_VALUE,"-106751991167d -7h -12m -55s -808ms"),
                Arguments.of(Long.MAX_VALUE,"106751991167d 7h 12m 55s 807ms"),
                Arguments.of(-1L,"0d 0h 0m 0s -1ms"),
                Arguments.of(0L,"0d 0h 0m 0s 0ms"),
                Arguments.of(100L,"0d 0h 0m 0s 100ms"),
                Arguments.of(-100L,"0d 0h 0m 0s -100ms"),
                Arguments.of(null,""));
    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    void stringToValue(final Long timestamp,final String timeText){
        final DurationFormatter testee = new DurationFormatter();
        try {
            assertEquals(timestamp,testee.stringToValue(timeText));
        }catch(ParseException e){
            assert (false);
        }
    }

    @ParameterizedTest
    @MethodSource("generateTestData")
    void valueToString(final Long timestamp,final String timeText) throws ParseException {
        final DurationFormatter testee = new DurationFormatter();
        assertEquals(timeText, testee.valueToString(timestamp));
    }
}