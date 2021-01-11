package app.gpx_animator.core.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class SpeedUnitTest {

    @Test
    public void testKilometersPerHour() {
        assertEquals(0.1, SpeedUnit.KMH.convertSpeed(0.1), 0.05);
        assertEquals(5.3, SpeedUnit.KMH.convertSpeed(5.3), 0.05);
        assertEquals(9.9, SpeedUnit.KMH.convertSpeed(9.9), 0.05);
        assertEquals(123.4, SpeedUnit.KMH.convertSpeed(123.4), 0.05);
    }

    @Test
    public void testMilesPerHour() {
        assertEquals(0.1, SpeedUnit.MPH.convertSpeed(0.1), 0.05);
        assertEquals(3.3, SpeedUnit.MPH.convertSpeed(5.3), 0.05);
        assertEquals(6.2, SpeedUnit.MPH.convertSpeed(9.9), 0.05);
        assertEquals(76.7, SpeedUnit.MPH.convertSpeed(123.4), 0.05);
    }

    @Test
    public void testMinutesPerKilometer() {
        assertEquals(600, SpeedUnit.MIN_KM.convertSpeed(0.1), 0.05);
        assertEquals(60, SpeedUnit.MIN_KM.convertSpeed(1), 0.05);
        assertEquals(11.3, SpeedUnit.MIN_KM.convertSpeed(5.3), 0.05);
        assertEquals(6.06, SpeedUnit.MIN_KM.convertSpeed(9.9), 0.05);
        assertEquals(0.5, SpeedUnit.MIN_KM.convertSpeed(123.4), 0.05);
    }

    @Test
    public void testMinutesPerMile() {
        assertEquals(965.6, SpeedUnit.MIN_MI.convertSpeed(0.1), 0.05);
        assertEquals(96.6, SpeedUnit.MIN_MI.convertSpeed(1), 0.05);
        assertEquals(18.2, SpeedUnit.MIN_MI.convertSpeed(5.3), 0.05);
        assertEquals(9.75, SpeedUnit.MIN_MI.convertSpeed(9.9), 0.05);
        assertEquals(0.78, SpeedUnit.MIN_MI.convertSpeed(123.4), 0.05);
    }

    @Test
    public void testKnots() {
        assertEquals(0.054, SpeedUnit.KNOTS.convertSpeed(0.1), 0.05);
        assertEquals(2.862, SpeedUnit.KNOTS.convertSpeed(5.3), 0.05);
        assertEquals(5.346, SpeedUnit.KNOTS.convertSpeed(9.9), 0.05);
        assertEquals(66.631, SpeedUnit.KNOTS.convertSpeed(123.4), 0.05);
    }

}
