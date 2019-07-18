package sk.freemap.gpxAnimator;

import java.awt.*;
import java.util.HashMap;

public enum Option {

	GUI("gui", "show GUI"),
	INPUT("input", "input GPX filename"),
	OUTPUT("output", "filename for generated video or filename template for saved image frames where %06d will be replaced by frame sequence number"),
	LABEL("label", "text displayed next to marker"),
	COLOR("color", "track color in #RRGGBB representation"),
	MARGIN("margin", "margin in pixels"),
	TIME_OFFSET("time-offset", "time offset for track in milliseconds"),
	FORCED_POINT_TIME_INTERVAL("forced-point-time-interval", "interval between adjanced GPS points in milliseconds - useful for GPX files with missing point time information; " +
			"if specified then time offset must be set representing absolute; empty for no forcing"),
	SPEEDUP("speedup", "speed multiplication of the real time; complementary to specifying total time"),
	LINE_WIDTH("line-width", "track line width in pixels"),
	TAIL_DURATION("tail-duration", "highlighted tail length in real time milliseconds"),
	FPS("fps", "frames per second"),
	MARKER_SIZE("marker-size", "marker size in pixels"),
	WAYPOINT_SIZE("waypoint-size", "waypoint size in pixels; for no waypoints specify 0"),
	MIN_LAT("min-lat", "minimal latitude; leave empty if it should be automatically computed from the GPX tracks"),
	MAX_LAT("max-lat", "maximal latitude; leave empty if it should be automatically computed from the GPX tracks"),
	MIN_LON("min-lon", "minimal longitude; leave empty if it should be automatically computed from the GPX tracks"),
	MAX_LON("max-lon", "maximal longitude; leave empty if it should be automatically computed from the GPX tracks"),
	WIDTH("width", "video width in pixels; if not specified but zoom is specified, then computed from GPX bounding box and margin, otherwise 800"),
	HEIGHT("height", "video height in pixels; if unspecified, it is derived from width, GPX bounding box and margin"),
	ZOOM("zoom", "map zoom typically from 1 to 18; if not specified and TMS URL Template (Background Map) is specified then it is computed from width"),
	FONT_SIZE("font-size", "datetime text font size; set to 0 for no date text"),
	TMS_URL_TEMPLATE("tms-url-template", "slippymap (TMS) URL template for background map where {x}, {y} and {zoom} placeholders will be replaced; for example use http://tile.openstreetmap.org/{zoom}/{x}/{y}.png for OpenStreetMap"),
	ATTRIBUTION("attribution", "map attribution text; %MAP_ATTRIBUTION% placeholder is replaced by attribution of selected pre-defined map (only from GUI)"),
	BACKGROUND_MAP_VISIBILITY("background-map-visibility", "opacity of the background map from 0.0 to 1.0"),
	TOTAL_TIME("total-time", "total length of video in milliseconds; complementary to speedup"),
	KEEP_IDLE("keep-idle", "keep parts where no movement is present"),
	FLASHBACK_COLOR("flashback-color", "transition color between non-idle parts"),
	FLASHBACK_DURATION("flashback-duration", "color of the idle-skipping flashback effect in #AARRGGBB representation"),
	KEEP_LAST_FRAME("keep-last-frame", "time to repeat the last rendered frame in milliseconds; complementary to total time"),
	SKIP_IDLE("skip-idle", "idle-skipping flashback effect duration in milliseconds; set to empty for no flashback"),
	PHOTOS("photos", "a directory containing photos to be added to the animation (must contain EXIF information with date and time of photo taken)"),
	PHOTO_TIME("photo-time", "the amount of time, a photo should be shown above the map"),
	HELP("help", "this help"),
	TAIL_COLOR("tail-color", "highlighted tail color");

    private static java.util.Map<String, Option> map = new HashMap<String, Option>();
	
	static {
		for (final Option option : Option.values()) {
			map.put(option.name, option);
		}
	}
	
	public static Option fromName(final String name) {
		return map.get(name);
	}

	private String name;

	private String help;

	private Option(final String name, final String help) {
		this.name = name;
		this.help = help;
	}
	
	public String getName() {
		return name;
	}
	
	public String getHelp() {
		return help;
	}
	
}
