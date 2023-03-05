/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.cli;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.Help;
import app.gpx_animator.core.Option;
import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.configuration.TrackConfiguration;
import app.gpx_animator.core.configuration.adapter.FontXmlAdapter;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.data.TrackIcon;
import app.gpx_animator.core.preferences.Preferences;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

import javax.xml.XMLConstants;

import org.apache.maven.artifact.versioning.DefaultArtifactVersion;
import org.jetbrains.annotations.NotNull;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static app.gpx_animator.core.configuration.TrackConfiguration.DEFAULT_PRE_DRAW_TRACK_COLOR;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class CommandLineConfigurationFactory {

    private final List<TrackIcon> trackIconList = new ArrayList<>();
    private final List<Boolean> mirrorTrackIconList = new ArrayList<>();
    private final List<String> inputGpxList = new ArrayList<>();
    private final List<Long> trimGpxStartList = new ArrayList<>();
    private final List<Long> trimGpxEndList = new ArrayList<>();
    private final List<String> inputIconList = new ArrayList<>();

    private final List<Color> colorList = new ArrayList<>();
    private final List<Color> preDrawTrackColorList = new ArrayList<>();

    private final List<Float> lineWidthList = new ArrayList<>();

    private final boolean gui;


    private final Configuration configuration;


    @SuppressWarnings({"checkstyle:MethodLength"}) // Is it worth investing time refactoring this class?
    public CommandLineConfigurationFactory(final String... args) throws UserException {
        final var resourceBundle = Preferences.getResourceBundle();

        final var cfg = Configuration.createBuilder();

        var forceGui = false;

        final List<String> labelList = new ArrayList<>();
        final List<Long> timeOffsetList = new ArrayList<>();
        final List<Long> forcedPointIntervalList = new ArrayList<>();

        for (var i = 0; i < args.length; i++) {
            final var arg = args[i];

            try {
                final var option = arg.startsWith("--") ? Option.fromName(arg.substring(2)) : null;

                if (option == null) {
                    throw new UserException(String.format(resourceBundle.getString("cli.error.option"), arg));
                } else {
                    switch (option) {
                        case ATTRIBUTION -> cfg.attribution(args[++i]);
                        case ATTRIBUTION_POSITION -> cfg.attributionPosition(Position.parse(args[++i]));
                        case ATTRIBUTION_MARGIN -> cfg.attributionMargin(Integer.parseInt(args[++i]));
                        case INFORMATION_POSITION -> cfg.informationPosition(Position.parse(args[++i]));
                        case INFORMATION_MARGIN -> cfg.informationMargin(Integer.parseInt(args[++i]));
                        case COMMENT_POSITION -> cfg.commentPosition(Position.parse(args[++i]));
                        case COMMENT_MARGIN -> cfg.commentMargin(Integer.parseInt(args[++i]));
                        case BACKGROUND_MAP_VISIBILITY -> cfg.backgroundMapVisibility(Float.parseFloat(args[++i]));
                        case COLOR -> colorList.add(Color.decode(args[++i]));
                        case BACKGROUND_COLOR -> {
                            final long lv = Long.decode(args[++i]);
                            cfg.backgroundColor(new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true));
                        }
                        case BACKGROUND_IMAGE -> cfg.backgroundImage(new File(args[++i]));
                        case FLASHBACK_COLOR -> {
                            final long lv1 = Long.decode(args[++i]);
                            cfg.flashbackColor(new Color(lv1 < Integer.MAX_VALUE ? (int) lv1 : (int) (0xffffffff00000000L | lv1), true));
                        }
                        case FLASHBACK_DURATION -> {
                            final var s = args[++i];
                            cfg.flashbackDuration(s.trim().isEmpty() ? null : Long.parseLong(s)); // NOPMD -- null = not set
                        }
                        case FONT -> cfg.font(new FontXmlAdapter().unmarshal(args[++i]));
                        case TRIM_GPX_START -> trimGpxStartList.add(Long.parseLong(args[++i]));
                        case TRIM_GPX_END -> trimGpxEndList.add(Long.parseLong(args[++i]));
                        case FORCED_POINT_TIME_INTERVAL -> {
                            final var s1 = args[++i].trim();
                            forcedPointIntervalList.add(s1.isEmpty() ? null : Long.valueOf(s1)); // NOPMD -- null = not set
                        }
                        case FPS -> cfg.fps(Double.parseDouble(args[++i]));
                        case GPS_TIMEOUT -> cfg.gpsTimeout(Long.parseLong(args[++i]));
                        case GUI -> {
                            if (GraphicsEnvironment.isHeadless()) {
                                throw new UserException(resourceBundle.getString("cli.error.graphics"));
                            }
                            forceGui = true;
                        }
                        case HEIGHT -> cfg.height(Integer.parseInt(args[++i]));
                        case HELP -> {
                            try (var pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
                                pw.println(Constants.APPNAME_VERSION);
                                pw.println(Constants.COPYRIGHT);
                                pw.println();
                                pw.println(resourceBundle.getString("cli.help.usage"));
                                Help.printHelp(new Help.PrintWriterOptionHelpWriter(pw));
                                pw.flush();
                            }
                            exit();
                        }
                        case INPUT -> inputGpxList.add(args[++i]);
                        case TRACK_ICON -> trackIconList.add(new TrackIcon(args[++i]));
                        case TRACK_ICON_FILE -> inputIconList.add(args[++i]);
                        case TRACK_ICON_MIRROR -> mirrorTrackIconList.add(true);
                        case KEEP_IDLE -> cfg.skipIdle(false);
                        case KEEP_LAST_FRAME -> cfg.keepLastFrame(Long.parseLong(args[++i]));
                        case LABEL -> labelList.add(args[++i]);
                        case LINE_WIDTH -> lineWidthList.add(Float.valueOf(args[++i]));
                        case MARGIN -> cfg.margin(Integer.parseInt(args[++i]));
                        case MARKER_SIZE -> cfg.markerSize(Double.parseDouble(args[++i]));
                        case MAX_LAT -> cfg.maxLat(Double.parseDouble(args[++i]));
                        case MAX_LON -> cfg.maxLon(Double.parseDouble(args[++i]));
                        case MIN_LAT -> cfg.minLat(Double.parseDouble(args[++i]));
                        case MIN_LON -> cfg.minLon(Double.parseDouble(args[++i]));
                        case OUTPUT -> cfg.output(new File(args[++i]));
                        case LOGO -> cfg.logo(new File(args[++i]));
                        case LOGO_POSITION -> cfg.logoPosition(Position.parse(args[++i]));
                        case LOGO_MARGIN -> cfg.logoMargin(Integer.parseInt(args[++i]));
                        case PHOTO_DIR -> cfg.photoDirectory(new File(args[++i]));
                        case PHOTO_TIME -> cfg.photoTime(Long.parseLong(args[++i]));
                        case PHOTO_ANIMATION_DURATION -> cfg.photoAnimationDuration(Long.parseLong(args[++i]));
                        case PREVIEW_LENGTH -> cfg.previewLength(Long.parseLong(args[++i]));
                        case SKIP_IDLE -> cfg.skipIdle(Boolean.parseBoolean(args[++i]));
                        case PRE_DRAW_TRACK -> cfg.preDrawTrack(true);
                        case PRE_DRAW_TRACK_COLOR -> preDrawTrackColorList.add((Color.decode(args[++i])));
                        case SPEEDUP -> cfg.speedup(Double.parseDouble(args[++i]));
                        case SPEED_UNIT -> cfg.speedUnit(SpeedUnit.parse(args[++i], SpeedUnit.KMH));
                        case TAIL_DURATION -> cfg.tailDuration(Long.parseLong(args[++i]));
                        case TAIL_COLOR -> {
                            final long lvTailColor = Long.decode(args[++i]);
                            cfg.tailColor(new Color(lvTailColor < Integer.MAX_VALUE
                                    ? (int) lvTailColor : (int) (0xffffffff00000000L | lvTailColor), true));
                        }
                        case TAIL_COLOR_FADEOUT -> cfg.tailColorFadeout(Boolean.parseBoolean(args[++i]));
                        case TIME_OFFSET -> {
                            final var s2 = args[++i].trim();
                            timeOffsetList.add(s2.isEmpty() ? null : Long.valueOf(s2)); // NOPMD -- null = not set
                        }
                        case TMS_URL_TEMPLATE -> cfg.tmsUrlTemplate(args[++i]);
                        case TMS_API_KEY -> cfg.tmsApiKey(args[++i]);
                        case TMS_USER_AGENT -> cfg.tmsUserAgent(args[++i]);
                        case TOTAL_TIME -> {
                            final var s3 = args[++i].trim();
                            cfg.totalTime(s3.isEmpty() ? null : Long.valueOf(s3)); // NOPMD -- null = not set
                        }
                        case VIEWPORT_WIDTH -> cfg.viewportWidth(Integer.parseInt(args[++i]));
                        case VIEWPORT_HEIGHT -> cfg.viewportHeight(Integer.parseInt(args[++i]));
                        case VIEWPORT_INERTIA -> cfg.viewportInertia(Integer.parseInt(args[++i]));
                        case WAYPOINT_FONT -> cfg.waypointFont(new FontXmlAdapter().unmarshal(args[++i]));
                        case WAYPOINT_SIZE -> cfg.waypointSize(Double.parseDouble(args[++i]));
                        case WIDTH -> cfg.width(Integer.parseInt(args[++i]));
                        case ZOOM -> cfg.zoom(Integer.parseInt(args[++i]));
                        case VERSION -> {
                            try (var pw = new PrintWriter(new OutputStreamWriter(System.out, StandardCharsets.UTF_8))) {
                                pw.println(Constants.APPNAME_VERSION);
                                pw.print("(");
                                pw.print(checkVersion(resourceBundle));
                                pw.print(")\n");
                                pw.flush();
                            }
                            exit();
                        }
                        default -> throw new UserException(String.format(resourceBundle.getString("cli.error.option.implementation"), option));
                    }

                    // TODO --configuration : args[++i];
                }
            } catch (final NumberFormatException e) {
                throw new UserException(String.format(resourceBundle.getString("cli.error.number"), arg));
            } catch (final ArrayIndexOutOfBoundsException e) {
                throw new UserException(String.format(resourceBundle.getString("cli.error.parameter"), arg));
            }
        }

        normalizeColors();
        normalizePreDrawTrackColors();
        normalizeLineWidths();
        normalizeTrackIcons();
        normalizeInputIcons();
        normalizeMirrorTrackIcons();
        normalizeTrimGpxStart();
        normalizeTrimGpxEnd();

        for (int i = 0, n = inputGpxList.size(); i < n; i++) {
            final var tcb = TrackConfiguration.createBuilder();
            tcb.inputGpx(new File(inputGpxList.get(i)));
            tcb.color(colorList.get(i));
            tcb.preDrawTrackColor(preDrawTrackColorList.get(i));
            tcb.lineWidth(lineWidthList.get(i));
            tcb.label(i < labelList.size() ? labelList.get(i) : "");
            tcb.timeOffset(i < timeOffsetList.size() ? timeOffsetList.get(i) : Long.valueOf(0));
            tcb.forcedPointInterval(i < forcedPointIntervalList.size() ? forcedPointIntervalList.get(i) : null); // NOPMD -- null = not set
            if (!trackIconList.isEmpty()) {
                tcb.trackIcon(trackIconList.get(i));
            }
            tcb.inputIcon(new File(inputIconList.get(i)));
            tcb.mirrorTrackIcon(mirrorTrackIconList.get(i));
            tcb.trimGpxStart(trimGpxStartList.get(i));
            tcb.trimGpxEnd(trimGpxEndList.get(i));
            cfg.addTrackConfiguration(tcb.build());
        }

        gui = args.length == 0 || forceGui;

        configuration = cfg.build();
    }

    @SuppressWarnings({"PMD.DoNotTerminateVM", "DuplicateStringLiteralInspection"}) // Exit after printing command line help message
    @SuppressFBWarnings(value = "DM_EXIT", justification = "Exit after printing command line help message") //NON-NLS
    private void exit() {
        System.exit(0);
    }

    private void normalizeColors() {
        final var size = inputGpxList.size();
        final var size2 = colorList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                colorList.add(Color.getHSBColor((float) i / size, 0.8f, 1f));
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                colorList.add(colorList.get(i - size2));
            }
        }
    }

    private void normalizePreDrawTrackColors() {
        final var size = inputGpxList.size();
        final var size2 = preDrawTrackColorList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                preDrawTrackColorList.add(DEFAULT_PRE_DRAW_TRACK_COLOR);
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                preDrawTrackColorList.add(preDrawTrackColorList.get(i - size2));
            }
        }
    }

    private void normalizeLineWidths() {
        final var size = inputGpxList.size();
        final var size2 = lineWidthList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                lineWidthList.add(2f);
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                lineWidthList.add(lineWidthList.get(i - size2));
            }
        }
    }

    private void normalizeTrackIcons() {
        final var size = inputGpxList.size();
        final var size2 = trackIconList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                trackIconList.add(new TrackIcon("", ""));
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                trackIconList.add(trackIconList.get(i - size2));
            }
        }
    }

    private void normalizeInputIcons() {
        final var size = inputGpxList.size();
        final var size2 = inputIconList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                inputIconList.add("dummy-input-icon");
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                inputIconList.add(inputIconList.get(i - size2));
            }
        }
    }

    private void normalizeMirrorTrackIcons() {
        final var size = inputGpxList.size();
        final var size2 = mirrorTrackIconList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                mirrorTrackIconList.add(false);
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                mirrorTrackIconList.add(mirrorTrackIconList.get(i - size2));
            }
        }
    }

    private void normalizeTrimGpxStart() {
        normalizeTrimGpx(inputGpxList, trimGpxStartList);
    }

    private void normalizeTrimGpxEnd() {
        normalizeTrimGpx(inputGpxList, trimGpxEndList);
    }

    private static void normalizeTrimGpx(@NotNull final List<String> inputGpxList,
                                         @NotNull final List<Long> trimGpxList) {
        final var size = inputGpxList.size();
        final var size2 = trimGpxList.size();
        if (size2 == 0) {
            for (var i = 0; i < size; i++) {
                trimGpxList.add(0L);
            }
        } else if (size2 < size) {
            for (var i = size2; i < size; i++) {
                trimGpxList.add(trimGpxList.get(i - size2));
            }
        }
    }

    private static String checkVersion(@NotNull final ResourceBundle resourceBundle) {
        final var currentVersion = new DefaultArtifactVersion(Constants.VERSION.replace("-SNAPSHOT", ""));

        try {
            final var dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            dbf.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            final var db = dbf.newDocumentBuilder();
            final var doc = db.parse(new URL(Constants.UPDATES_URL).openStream());
            doc.getDocumentElement().normalize();

            final var elem = (Element) doc.getElementsByTagName("entry").item(0);
            if (elem == null) {
                return resourceBundle.getString("version.check.error.xml");
            }

            final var updatesVersion = new DefaultArtifactVersion(elem.getAttribute("newVersion"));
            return updatesVersion.compareTo(currentVersion) <= 0
                    ? resourceBundle.getString("version.check.latest")
                    : String.format(resourceBundle.getString("version.check.newer"), updatesVersion);
        } catch (final ParserConfigurationException | SAXException e) {
            return resourceBundle.getString("version.check.error.xml");
        } catch (final IOException e) {
            return resourceBundle.getString("version.check.error.network");
        }
    }

    public Configuration getConfiguration() {
        return configuration;
    }


    public boolean isGui() {
        return gui;
    }

}
