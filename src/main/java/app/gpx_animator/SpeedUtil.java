package app.gpx_animator;

import java.awt.geom.Point2D;
import java.util.HashMap;

public final class SpeedUtil {

    private SpeedUtil() throws InstantiationException {
        throw new InstantiationException("SpeedUtils a utility class and can't be instantiated!");
    }


    private static final java.util.Map<Integer, Long> SPEED_VALUES = new HashMap<>();

    private static GpxPoint lastSpeedPoint = null;


    public static String getSpeedString(final Point2D point, final long time, final int frame, final double fps, final SpeedUnit speedUnit) {
        if (point instanceof GpxPoint) {
            final GpxPoint gpxPoint = (GpxPoint) point;
            final double speed = calculateSpeedForDisplay(gpxPoint, time, frame, fps, speedUnit);
            final String format = speed > 10 ? "%.0f %s" : "%.1f %s"; // with 1 decimal below 10, no decimals 10 and above
            return format.formatted(speed, speedUnit);
        } else {
            return "";
        }
    }


    private static double calculateSpeedForDisplay(final GpxPoint point, final long time, final int frame, final double fps,
                                                   final SpeedUnit speedUnit) {
        final long speed = calculateSpeed(point, time);
        SPEED_VALUES.put(frame, speed);

        final long deleteBefore = frame - (Math.round(fps)); // 1 second
        SPEED_VALUES.keySet().removeIf((f) -> f < deleteBefore);

        return speedUnit.convertSpeed(Math.round(SPEED_VALUES.values().stream().mapToLong(Long::longValue).average().orElse(0)));
    }


    private static long calculateSpeed(final GpxPoint point, final long time) {
        final long timeout = time - 1_000 * 60; // 1 minute
        final long distance = calculateDistance(lastSpeedPoint, point);
        final double timeDiff = lastSpeedPoint == null ? 0 : point.getTime() - lastSpeedPoint.getTime();

        final long speed;
        if (distance > 0 && point.getTime() > timeout) {
            speed = Math.round((3_600 * distance) / timeDiff);
        } else {
            speed = 0;
        }

        lastSpeedPoint = point;
        return speed;
    }


    private static long calculateDistance(final GpxPoint point1, final GpxPoint point2) {
        if (point1 == null) {
            return 0;
        }

        final double lat1 = point1.getLatLon().getLat();
        final double lon1 = point1.getLatLon().getLon();
        final double lat2 = point2.getLatLon().getLat();
        final double lon2 = point2.getLatLon().getLon();

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            final double theta = lon1 - lon2;
            final double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            final double arcCosine = Math.acos(dist);
            final double degrees = Math.toDegrees(arcCosine);
            final double mi = degrees * 60 * 1.1515; // to miles
            final double km = mi * 1.609344; // to kilometers
            final double m = km * 1_000; // to meters
            return Math.round(m); // round to full meters
        }
    }

}
