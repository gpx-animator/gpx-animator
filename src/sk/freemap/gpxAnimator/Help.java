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


public class Help {

	private static final String HELP_SPEEDUP = "speed multiplication of the real time";

	private static final String HELP_MARGIN = "margin in pixels";

	private static final String HELP_TAIL_DURATION = "latest time of highlighted tail in seconds";

	private static final String HELP_FORCED_POINT_TIME_INTERVAL = "interval between adjanced GPS points in milliseconds - useful for GPX files with missing point time information; if specified";

	private static final String HELP_TIME_OFFSET = "time offset for track in milliseconds";

	public static final String HELP_INPUT = "input GPX filename";
	
	public static final String HELP_OUTPUT = "output filename template for saved frames";
	
	public static final String HELP_LABEL = "text displayed next to marker";

	public static final String HELP_MARKER_SIZE = "marker size in pixels";
	
	public static final String HELP_WAYPOINT_SIZE = "waypoint size in pixels";
	
	public static final String HELP_COLOR = "track color in #RRGGBB representation";
	
	private static final String HELP_LINE_WIDTH = "track line width in pixels";
	

	static void printHelp() {
		System.out.println("GPX Animator 0.9");
		System.out.println("Copyright 2013 Martin Ždila, Freemap Slovakia");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("--help");
		System.out.println("\tthis help");
		System.out.println("--input <input>");
		System.out.println("\t" + HELP_INPUT + "; can be provided multiple times for multiple tracks");
		System.out.println("--output <output>");
		System.out.println("\t" + HELP_OUTPUT + "; default frame%08d.png");
		System.out.println("--label <label>");
		System.out.println("\t" + HELP_LABEL + "; can be specified multiple times if multiple tracks are provided");
		System.out.println("--marker-size <size>");
		System.out.println("\t" + HELP_MARKER_SIZE + "; default 8.0");
		System.out.println("--waypoint-size <size>");
		System.out.println("\t" + HELP_WAYPOINT_SIZE + "; for no waypoints specify 0.0; default 6.0");
		System.out.println("--color <color>");
		System.out.println("\t" + HELP_COLOR + "; can be specified multiple times if multiple tracks are provided");
		System.out.println("--line-width <width>");
		System.out.println("\t" + HELP_LINE_WIDTH + "; can be specified multiple times if multiple tracks are provided; default 2.0");
		System.out.println("--time-offset <milliseconds>");
		System.out.println("\t" + HELP_TIME_OFFSET + "; can be specified multiple times if multiple tracks are provided");
		System.out.println("--forced-point-time-interval <milliseconds>");
		System.out.println("\t" + HELP_FORCED_POINT_TIME_INTERVAL + ", absolute time must be set with --time-offset option; can be specified multiple times if multiple tracks are provided; 0 for no forcing; default 0");
		System.out.println("--tail-duration <time>");
		System.out.println("\t" + HELP_TAIL_DURATION + "; default 3600");
		System.out.println("--margin <margin>");
		System.out.println("\t" + HELP_MARGIN + "; default 20");
		System.out.println("--speedup <speedup>");
		System.out.println("\t" + HELP_SPEEDUP + "; default 1000.0; complementary to --total-time option");
		System.out.println("--total-time <time>");
		System.out.println("\ttotal length of video in seconds; complementary to --speedup option");
		System.out.println("--fps <fps>");
		System.out.println("\tframes per second; default 30.0");
		System.out.println("--width <width>");
		System.out.println("\tvideo width in pixels; if not specified but --zoom option is specified, then computed from GPX bounding box and margin, otherwise 800");
		System.out.println("--height <height>");
		System.out.println("\tvideo height in pixels; if unspecified, it is derived from width, GPX bounding box and margin");
		System.out.println("--zoom <zoom>");
		System.out.println("\tmap zoom typically from 1 to 18; if not specified and --tms-url-template option is used then it is computed from --width option");
		System.out.println("--tms-url-template <template>");
		System.out.println("\tslippymap (TMS) URL template for background map where {x}, {y} and {zoom} placeholders will be replaced; " +
				"for example use http://tile.openstreetmap.org/{zoom}/{x}/{y}.png for OpenStreetMap");
		System.out.println("--background-map-visibility <visibility>");
		System.out.println("\tvisibility of the background map in %, default 50.0");
		System.out.println("--font-size <size>");
		System.out.println("\tdatetime text font size; default 12; set to 0 for no date text");
		System.out.println("--keep-idle");
		System.out.println("\tdon't skip parts where no movement is present");
		System.out.println("--flashback-color <ARGBcolor>");
		System.out.println("\tcolor of the idle-skipping flashback effect in #AARRGGBB representation; default is opaque white - #ffffffff");
		System.out.println("--flashback-duration <duration>");
		System.out.println("\tidle-skipping flashback effect duration in milliseconds; set to 0.0 for no flashback; default 250.0");
		System.out.println("--debug");
		System.out.println("\ttoggle debugging");
	}

}
