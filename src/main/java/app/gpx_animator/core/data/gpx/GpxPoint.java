/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.data.LatLon;

import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.io.Serial;

public final class GpxPoint extends Point2D.Double {

    @Serial
    private static final long serialVersionUID = -1060001559230478467L;

    private final LatLon latLon;
    private final long time;
    private final java.lang.Double speed;

    public GpxPoint(final double x, final double y, final LatLon latLon, final long time, @Nullable final java.lang.Double speed) {
        super(x, y);
        this.latLon = latLon;
        this.time = time;
        this.speed = speed;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public long getTime() {
        return time;
    }

    public java.lang.Double getSpeed() {
        return speed;
    }
}
