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

public final class WayPoint implements MyPoint {
    private Double latitude;
    private Double longitude;
    private Long time;
    private String name;
    private String comment;

    @Override
    public @Nullable Double getLatitude() {
        return latitude;
    }

    public void setLatitude(@Nullable final Double latitude) {
        this.latitude = latitude;
    }

    @Override
    public @Nullable Double getLongitude() {
        return longitude;
    }

    public void setLongitude(@Nullable final Double longitude) {
        this.longitude = longitude;
    }

    @Override
    public @Nullable Long getTime() {
        return time;
    }

    public void setTime(@Nullable final Long time) {
        this.time = time;
    }

    public @Nullable String getName() {
        return name;
    }

    public void setName(@Nullable final String name) {
        this.name = name;
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
        var that = (WayPoint) obj;
        return Objects.equals(this.latitude, that.latitude) &&
                Objects.equals(this.longitude, that.longitude) &&
                Objects.equals(this.time, that.time) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(this.comment, that.comment);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitude, longitude, time, name, comment);
    }

    @Override
    public @NotNull String toString() {
        return "WayPoint[" +
                "latitude=" + latitude + ", " +
                "longitude=" + longitude + ", " +
                "time=" + time + ", " +
                "name=" + name + ", " +
                "comment=" + comment + ']';
    }
}
