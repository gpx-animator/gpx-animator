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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class TrackSegment {
    private final List<TrackPoint> trackPoints = new ArrayList<>();

    public void addTrackPoint(@NotNull final TrackPoint trackPoint) {
        trackPoints.add(trackPoint);
    }

    public @NotNull List<TrackPoint> getTrackPoints() {
        return Collections.unmodifiableList(trackPoints);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (TrackSegment) obj;
        return Objects.equals(this.trackPoints, that.trackPoints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackPoints);
    }

    @Override
    public @NotNull String toString() {
        return "TrackSegment[" +
                "trackPoints=" + trackPoints + ']';
    }

}
