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

import app.gpx_animator.core.data.entity.TrackPoint;
import app.gpx_animator.core.data.gpx.GpxPoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PointUtilTest {

    @Test
    void calculateSpeedWhenFirstPointIsNull() {
        GpxPoint point = new GpxPoint(0, 0, new TrackPoint(0.0, 0.0, 10L, null, null), 10, null);
        var speed = PointUtil.calculateSpeed(null, point, 20);
        assertEquals(0, speed);
    }

    @Test
    void calculateSpeedWhenTimeIsEquals() {
        GpxPoint firstPoint = new GpxPoint(0, 0, new TrackPoint(10.0, 0.0, 10L, null, null), 10, null);
        GpxPoint secondPoint = new GpxPoint(0, 0, new TrackPoint(10.0, 10.0, 10L, null, null), 10, null);

        var speed = PointUtil.calculateSpeed(firstPoint, secondPoint, 20);
        assertEquals(0, speed);
    }

    @Test
    void calculateSpeedWhenPointsIsNotEquals() {
        GpxPoint firstPoint = new GpxPoint(0, 0, new TrackPoint(55.035677, 82.980398, 10L, null, null), 10, null);
        GpxPoint secondPoint = new GpxPoint(0, 0, new TrackPoint(55.035456, 82.981595, 20L, null, null), 20, null);
        var speed = PointUtil.calculateSpeed(firstPoint, secondPoint, 20);
        assertEquals(28800, speed);
    }
}
