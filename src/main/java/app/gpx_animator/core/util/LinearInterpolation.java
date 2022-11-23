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

import app.gpx_animator.core.data.LatLon;
import app.gpx_animator.core.data.gpx.GpxPoint;
import java.awt.geom.Point2D;
import java.util.TreeMap;

public final class LinearInterpolation {
    private final TreeMap<Long, Point2D> map;
    public LinearInterpolation(final TreeMap<Long, Point2D> map) {
        this.map = new TreeMap<Long, Point2D>(map);
    }

    public Point2D getPointAtTime(final long time) {
        var point = this.map.get(time);
        if (point == null) {
            var time1 = this.map.floorKey(time);
            var time2 = this.map.ceilingKey(time);
            if (time1 == null || time2 == null) {
                return null;
            }
            var point1 = this.map.get(time1);
            var point2 = this.map.get(time2);
            if (point1 instanceof GpxPoint gpxPoint1 && point2 instanceof GpxPoint gpxPoint2) {
                var x = interpolate(time1, gpxPoint1.getX(), time2, gpxPoint2.getX(), time);
                var y = interpolate(time1, gpxPoint1.getY(), time2, gpxPoint2.getY(), time);
                var lat = interpolate(time1, gpxPoint1.getLatLon().getLat(), time2, gpxPoint2.getLatLon().getLat(), time);
                var lon = interpolate(time1, gpxPoint1.getLatLon().getLon(), time2, gpxPoint2.getLatLon().getLon(), time);
                point = new GpxPoint(x, y, new LatLon(lat, lon, time, null, null), time, null);
            }
        }
        return point;
    }


    private double interpolate(final double time1, final double value1, final double time2, final double value2, final double time) {
        return value1 + (value2 - value1) * (time - time1) / (time2 - time1);
    }
}
