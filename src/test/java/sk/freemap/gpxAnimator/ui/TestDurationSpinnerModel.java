package sk.freemap.gpxAnimator.ui;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestDurationSpinnerModel {

    private static List<Long> edgeCases = new ArrayList<>();

    @BeforeAll
    public static void setup() {
        edgeCases.add(Long.MIN_VALUE);
        edgeCases.add(Long.MAX_VALUE);
        edgeCases.add(-1L);
        edgeCases.add(0L);
        edgeCases.add(100L);
        edgeCases.add(null);
    }

    @Test
    public void durationSpinnerModelSetGetTest() {

        DurationSpinnerModel d = new DurationSpinnerModel();

        for (Long edgeCase : edgeCases) {
            d.setValue(edgeCase);
            assertEquals(edgeCase, d.getValue());
        }
    }

    @Test
    public void durationSpinnerModelFieldTest() {

        DurationSpinnerModel d = new DurationSpinnerModel();

        assertEquals(DurationSpinnerModel.Field.MILLISECOND, DurationSpinnerModel.Field.fromUnit("ms"));

        try {
            d.getField();
            assert (false);
        } catch (AssertionError e) {
            // assertion error expected
        }

        d.setField(DurationSpinnerModel.Field.MILLISECOND);
        assertEquals(DurationSpinnerModel.Field.MILLISECOND, d.getField());

    }

}
