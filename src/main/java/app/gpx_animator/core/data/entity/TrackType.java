package app.gpx_animator.core.data.entity;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public enum TrackType {

    CYCLING,
    UNKNOWN;

    private static final Logger LOGGER = LoggerFactory.getLogger(TrackType.class);

    public static @NotNull TrackType getTrackType(@NotNull final String value) {
        try {
            return valueOf(value.toUpperCase(Locale.getDefault()));
        } catch (final IllegalArgumentException e) {
            LOGGER.warn(e.getMessage());
            return UNKNOWN;
        }
    }

}
