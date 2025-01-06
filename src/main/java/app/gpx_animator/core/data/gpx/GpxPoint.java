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
package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.data.entity.TrackPoint;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.io.Serial;
import java.util.Objects;

public final class GpxPoint extends Point2D.Double {

    @Serial
    private static final long serialVersionUID = -1060001559230478467L;

    private final TrackPoint trackPoint;
    private final long time;
    private final java.lang.Double speed;

    public GpxPoint(final double x, final double y, final TrackPoint trackPoint, final long time, @Nullable final java.lang.Double speed) {
        super(x, y);
        this.trackPoint = trackPoint;
        this.time = time;
        this.speed = speed;
    }

    public TrackPoint getTrackPoint() {
        return trackPoint;
    }

    public long getTime() {
        return time;
    }

    public java.lang.Double getSpeed() {
        return speed;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        if (!super.equals(o)) {
            return false;
        }

        GpxPoint gpxPoint = (GpxPoint) o;

        if (time != gpxPoint.time) {
            return false;
        }
        if (!trackPoint.equals(gpxPoint.trackPoint)) {
            return false;
        }
        return Objects.equals(speed, gpxPoint.speed);
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + trackPoint.hashCode();
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (speed != null ? speed.hashCode() : 0);
        return result;
    }

	public void setComment(String cmt) {
		this.trackPoint.setComment(cmt);
		
	}
}
