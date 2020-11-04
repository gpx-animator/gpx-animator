package app.gpx_animator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class KmHrMinPerKmTest {

    private String calcKmPerHrToMinPerKm(final long speed) {
        double minPerKm = speed;
        double mins = (60 / minPerKm);
        double secs = (mins - (int) mins) * 60;
        double tenS = 10;
        if (minPerKm != 0) {
            if (secs == 0) {
                return String.format("%d km/h | " + (int) mins + ":00 minutes/km", speed); //NON-NLS
            } else if (secs >= tenS) {
                return String.format("%d km/h | " + (int) mins + ":" + (int) secs + " minutes/km", speed); //NON-NLS
            } else {
                return String.format("%d km/h | " + (int) mins + ":0" + (int) secs + " minutes/km", speed); //NON-NLS
            }
        } else {
            return String.format("%d km/h | 0:00 minutes/km", speed); //NON-NLS
        }
    }

    @Test
    void correctlyConverted() {

        // speed is of type double casted long
        assertNotEquals("5.5 km/h | 10:54 minutes/km", calcKmPerHrToMinPerKm((long) 5.5));

        // speed is less than 0 km/hr
        assertEquals("-1 km/h | -60:00 minutes/km", calcKmPerHrToMinPerKm(-1));

        // speed is 0 km/hr
        assertEquals("0 km/h | 0:00 minutes/km", calcKmPerHrToMinPerKm(0));

        // speed is greater than 0 km/hr but less than 10 km/hr
        assertEquals("1 km/h | 60:00 minutes/km", calcKmPerHrToMinPerKm(1));
        assertEquals("7 km/h | 8:34 minutes/km", calcKmPerHrToMinPerKm(7));
        assertEquals("9 km/h | 6:40 minutes/km", calcKmPerHrToMinPerKm(9));

        // speed is greater than 10 km/hr
        assertEquals("10 km/h | 6:00 minutes/km", calcKmPerHrToMinPerKm(10));
        assertEquals("33 km/h | 1:49 minutes/km", calcKmPerHrToMinPerKm(33));
        assertEquals("59 km/h | 1:01 minutes/km", calcKmPerHrToMinPerKm(59));

        // speed is 60 km/hr
        assertEquals("60 km/h | 1:00 minutes/km", calcKmPerHrToMinPerKm(60));

        //speed is greater than 60 km/hr
        assertEquals("100 km/h | 0:36 minutes/km", calcKmPerHrToMinPerKm(100));
        assertEquals("1000 km/h | 0:03 minutes/km", calcKmPerHrToMinPerKm(1000));
        assertEquals("10000 km/h | 0:00 minutes/km", calcKmPerHrToMinPerKm(10000));
    }
}
