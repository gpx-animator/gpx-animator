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
package app.gpx_animator.core.data.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public final class TrackPoint implements MyPoint {
    private Double latitude;
    private Double longitude;
    private Long time;
    private Double speed;
    private String comment;

    public @Nullable Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@Nullable final Double latitude) {
        this.latitude = latitude;
    }

    public @Nullable Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@Nullable final Double longitude) {
        this.longitude = longitude;
    }

    public @Nullable Long getTime() {
        return time;
    }

    public void setTime(@Nullable final Long time) {
        this.time = time;
    }

    public @Nullable Double getSpeed() {
        return speed;
    }

    public void setSpeed(@Nullable final Double speed) {
        this.speed = speed;
    }

    public @Nullable String getComment() {
        return comment;
    }

    public void setComment(@Nullable final String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TrackPoint) obj;
        return Objects.equals(this.latitude, that.latitude) &&
                Objects.equals(this.longitude, that.longitude) &&
                Objects.equals(this.time, that.time) &&
                Objects.equals(this.speed, that.speed) &&
                Objects.equals(this.comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, time, speed, comment);
    }

    @Override
    public @NotNull String toString() {
        return "TrackPoint[" +
                "latitude=" + latitude + ", " +
                "longitude=" + longitude + ", " +
                "time=" + time + ", " +
                "speed=" + speed + ", " +
                "comment=" + comment + ']';
    }
}
