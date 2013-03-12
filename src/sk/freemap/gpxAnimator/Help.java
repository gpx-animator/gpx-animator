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

	private static final String HELP_SPEEDUP = "speed multiplication of the real time";

	private static final String HELP_MARGIN = "margin in pixels";

	private static final String HELP_TAIL_DURATION = "highlighted tail length in real time milliseconds";

	private static final String HELP_FORCED_POINT_TIME_INTERVAL = "interval between adjanced GPS points in milliseconds - useful for GPX files with missing point time information; if specified";

	private static final String HELP_TIME_OFFSET = "time offset for track in milliseconds";

	public static final String HELP_INPUT = "input GPX filename";
	
	public static final String HELP_OUTPUT = "output filename template for saved frames";
	
	public static final String HELP_LABEL = "text displayed next to marker";

	public static final String HELP_MARKER_SIZE = "marker size in pixels";
	
	public static final String HELP_WAYPOINT_SIZE = "waypoint size in pixels";
	
	public static final String HELP_COLOR = "track color in #RRGGBB representation";
	
	private static final String HELP_LINE_WIDTH = "track line width in pixels";
	

	public static void printHelp(final OptionHelpWriter w) {
		w.writeOptionHelp("help", null, "this help", false, null);
		w.writeOptionHelp("gui", null, "show GUI", false, "if no argument is specified");
		w.writeOptionHelp("input", "input", HELP_INPUT, true, null);
		w.writeOptionHelp("output", "output", HELP_OUTPUT, false, "frame%08d.png");
		w.writeOptionHelp("label", "label", HELP_LABEL, true, "\"\"");
		w.writeOptionHelp("marker-size", "size", HELP_MARKER_SIZE, false, "8.0");
		w.writeOptionHelp("waypoint-size", "size", HELP_WAYPOINT_SIZE + "; for no waypoints specify", false, "6.0");
		w.writeOptionHelp("color", "color", HELP_COLOR, true, "some nice color :-)");
		w.writeOptionHelp("line-width", "width", HELP_LINE_WIDTH, true, "2.0");
		w.writeOptionHelp("time-offset", "milliseconds", HELP_TIME_OFFSET, true, "0");
		w.writeOptionHelp("forced-point-time-interval", "milliseconds", HELP_FORCED_POINT_TIME_INTERVAL + ", absolute time must be set with --time-offset option; can be specified multiple times if multiple tracks are provided; \"\" for no forcing", true, "\"\"");
		w.writeOptionHelp("tail-duration", "time", HELP_TAIL_DURATION, false, "3600000");
		w.writeOptionHelp("margin", "margin", HELP_MARGIN, false, "20");
		w.writeOptionHelp("speedup", "speedup", HELP_SPEEDUP + "; complementary to --total-time option", false, "1000.0");
		w.writeOptionHelp("total-time", "time", "total length of video in milliseconds; complementary to --speedup option", false, null);
		w.writeOptionHelp("fps", "fps", "frames per second", false, "30.0");
		w.writeOptionHelp("width", "width", "video width in pixels; if not specified but --zoom option is specified, then computed from GPX bounding box and margin, otherwise 800", false, "(800)");
		w.writeOptionHelp("height", "height", "video height in pixels; if unspecified, it is derived from width, GPX bounding box and margin", false, null);
		w.writeOptionHelp("zoom", "zoom", "map zoom typically from 1 to 18; if not specified and --tms-url-template option is used then it is computed from --width option", false, null);
		w.writeOptionHelp("tms-url-template", "template", "slippymap (TMS) URL template for background map where {x}, {y} and {zoom} placeholders will be replaced; " +
				"for example use http://tile.openstreetmap.org/{zoom}/{x}/{y}.png for OpenStreetMap", false, null);
		w.writeOptionHelp("background-map-visibility", "visibility", "visibility of the background map in %", false, "50.0");
		w.writeOptionHelp("font-size", "size", "datetime text font size; set to 0 for no date text", false, "12");
		w.writeOptionHelp("keep-idle", null, "don't skip parts where no movement is present", false, null);
		w.writeOptionHelp("flashback-color", "ARGBcolor", "color of the idle-skipping flashback effect in #AARRGGBB representation", false, "opaque white - #ffffffff");
		w.writeOptionHelp("flashback-duration", "duration", "idle-skipping flashback effect duration in milliseconds; set to \"\" for no flashback", false, "250");
		w.writeOptionHelp("debug", null, "toggle debugging", false, null);
	}
	
	public interface OptionHelpWriter {
		void writeOptionHelp(String option, String argument, String description, boolean track, String defaultValue);
	}
	
	public static class PrintWriterOptionHelpWriter implements OptionHelpWriter {
		private final PrintWriter pw;

		public PrintWriterOptionHelpWriter(final PrintWriter pw) {
			this.pw = pw;
		}

		@Override
		public void writeOptionHelp(final String option, final String argument, final String description, final boolean track, final String defaultValue) {
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
