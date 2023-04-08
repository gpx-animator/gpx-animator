package app.gpx_animator.core.data.gpx;

import org.jetbrains.annotations.NotNull;

public enum GPX {

    LONGITUDE("lon"),
    LATITUDE("lat"),
    TRACK_SEGMENT("trkseg"),
    TRACK_POINT("trkpt"),
    WAY_POINT("wpt"),
    TIME("time"),
    SPEED("speed"),
    NAME("name"),
    COMMENT("cmt");

    private final String name;

    GPX(@NotNull final String name) {
        this.name = name;
    }

    public @NotNull String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }
}
