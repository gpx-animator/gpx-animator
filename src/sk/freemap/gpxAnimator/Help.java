package sk.freemap.gpxAnimator;

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
class Help {

	static void printHelp() {
		System.out.println("GPX Animator 0.8");
		System.out.println("Copyright 2013 Martin Ždila, Freemap Slovakia");
		System.out.println();
		System.out.println("Usage:");
		System.out.println("--help");
		System.out.println("\tthis help");
		System.out.println("--input <input>");
		System.out.println("\tinput GPX filename; can be provided multiple times for multiple tracks");
		System.out.println("--output <output>");
		System.out.println("\toutput filename template for saved frames; default frame%08d.png");
		System.out.println("--label <label>");
		System.out.println("\ttext displayed next to marker; can be specified multiple times if multiple tracks are provided");
		System.out.println("--marker-size <size>");
		System.out.println("\tmarker size in pixels; default 8.0");
		System.out.println("--waypoint-size <size>");
		System.out.println("\twaypoint size in pixels; for no waypoints specify 0.0; default 6.0");
		System.out.println("--color <color>");
		System.out.println("\ttrack color in #RRGGBB representation; can be specified multiple times if multiple tracks are provided");
		System.out.println("--line-width <width>");
		System.out.println("\ttrack line width in pixels; can be specified multiple times if multiple tracks are provided; default 2.0");
		System.out.println("--time-offset <milliseconds>");
		System.out.println("\ttime offset for track in milliseconds; can be specified multiple times if multiple tracks are provided");
		System.out.println("--forced-point-time-interval <milliseconds>");
		System.out.println("\tinterval between adjanced GPS points in milliseconds - useful for GPX files with missing point time information; if specified, " +
				"absolute time must be set with --time-offset option; can be specified multiple times if multiple tracks are provided; 0 for no forcing; default 0");
		System.out.println("--tail-duration <time>");
		System.out.println("\tlatest time of highlighted tail in seconds; default 3600");
		System.out.println("--margin <margin>");
		System.out.println("\tmargin in pixels; default 20");
		System.out.println("--speedup <speedup>");
		System.out.println("\tspeed multiplication of the real time; default 1000.0; complementary to --total-time option");
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
