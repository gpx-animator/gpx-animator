/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package sk.freemap.gpxAnimator;

import java.io.PrintWriter;


public class Help {

	public static final String HELP_FLASHBACK_DURATION = "idle-skipping flashback effect duration in milliseconds; set to empty for no flashback";

	public static final String HELP_FLASHBACK_COLOR = "color of the idle-skipping flashback effect in #AARRGGBB representation";

	public static final String HELP_SKIP_IDLE = "skip parts where no movement is present";

	public static final String HELP_FONT_SIZE = "datetime text font size; set to 0 for no date text";

	public static final String HELP_BG_MAP_VISIBILITY = "visibility of the background map";

	public static final String HELP_TMS_URL_TEMPLATE = "slippymap (TMS) URL template for background map where {x}, {y} and {zoom} placeholders will be replaced; for example use http://tile.openstreetmap.org/{zoom}/{x}/{y}.png for OpenStreetMap";

	public static final String HELP_ATTRIBUTION = "map attribution text; %MAP_ATTRIBUTION% placeholder is replaced by attribution of selected pre-defined map (only from GUI)";

	public static final String HELP_ZOOM = "map zoom typically from 1 to 18; if not specified and TMS URL Template (Background Map) is specified then it is computed from width";

	public static final String HELP_HEIGHT = "video height in pixels; if unspecified, it is derived from width, GPX bounding box and margin";

	public static final String HELP_FPS = "frames per second";

	public static final String HELP_WIDTH = "video width in pixels; if not specified but zoom is specified, then computed from GPX bounding box and margin, otherwise 800";

	public static final String HELP_TOTAL_LENGTH = "total length of video in milliseconds; complementary to speedup";

	public static final String HELP_SPEEDUP = "speed multiplication of the real time; complementary to specifying total time";

	public static final String HELP_MARGIN = "margin in pixels";

	public static final String HELP_TAIL_DURATION = "highlighted tail length in real time milliseconds";

	public static final String HELP_FORCED_POINT_TIME_INTERVAL = "interval between adjanced GPS points in milliseconds - useful for GPX files with missing point time information; " +
			"if specified then time offset must be set representing absolute; empty for no forcing";

	public static final String HELP_TIME_OFFSET = "time offset for track in milliseconds";

	public static final String HELP_INPUT = "input GPX filename";
	
	public static final String HELP_OUTPUT = "filename for generated video or filename template for saved image frames where %06d will be replaced by frame sequence number";
	
	public static final String HELP_LABEL = "text displayed next to marker";

	public static final String HELP_MARKER_SIZE = "marker size in pixels";
	
	public static final String HELP_WAYPOINT_SIZE = "waypoint size in pixels";
	
	public static final String HELP_COLOR = "track color in #RRGGBB representation";
	
	public static final String HELP_LINE_WIDTH = "track line width in pixels";


	public static void printHelp(final OptionHelpWriter w) {
		final Configuration cfg;
		final TrackConfiguration tc;
		try {
			cfg = Configuration.createBuilder().build();
			tc = TrackConfiguration.createBuilder().build();
		} catch (final UserException e) {
			throw new RuntimeException(e); // should never happen
		}
		
		
		w.writeOptionHelp("help", null, "this help", false, null);
		w.writeOptionHelp("gui", null, "show GUI", false, "if no argument is specified");
		w.writeOptionHelp("input", "input", HELP_INPUT, true, tc.getInputGpx());
		w.writeOptionHelp("output", "output", HELP_OUTPUT, false, cfg.getOutput());
		w.writeOptionHelp("label", "label", HELP_LABEL, true, tc.getLabel());
		w.writeOptionHelp("marker-size", "size", HELP_MARKER_SIZE, false, cfg.getMarkerSize());
		w.writeOptionHelp("waypoint-size", "size", HELP_WAYPOINT_SIZE + "; for no waypoints specify 0", false, cfg.getWaypointSize());
		w.writeOptionHelp("color", "color", HELP_COLOR, true, "some nice color :-)");
		w.writeOptionHelp("line-width", "width", HELP_LINE_WIDTH, true, tc.getLineWidth());
		w.writeOptionHelp("time-offset", "milliseconds", HELP_TIME_OFFSET, true, tc.getTimeOffset());
		w.writeOptionHelp("forced-point-time-interval", "milliseconds", HELP_FORCED_POINT_TIME_INTERVAL, true, tc.getForcedPointInterval());
		w.writeOptionHelp("tail-duration", "time", HELP_TAIL_DURATION, false, cfg.getTailDuration());
		w.writeOptionHelp("margin", "margin", HELP_MARGIN, false, cfg.getMargin());
		w.writeOptionHelp("speedup", "speedup", HELP_SPEEDUP, false, cfg.getSpeedup());
		w.writeOptionHelp("total-time", "time", HELP_TOTAL_LENGTH, false, cfg.getTotalTime());
		w.writeOptionHelp("fps", "fps", HELP_FPS, false, cfg.getFps());
		w.writeOptionHelp("width", "width", HELP_WIDTH, false, "(800)"); // cfg.getWidth()
		w.writeOptionHelp("height", "height", HELP_HEIGHT, false, cfg.getHeight());
		w.writeOptionHelp("zoom", "zoom", HELP_ZOOM, false, cfg.getZoom());
		w.writeOptionHelp("tms-url-template", "template", HELP_TMS_URL_TEMPLATE, false, cfg.getTmsUrlTemplate());
		w.writeOptionHelp("attribution", "text", HELP_ATTRIBUTION, false, cfg.getAttribution());
		w.writeOptionHelp("background-map-visibility", "visibility", HELP_BG_MAP_VISIBILITY + " from 0.0 to 1.0", false, cfg.getBackgroundMapVisibility());
		w.writeOptionHelp("font-size", "size", HELP_FONT_SIZE, false, cfg.getFontSize());
		w.writeOptionHelp("skip-idle", null, HELP_SKIP_IDLE, false, cfg.isSkipIdle());
		w.writeOptionHelp("flashback-color", "ARGBcolor", HELP_FLASHBACK_COLOR, false, "opaque white - #ffffffff"); // TODO cfg.getFlashbackColor()
		w.writeOptionHelp("flashback-duration", "duration", HELP_FLASHBACK_DURATION, false, cfg.getFlashbackDuration());
	}
	
	public interface OptionHelpWriter {
		void writeOptionHelp(String option, String argument, String description, boolean track, Object defaultValue);
	}
	
	public static class PrintWriterOptionHelpWriter implements OptionHelpWriter {
		private final PrintWriter pw;

		public PrintWriterOptionHelpWriter(final PrintWriter pw) {
			this.pw = pw;
		}

		@Override
		public void writeOptionHelp(final String option, final String argument, final String description, final boolean track, final Object defaultValue) {
			pw.print("--");
			pw.print(option);
			if (argument != null) {
				pw.print(" <");
				pw.print(argument);
				pw.print(">");
			}
			pw.println();
			pw.print('\t');
			pw.print(description);
			if (track) {
				pw.print("; can be specified multiple times if multiple tracks are provided");
			}
			if (defaultValue != null) {
				pw.print("; default ");
				pw.print(defaultValue);
			}
			pw.println();
		}
	}

}
