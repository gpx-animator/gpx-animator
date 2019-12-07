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

    public static void printHelp(final OptionHelpWriter w) {
        final Configuration cfg;
        final TrackConfiguration tc;
        try {
            cfg = Configuration.createBuilder().build();
            tc = TrackConfiguration.createBuilder().build();
        } catch (final UserException e) {
            throw new RuntimeException(e); // should never happen
        }

        w.writeOptionHelp(Option.ATTRIBUTION, "text", false, cfg.getAttribution());
        w.writeOptionHelp(Option.BACKGROUND_MAP_VISIBILITY, "visibility", false, cfg.getBackgroundMapVisibility());
        w.writeOptionHelp(Option.COLOR, "color", true, "some nice color :-)");
        w.writeOptionHelp(Option.FLASHBACK_COLOR, "ARGBcolor", false, "opaque white - #ffffffff"); // TODO cfg.getFlashbackColor()
        w.writeOptionHelp(Option.FLASHBACK_DURATION, "duration", false, cfg.getFlashbackDuration());
        w.writeOptionHelp(Option.FONT_SIZE, "size", false, cfg.getFontSize());
        w.writeOptionHelp(Option.FORCED_POINT_TIME_INTERVAL, "milliseconds", true, tc.getForcedPointInterval());
        w.writeOptionHelp(Option.FPS, "fps", false, cfg.getFps());
        w.writeOptionHelp(Option.GUI, null, false, "if no argument is specified");
        w.writeOptionHelp(Option.HEIGHT, "height", false, cfg.getHeight());
        w.writeOptionHelp(Option.HELP, null, false, null);
        w.writeOptionHelp(Option.INPUT, "input", true, tc.getInputGpx());
        w.writeOptionHelp(Option.ICON_ENABLE, "iconEnable", true, tc.isEnableIcon());
        w.writeOptionHelp(Option.LABEL, "label", true, tc.getLabel());
        w.writeOptionHelp(Option.LINE_WIDTH, "width", true, tc.getLineWidth());
        w.writeOptionHelp(Option.MARGIN, "margin", false, cfg.getMargin());
        w.writeOptionHelp(Option.MARKER_SIZE, "size", false, cfg.getMarkerSize());
        w.writeOptionHelp(Option.MAX_LAT, "latitude", false, cfg.getMaxLat());
        w.writeOptionHelp(Option.MAX_LON, "longitude", false, cfg.getMaxLon());
        w.writeOptionHelp(Option.MIN_LAT, "latitude", false, cfg.getMinLat());
        w.writeOptionHelp(Option.MIN_LON, "longitude", false, cfg.getMinLon());
        w.writeOptionHelp(Option.OUTPUT, "output", false, cfg.getOutput());
        w.writeOptionHelp(Option.PHOTO_TIME, "milliseconds", false, cfg.getPhotoTime());
        w.writeOptionHelp(Option.PHOTOS, "directory", false, cfg.getPhotos());
        w.writeOptionHelp(Option.SKIP_IDLE, null, false, cfg.isSkipIdle());
        w.writeOptionHelp(Option.SPEEDUP, "speedup", false, cfg.getSpeedup());
        w.writeOptionHelp(Option.TAIL_DURATION, "time", false, cfg.getTailDuration());
        w.writeOptionHelp(Option.TAIL_COLOR, "tail-color", false, cfg.getTailColor());
        w.writeOptionHelp(Option.TILE_CACHE_TIME_LIMIT, "seconds", false, cfg.getTileCacheTimeLimit());
        w.writeOptionHelp(Option.TILE_CACHE_PATH, "text", false, cfg.getTileCachePath());
        w.writeOptionHelp(Option.TIME_OFFSET, "milliseconds", true, tc.getTimeOffset());
        w.writeOptionHelp(Option.TMS_URL_TEMPLATE, "template", false, cfg.getTmsUrlTemplate());
        w.writeOptionHelp(Option.TOTAL_TIME, "time", false, cfg.getTotalTime());
        w.writeOptionHelp(Option.WAYPOINT_SIZE, "size", false, cfg.getWaypointSize());
        w.writeOptionHelp(Option.WIDTH, "width", false, "(800)"); // cfg.getWidth()
        w.writeOptionHelp(Option.ZOOM, "zoom", false, cfg.getZoom());
    }

    public interface OptionHelpWriter {
        void writeOptionHelp(Option option, String argument, boolean track, Object defaultValue);
    }

    public static class PrintWriterOptionHelpWriter implements OptionHelpWriter {
        private final PrintWriter pw;

        public PrintWriterOptionHelpWriter(final PrintWriter pw) {
            this.pw = pw;
        }

        @Override
        public void writeOptionHelp(final Option option, final String argument, final boolean track, final Object defaultValue) {
            pw.print("--");
            pw.print(option.getName());
            if (argument != null) {
                pw.print(" <");
                pw.print(argument);
                pw.print(">");
            }
            pw.println();
            pw.print('\t');
            pw.print(option.getHelp());
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
