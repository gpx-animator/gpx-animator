package sk.freemap.gpxAnimator;

import java.util.HashMap;
import java.util.ResourceBundle;

public enum Option {

    GUI("gui"), //NON-NLS
    INPUT("input"), //NON-NLS
    OUTPUT("output"), //NON-NLS
    LABEL("label"), //NON-NLS
    COLOR("color"), //NON-NLS
    MARGIN("margin"), //NON-NLS
    TIME_OFFSET("time-offset"), //NON-NLS
    TRIM_GPX_START("trim-gpx-start"), //NON-NLS
    TRIM_GPX_END("trim-gpx-end"), //NON-NLS
    FORCED_POINT_TIME_INTERVAL("forced-point-time-interval"), //NON-NLS
    SPEEDUP("speedup"), //NON-NLS
    LINE_WIDTH("line-width"), //NON-NLS
    TAIL_DURATION("tail-duration"), //NON-NLS
    TAIL_COLOR("tail-color"), //NON-NLS
    FPS("fps"), //NON-NLS
    MARKER_SIZE("marker-size"), //NON-NLS
    WAYPOINT_SIZE("waypoint-size"), //NON-NLS
    MIN_LAT("min-lat"), //NON-NLS
    MAX_LAT("max-lat"), //NON-NLS
    MIN_LON("min-lon"), //NON-NLS
    MAX_LON("max-lon"), //NON-NLS
    WIDTH("width"), //NON-NLS
    HEIGHT("height"), //NON-NLS
    ZOOM("zoom"), //NON-NLS
    FONT_SIZE("font-size"), //NON-NLS
    TMS_URL_TEMPLATE("tms-url-template"), //NON-NLS
    ATTRIBUTION("attribution"), //NON-NLS
    BACKGROUND_MAP_VISIBILITY("background-map-visibility"), //NON-NLS
    TOTAL_TIME("total-time"), //NON-NLS
    KEEP_IDLE("keep-idle"), //NON-NLS
    BACKGROUND_COLOR("background-color"), //NON-NLS
    FLASHBACK_COLOR("flashback-color"), //NON-NLS
    FLASHBACK_DURATION("flashback-duration"), //NON-NLS
    KEEP_LAST_FRAME("keep-last-frame"), //NON-NLS
    SKIP_IDLE("skip-idle"), //NON-NLS
    LOGO("logo"), //NON-NLS
    PHOTO_DIR("photo-dir"), //NON-NLS
    PHOTO_TIME("photo-time"), //NON-NLS
    HELP("help"), //NON-NLS
    TRACK_ICON("track-icon"); //NON-NLS

    private static final java.util.Map<String, Option> OPTION_MAP = new HashMap<>();

    static {
        for (final Option option : Option.values()) {
            OPTION_MAP.put(option.name, option);
        }
    }

    private String name;
    private String help;

    Option(final String key) {
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
