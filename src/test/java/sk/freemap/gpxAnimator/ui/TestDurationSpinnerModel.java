package sk.freemap.gpxAnimator.ui;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

public class TestDurationSpinnerModel {

    private static Stream<Arguments> testDataProvider() {
        return Stream.of(
                Arguments.of(Long.MIN_VALUE),
                Arguments.of(Long.MAX_VALUE),
                Arguments.of(1L),
                Arguments.of(0L),
                Arguments.of(100L),
                Arguments.of((Long) null)
        );
    }

    @ParameterizedTest
    @MethodSource("testDataProvider")
    public void durationSpinnerModelSetGetTest(final Long value) {
        final DurationSpinnerModel testee = new DurationSpinnerModel();
        testee.setValue(value);
        assertEquals(value, testee.getValue());
    }

    @Test
    public void durationSpinnerModelFieldTest() {
        final DurationSpinnerModel testee = new DurationSpinnerModel();
        assertEquals(DurationSpinnerModel.Field.MILLISECOND, DurationSpinnerModel.Field.fromUnit("ms"));

        try {
            testee.getField();
            assert (false);
        } catch (final AssertionError e) {
            // assertion error expected
        }

        testee.setField(DurationSpinnerModel.Field.MILLISECOND);
        assertEquals(DurationSpinnerModel.Field.MILLISECOND, testee.getField());
    }
}
