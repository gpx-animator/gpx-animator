/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.util;

import app.gpx_animator.core.data.gpx.GpxPoint;
import org.jetbrains.annotations.Nullable;

public final class PointUtil {

    private PointUtil() throws InstantiationException {
        throw new InstantiationException("Utility classes can't be instantiated!");
    }
    public static double calculateSpeed(@Nullable final GpxPoint lastPoint, final GpxPoint point, final long time) {
        final var timeout = time - 1_000 * 60; // 1 minute // TODO use speedup and fps
        if (lastPoint == null) {
            return 0;
        }
        final var distance = calculateDistance(lastPoint, point);
        final double timeDiff = point.getTime() - lastPoint.getTime();

        final double speed;
        if (distance > 0 && point.getTime() > timeout) {
            speed = timeDiff > 0 ? Math.round((3_600 * distance) / timeDiff) : 0;
        } else {
            speed = 0;
        }

        return speed;
    }

    private static long calculateDistance(final GpxPoint point1, final GpxPoint point2) {
        final var lat1 = point1.getTrackPoint().getLatitude();
        final var lon1 = point1.getTrackPoint().getLongitude();
        final var lat2 = point2.getTrackPoint().getLatitude();
        final var lon2 = point2.getTrackPoint().getLongitude();

        if ((lat1.equals(lat2)) && (lon1.equals(lon2))) {
            return 0;
        } else {
            final var theta = lon1 - lon2;
            final var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            final var arcCosine = Math.acos(dist);
            final var degrees = Math.toDegrees(arcCosine);
            final var mi = degrees * 60 * 1.1515; // to miles
            final var km = mi * 1.609344; // to kilometers
            final var m = km * 1_000; // to meters
            return Math.round(m); // round to full meters
        }
    }
}
