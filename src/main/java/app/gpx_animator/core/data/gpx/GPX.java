package app.gpx_animator.core.data.gpx;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

public enum GPX {

    TRACK("trk"),
    TYPE("type"),
    TRACK_SEGMENT("trkseg"),
    TRACK_POINT("trkpt"),
    WAY_POINT("wpt"),
    LONGITUDE("lon"),
    LATITUDE("lat"),
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
    public @NotNull String toString() {
        return getName();
    }

    public static @NotNull GPX getElement(@NotNull final String value) {
        return Arrays.stream(GPX.values())
                .filter(gpx -> gpx.getName().equalsIgnoreCase(value))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
