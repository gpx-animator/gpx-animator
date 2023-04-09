package app.gpx_animator.core.data.entity;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Track {
    private final List<TrackSegment> trackSegments = new ArrayList<>();
    private String name;
    private String comment;
    private TrackType type = TrackType.UNKNOWN;

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

    public @Nullable TrackType getType() {
        return type;
    }

    public void setType(@Nullable final TrackType type) {
        this.type = type;
    }

    public void addTrackSegment(@NotNull final TrackSegment trackSegment) {
        trackSegments.add(trackSegment);
    }

    public @NotNull List<TrackSegment> getTrackSegments() {
        return Collections.unmodifiableList(trackSegments);
    }

    @Override
    public boolean equals(@Nullable final Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Track) obj;
        return Objects.equals(this.name, that.name) &&
                Objects.equals(this.comment, that.comment) &&
                Objects.equals(this.type, that.type) &&
                Objects.equals(this.trackSegments, that.trackSegments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, comment, type, trackSegments);
    }

    @Override
    public @NotNull String toString() {
        return "Track[" +
                "name=" + name + ", " +
                "comment=" + comment + ", " +
                "type=" + type + ", " +
                "trackSegments=" + trackSegments + ']';
    }
}
