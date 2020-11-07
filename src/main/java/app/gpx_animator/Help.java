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
package app.gpx_animator;

import java.io.PrintWriter;
import java.util.ResourceBundle;


public final class Help {

    private Help() throws InstantiationException {
        throw new InstantiationException("Help is a Utility class and can't be instantiated!");
    }

    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static void printHelp(final OptionHelpWriter w) {
        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        final Configuration cfg = Configuration.createBuilder().build();
        final TrackConfiguration tc = TrackConfiguration.createBuilder().build();

        w.writeOptionHelp(Option.ATTRIBUTION, "text", false, cfg.getAttribution()); //NON-NLS
        w.writeOptionHelp(Option.BACKGROUND_MAP_VISIBILITY, "visibility", false, cfg.getBackgroundMapVisibility()); //NON-NLS
        w.writeOptionHelp(Option.COLOR, "color", true, resourceBundle.getString("help.option.color.default")); //NON-NLS
        w.writeOptionHelp(Option.FLASHBACK_COLOR, "ARGBcolor", false, "opaque white - #ffffffff"); // TODO cfg.getFlashbackColor()  NON-NLS
        w.writeOptionHelp(Option.FLASHBACK_DURATION, "duration", false, cfg.getFlashbackDuration()); //NON-NLS
        w.writeOptionHelp(Option.FONT_SIZE, "size", false, cfg.getFontSize()); //NON-NLS
        w.writeOptionHelp(Option.FORCED_POINT_TIME_INTERVAL, "milliseconds", true, tc.getForcedPointInterval()); //NON-NLS
        w.writeOptionHelp(Option.FPS, "fps", false, cfg.getFps()); //NON-NLS
        w.writeOptionHelp(Option.GUI, null, false, resourceBundle.getString("help.option.gui.default"));
        w.writeOptionHelp(Option.HEIGHT, "height", false, cfg.getHeight()); //NON-NLS
        w.writeOptionHelp(Option.HELP, null, false, null);
        w.writeOptionHelp(Option.INPUT, "input", true, tc.getInputGpx()); //NON-NLS
        w.writeOptionHelp(Option.TRACK_ICON, "trackIcon", true, tc.getTrackIcon()); //NON-NLS
        w.writeOptionHelp(Option.FLIP_ICON, "flipIcon", true, tc.getFlipIcon()); //NON-NLS
        w.writeOptionHelp(Option.LABEL, "label", true, tc.getLabel()); //NON-NLS
        w.writeOptionHelp(Option.LINE_WIDTH, "width", true, tc.getLineWidth()); //NON-NLS
        w.writeOptionHelp(Option.MARGIN, "margin", false, cfg.getMargin()); //NON-NLS
        w.writeOptionHelp(Option.MARKER_SIZE, "size", false, cfg.getMarkerSize()); //NON-NLS
        w.writeOptionHelp(Option.MAX_LAT, "latitude", false, cfg.getMaxLat()); //NON-NLS
        w.writeOptionHelp(Option.MAX_LON, "longitude", false, cfg.getMaxLon()); //NON-NLS
        w.writeOptionHelp(Option.MIN_LAT, "latitude", false, cfg.getMinLat()); //NON-NLS
        w.writeOptionHelp(Option.MIN_LON, "longitude", false, cfg.getMinLon()); //NON-NLS
        w.writeOptionHelp(Option.OUTPUT, "output", false, cfg.getOutput()); //NON-NLS
        w.writeOptionHelp(Option.PHOTO_TIME, "milliseconds", false, cfg.getPhotoTime()); //NON-NLS
        w.writeOptionHelp(Option.PHOTO_DIR, "directory", false, cfg.getPhotoDirectory()); //NON-NLS
        w.writeOptionHelp(Option.SKIP_IDLE, null, false, cfg.isSkipIdle());
        w.writeOptionHelp(Option.SPEEDUP, "speedup", false, cfg.getSpeedup()); //NON-NLS
        w.writeOptionHelp(Option.TAIL_DURATION, "time", false, cfg.getTailDuration()); //NON-NLS
        w.writeOptionHelp(Option.TAIL_COLOR, "tail-color", false, cfg.getTailColor()); //NON-NLS
        w.writeOptionHelp(Option.TIME_OFFSET, "milliseconds", true, tc.getTimeOffset()); //NON-NLS
        w.writeOptionHelp(Option.TMS_URL_TEMPLATE, "template", false, cfg.getTmsUrlTemplate()); //NON-NLS
        w.writeOptionHelp(Option.TOTAL_TIME, "time", false, cfg.getTotalTime()); //NON-NLS
        w.writeOptionHelp(Option.WAYPOINT_SIZE, "size", false, cfg.getWaypointSize()); //NON-NLS
        w.writeOptionHelp(Option.WIDTH, "width", false, "(800)"); // TODO cfg.getWidth() NON-NLS
        w.writeOptionHelp(Option.ZOOM, "zoom", false, cfg.getZoom()); //NON-NLS
    }

    public interface OptionHelpWriter {
        void writeOptionHelp(Option option, String argument, boolean track, Object defaultValue);
    }

    @SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
    public static final class PrintWriterOptionHelpWriter implements OptionHelpWriter {
        private final PrintWriter pw;

        public PrintWriterOptionHelpWriter(final PrintWriter pw) {
            this.pw = pw;
        }

        @Override
        public void writeOptionHelp(final Option option, final String argument, final boolean track, final Object defaultValue) {
            final ResourceBundle resourceBundle = Preferences.getResourceBundle();

            pw.print("--");
            pw.print(option.getName());
            if (argument != null) {
                pw.print(" <");
                pw.print(argument);
                pw.print(">");
            }
            pw.println();
            //noinspection MagicCharacter
            pw.print('\t');
            pw.print(option.getHelp());
            if (track) {
                pw.print("; ");
                pw.print(resourceBundle.getString("help.options.multiple"));
            }
            if (defaultValue != null) {
                pw.print("; ");
                pw.print(resourceBundle.getString("help.options.default"));
                pw.print(defaultValue);
            }
            pw.println();
        }
    }

}
