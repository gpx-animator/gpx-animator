package app.gpx_animator;

import org.jetbrains.annotations.NonNls;

import java.util.HashMap;
import java.util.ResourceBundle;

@SuppressWarnings("DuplicateStringLiteralInspection")
public enum Option {

    GUI("gui"),
    INPUT("input"),
    OUTPUT("output"),
    LABEL("label"),
    COLOR("color"),
    MARGIN("margin"),
    TIME_OFFSET("time-offset"),
    TRIM_GPX_START("trim-gpx-start"),
    TRIM_GPX_END("trim-gpx-end"),
    FORCED_POINT_TIME_INTERVAL("forced-point-time-interval"),
    SPEEDUP("speedup"),
    LINE_WIDTH("line-width"),
    TAIL_DURATION("tail-duration"),
    TAIL_COLOR("tail-color"),
    FPS("fps"),
    MARKER_SIZE("marker-size"),
    WAYPOINT_SIZE("waypoint-size"),
    MIN_LAT("min-lat"),
    MAX_LAT("max-lat"),
    MIN_LON("min-lon"),
    MAX_LON("max-lon"),
    WIDTH("width"),
    HEIGHT("height"),
    ZOOM("zoom"),
    FONT("font"),
    TMS_URL_TEMPLATE("tms-url-template"),
    ATTRIBUTION("attribution"),
    BACKGROUND_MAP_VISIBILITY("background-map-visibility"),
    TOTAL_TIME("total-time"),
    KEEP_IDLE("keep-idle"),
    BACKGROUND_COLOR("background-color"),
    FLASHBACK_COLOR("flashback-color"),
    FLASHBACK_DURATION("flashback-duration"),
    KEEP_LAST_FRAME("keep-last-frame"),
    SKIP_IDLE("skip-idle"),
    LOGO("logo"),
    LOGO_POSITION("logo-location"),
    ATTRIBUTION_POSITION("attribution-location"),
    INFORMATION_POSITION("information-location"),
    PHOTO_DIR("photo-dir"),
    PHOTO_TIME("photo-time"),
    PHOTO_ANIMATION_DURATION("photo-animation-duration"),
    HELP("help"),
    TRACK_ICON("track-icon"),
    TRACK_ICON_FILE("track-icon-file"),
    TRACK_ICON_MIRROR("track-icon-mirror"),
    SPEED_UNIT("speed-unit");

    private static final java.util.Map<String, Option> OPTION_MAP = new HashMap<>();

    static {
        for (final Option option : Option.values()) {
            OPTION_MAP.put(option.name, option);
        }
    }

    private final String name;
    private final String help;

    Option(@NonNls final String key) {
        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        this.name = key;
        this.help = resourceBundle.getString("option.help.".concat(key)); //NON-NLS
    }

    public static Option fromName(final String name) {
        return OPTION_MAP.get(name);
    }

    public String getName() {
        return name;
    }

    public String getHelp() {
        return help;
    }

}
