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

import app.gpx_animator.frameWriter.FileFrameWriter;
import app.gpx_animator.frameWriter.FrameWriter;
import app.gpx_animator.frameWriter.VideoFrameWriter;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.Serial;
import java.text.DateFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static app.gpx_animator.Utils.isEqual;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class Renderer {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    private static final double MS = 1000d;

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final Configuration cfg;

    private final List<List<TreeMap<Long, Point2D>>> timePointMapListList = new ArrayList<>();

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private Font font;
    private FontMetrics fontMetrics;

    private long minTime = Long.MAX_VALUE;
    private long maxTime = Long.MIN_VALUE;
    private double minX = Double.POSITIVE_INFINITY;
    private double maxX = Double.NEGATIVE_INFINITY;
    private double minY = Double.POSITIVE_INFINITY;
    private double maxY = Double.NEGATIVE_INFINITY;

    // to implement moving map smoothing
    private final LinkedList<Point2D> recentMarkers;
    private double recentMarkersXSum = 0.0;
    private double recentMarkersYSum = 0.0;

    private double speedup;

    private String lastComment = "";

    public Renderer(final Configuration cfg) {
        this.cfg = cfg;
        this.recentMarkers = new LinkedList<>();
    }

    private static double lonToX(final Double maxLon) {
        return Math.toRadians(maxLon);
    }

    private static double latToY(final double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
    }

    private static Color blendTailColor(final Color tailColor, final Color trackColor, final float ratio) {
        var r = ((double) (1 - ratio)) * tailColor.getRed() + (double) ratio * trackColor.getRed();
        var g = ((double) (1 - ratio)) * tailColor.getGreen() + (double) ratio * trackColor.getGreen();
        var b = ((double) (1 - ratio)) * tailColor.getBlue() + (double) ratio * trackColor.getBlue();
        double a = Math.max(tailColor.getAlpha(), trackColor.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    @SuppressWarnings("checkstyle:InnerAssignment") // Checkstyle 8.37 can't handle the enhanced switch properly
    public void render(final RenderingContext rc) throws UserException {
        final var renderStartTime = LocalDateTime.now();

        final List<Long[]> spanList = new ArrayList<>();
        final var wpMap = new TreeMap<Long, Point2D>();
        parseGPX(spanList, wpMap);

        final var userSpecifiedWidth = cfg.getWidth() != null;
        final var width = userSpecifiedWidth ? cfg.getWidth() : 800;
        final var zoom = calculateZoomFactor(rc, width);
        final var scale = calculateScaleFactor(width, zoom);

        minX -= cfg.getMargin() / scale;
        maxX += cfg.getMargin() / scale;
        minY -= cfg.getMargin() / scale;
        maxY += cfg.getMargin() / scale;

        if (userSpecifiedWidth) {
            final var ww = width - (maxX - minX) * scale;
            minX -= ww / scale / 2.0;
            maxX += ww / scale / 2.0;
        }

        if (cfg.getHeight() != null) {
            final var hh = cfg.getHeight() - (maxY - minY) * scale;
            minY -= hh / scale / 2.0;
            maxY += hh / scale / 2.0;
        }

        timePointMapListList.forEach((timePointMapList) -> timePointMapList
                            .forEach((timePointMap) -> translateCoordinatesToZeroZero(scale, timePointMap)));
        translateCoordinatesToZeroZero(scale, wpMap);

        final var frameFilePattern = cfg.getOutput().toString();
        //noinspection MagicCharacter
        final var dot = frameFilePattern.lastIndexOf('.');
        final var ext = dot == -1 ? null : frameFilePattern.substring(dot + 1).toLowerCase(Locale.getDefault());
        final var toImages = ext != null && (isEqual("png", ext) || isEqual("jpg", ext)); //NON-NLS

        final var realWidth = calculateRealWidth(userSpecifiedWidth, scale, toImages);
        final var realHeight = calculateRealHeight(scale, toImages);
        LOGGER.info("{}x{};{}", realWidth, realHeight, scale);

        var viewportWidth = cfg.getViewportWidth() == null ? realWidth : cfg.getViewportWidth();
        if (viewportWidth > realWidth) {
            viewportWidth = realWidth;
        }
        var viewportHeight = cfg.getViewportHeight() == null ? realHeight : cfg.getViewportHeight();
        if (viewportHeight > realHeight) {
            viewportHeight = realHeight;
        }

        final var frameWriter = toImages
                ? new FileFrameWriter(frameFilePattern, ext, cfg.getFps())
                : new VideoFrameWriter(cfg.getOutput(), cfg.getFps(), viewportWidth, viewportHeight);

        final var bi = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_3BYTE_BGR);
        final var ga = (Graphics2D) bi.getGraphics();
        drawBackground(rc, zoom, bi, ga);

        font = cfg.getFont();
        fontMetrics = ga.getFontMetrics(font);

        speedup = cfg.getTotalTime() == null ? cfg.getSpeedup() : 1.0 * (maxTime - minTime) / cfg.getTotalTime();
        final var frames = (int) ((maxTime + cfg.getTailDuration() - minTime) * cfg.getFps() / (MS * speedup));
        final var photos = new Photos(cfg.getPhotoDirectory());

        // pre-draw the path in a different color
        if (cfg.isPreDrawTrack()) {
            paint(bi, frames - 1, getTime(frames - 1) - getTime(0), null, cfg.getPreDrawTrackColor());
        }

        var skip = -1f;
        for (var frame = 1; frame < frames; frame++) {
            if (rc.isCancelled1()) {
                return;
            }

            final Long time = getTime(frame);
            skip:
            if (cfg.isSkipIdle()) {
                for (final var span : spanList) {
                    if (span[0] <= time && span[1] >= time) {
                        break skip;
                    }
                }
                rc.setProgress1((int) (100.0 * frame / frames),
                        String.format(resourceBundle.getString("renderer.progress.unusedframes"), frame, (frames - 1)));
                skip = 1f;
                continue;
            }

            final var pct = (int) (100.0 * frame / frames);
            rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.frame"), frame, (frames - 1)));

            paint(bi, frame, 0, null, null);
            final var bi2 = Utils.deepCopy(bi);
            paint(bi2, frame, cfg.getTailDuration(), cfg.getTailColor(), null);
            drawWaypoints(bi2, frame, wpMap);

            final var marker = drawMarker(bi2, frame);

            skip = renderFlashback(skip, bi2);

            // apply viewport over bi2 (which could be the full viewport)
            final var viewportImage = applyViewport(bi2, marker, realWidth, realHeight, viewportWidth, viewportHeight);

            if (font != null) {
                drawInfo(viewportImage, frame, marker);
                drawComment(viewportImage, marker);

                var att = resourceBundle.getString("configuration.attribution");
                var getAt = cfg.getAttribution();
                if (att.equals(getAt)) {
                    drawAttribution(viewportImage, att.replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION).replace("%MAP_ATTRIBUTION%", ""));
                } else {
                    drawAttribution(viewportImage, getAt);
                }
            }

            frameWriter.addFrame(viewportImage);
            photos.render(time, cfg, bi2, frameWriter, rc, pct);
        }

        keepLastFrame(rc, frameWriter, bi, frames, wpMap);
        frameWriter.close();

        final var renderFinishTime = LocalDateTime.now();
        final var runtimeSeconds = ChronoUnit.SECONDS.between(renderStartTime, renderFinishTime);

        if (!rc.isCancelled1()) {
            rc.setProgress1(100, "Finished in %d seconds".formatted(runtimeSeconds)); // TODO i18n
            if (toImages) {
                LOGGER.info("Done in {} seconds. Images written to {}", runtimeSeconds, frameFilePattern);
            } else {
                LOGGER.info("Done in {} seconds. Movie written to {}", runtimeSeconds, cfg.getOutput());
            }
        } else {
            LOGGER.info("Canceled after {} seconds.", runtimeSeconds);
        }
    }

    private BufferedImage applyViewport(final BufferedImage bi, final Point2D marker,
                                        final int realWidth, final int realHeight,
                                        final int viewportWidth, final int viewportHeight) {
        if (viewportHeight == realHeight && viewportWidth == realWidth) {
            return bi;
        }

        // Add most recent markers to a queue (while updating a running average
        // of x and y coordinates). Note that this loop almost always adds just
        // 1 element to the end of the queue, except on the first invocation, in
        // which case it fills up the entire queue with just the first marker.
        // This prevents jitter in the beginning of the movie
        while (this.recentMarkers.size() < (cfg.getViewportInertia() + 1)) {
            this.recentMarkers.add(marker);
            recentMarkersXSum += marker.getX();
            recentMarkersYSum += marker.getY();
        }

        while (this.recentMarkers.size() > cfg.getViewportInertia()) {
            final var m = this.recentMarkers.removeFirst();
            recentMarkersXSum -= m.getX();
            recentMarkersYSum -= m.getY();
        }
        final var xAvg = recentMarkersXSum / (double) this.recentMarkers.size();
        final var yAvg = recentMarkersYSum / (double) this.recentMarkers.size();

        // top-left (x,y) coords of viewport with boundaries protected
        var x = xAvg - (double) viewportWidth / 2.0;
        var y = yAvg - (double) viewportHeight / 2.0;
        if (x < 0) {
            x = 0;
        } else if ((x + viewportWidth) > realWidth) {
            x = realWidth - viewportWidth;
        }

        if (y < 0) {
            y = 0;
        } else if ((y + viewportHeight) > realHeight) {
            y = realHeight - viewportHeight;
        }
        return Utils.deepCopy(bi, (int) x, (int) y, viewportWidth, viewportHeight);
    }

    private float renderFlashback(final float skip, final BufferedImage bi2) {
        final var flashbackColor = cfg.getFlashbackColor();
        if (skip > 0f && flashbackColor.getAlpha() > 0 && cfg.getFlashbackDuration() != null && cfg.getFlashbackDuration() > 0) {
            final var g2 = (Graphics2D) bi2.getGraphics();
            g2.setColor(new Color(flashbackColor.getRed(), flashbackColor.getGreen(), flashbackColor.getBlue(),
                    (int) (flashbackColor.getAlpha() * skip)));
            g2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());
            return (float) (skip - (1000f / cfg.getFlashbackDuration() / cfg.getFps()));
        }
        return skip;
    }

    private void drawBackground(final RenderingContext rc, final Integer zoom, final BufferedImage bi, final Graphics2D ga) throws UserException {
        drawBackgroundColor(bi, ga);
        drawBackgroundMap(rc, zoom, bi);
        drawBackgroundImage(bi);
        drawLogo(bi);
    }

    private void drawBackgroundColor(final BufferedImage bi, final Graphics2D ga) {
        final var backgroundColor = cfg.getBackgroundColor();
        ga.setColor(backgroundColor);
        ga.fillRect(0, 0, bi.getWidth(), bi.getHeight());
    }

    private void drawBackgroundImage(final BufferedImage bi) throws UserException {
        final var backgroundImage = cfg.getBackgroundImage();
        if (backgroundImage != null && backgroundImage.exists()) {
            final BufferedImage image;
            try {
                image = ImageIO.read(backgroundImage);
            } catch (final IOException e) {
                throw new UserException("Can't read background image: ".concat(e.getMessage()));
            }

            var scaledImage = image.getWidth() <= bi.getWidth() && image.getHeight() <= bi.getHeight() ? image
                    : Scalr.resize(Scalr.resize(image,
                            Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, bi.getWidth()),
                            Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, bi.getHeight());

            final var g2 = getGraphics(bi);
            g2.drawImage(scaledImage, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
        }
    }

    private void drawBackgroundMap(final RenderingContext rc, final Integer zoom, final BufferedImage bi) throws UserException {
        MapUtil.drawMap(bi, cfg.getTmsUrlTemplate(), cfg.getBackgroundMapVisibility(), zoom, minX, maxX, minY, maxY, rc);
    }

    private void drawLogo(final BufferedImage bi) throws UserException {
        if (Position.HIDDEN.equals(cfg.getLogoPosition())) {
            return;
        }

        final var logo = cfg.getLogo();
        if (logo != null && logo.exists()) {
            final BufferedImage image;
            try {
                image = ImageIO.read(logo);
            } catch (final IOException e) {
                throw new UserException("Can't read logo: ".concat(e.getMessage()));
            }
            final var g2 = getGraphics(bi);
            switch (cfg.getLogoPosition()) {
                case TOP_LEFT -> g2.drawImage(image, cfg.getLogoMargin(), cfg.getLogoMargin(), image.getWidth(), image.getHeight(), null);
                case TOP_CENTER -> g2.drawImage(image, (bi.getWidth() - image.getWidth()) / 2, cfg.getLogoMargin(), image.getWidth(),
                        image.getHeight(), null);
                case TOP_RIGHT -> g2.drawImage(image, bi.getWidth() - image.getWidth() - cfg.getLogoMargin(), cfg.getLogoMargin(),
                        image.getWidth(), image.getHeight(), null);
                case BOTTOM_LEFT -> g2.drawImage(image, cfg.getLogoMargin(), bi.getHeight() - image.getHeight() - cfg.getLogoMargin(),
                        image.getWidth(), image.getHeight(), null);
                case BOTTOM_CENTER -> g2.drawImage(image, (bi.getWidth() - image.getWidth()) / 2,
                        bi.getHeight() - image.getHeight() - cfg.getLogoMargin(), image.getWidth(), image.getHeight(), null);
                case BOTTOM_RIGHT -> g2.drawImage(image, bi.getWidth() - image.getWidth() - cfg.getLogoMargin(),
                        bi.getHeight() - image.getHeight() - cfg.getLogoMargin(), image.getWidth(), image.getHeight(), null);
                default -> throw new UserException("Invalid logo position!");
            }
        }
    }

    private void parseGPX(final List<Long[]> spanList, final TreeMap<Long, Point2D> wpMap) throws UserException {
        var trackIndex = -1;
        for (final var trackConfiguration : cfg.getTrackConfigurationList()) {
            trackIndex++;

            final var inputGpxFile = trackConfiguration.getInputGpx();
            final var gch = new GpxContentHandler();
            GpxParser.parseGpx(inputGpxFile, gch);

            final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<>();

            final var pointLists = gch.getPointLists();
            if (pointLists.isEmpty()) {
                throw new UserException(resourceBundle.getString("renderer.error.notrack").formatted(inputGpxFile));
            }
            for (final var latLonList : pointLists) {
                sigmaRoxRepair(latLonList);
                final var timePointMap = new TreeMap<Long, Point2D>();
                toTimePointMap(timePointMap, trackIndex, latLonList);
                trimGpxData(timePointMap, trackConfiguration);
                timePointMapList.add(timePointMap);
                toTimePointMap(wpMap, trackIndex, gch.getWaypointList());
                mergeConnectedSpans(spanList, timePointMap);
            }

            Collections.reverse(timePointMapList); // reversing because of last known location drawing
            timePointMapListList.add(timePointMapList);
        }
    }

    /**
     * There is an error in the Sigma Rox 12 (and maybe other models) which
     * does not save the timestamp on the first and last track points in the
     * GPX files. The second track point has an identical position to the first
     * one with a time, so the first entry can be ignored. Same with the
     * penultimate and last entry, so the last entry can be ignored, too. The
     * result is a fixed list of track points containing the correct track and
     * timestamps.
     *
     * @param latLonList list of track points loaded from the GPX file
     */
    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition") // here 2 is magic :-)
    private void sigmaRoxRepair(final List<LatLon> latLonList) {
        if (latLonList.size() >= 2) {
            final var first = latLonList.get(0);
            final var second = latLonList.get(1);
            if (first.getTime() == Long.MIN_VALUE && second.getTime() > Long.MIN_VALUE
                    && first.getLat() == second.getLat() && first.getLon() == second.getLon()) {
                latLonList.remove(0);
            }
        }
        if (latLonList.size() >= 2) {
            final var last = latLonList.get(latLonList.size() - 1);
            final var penultimate = latLonList.get(latLonList.size() - 2);
            if (last.getTime() == Long.MIN_VALUE && penultimate.getTime() > Long.MIN_VALUE
                    && last.getLat() == penultimate.getLat() && last.getLon() == penultimate.getLon()) {
                latLonList.remove(latLonList.size() - 1);
            }
        }
    }

    private int calculateRealHeight(final double scale, final boolean toImages) {
        var realHeight = (int) Math.round(((maxY - minY) * scale));
        if (realHeight % 2 != 0 && cfg.getHeight() == null && !toImages) {
            realHeight++;
        }
        return realHeight;
    }

    private int calculateRealWidth(final boolean userSpecifiedWidth, final double scale, final boolean toImages) {
        var realWidth = (int) Math.round(((maxX - minX) * scale));
        if (realWidth % 2 != 0 && !userSpecifiedWidth && !toImages) {
            realWidth++;
        }
        return realWidth;
    }

    private void translateCoordinatesToZeroZero(final double scale, final TreeMap<Long, Point2D> timePointMap) {
        if (!timePointMap.isEmpty()) {
            maxTime = Math.max(maxTime, timePointMap.lastKey());
            minTime = Math.min(minTime, timePointMap.firstKey());

            for (final var point : timePointMap.values()) {
                point.setLocation((point.getX() - minX) * scale, (maxY - point.getY()) * scale);
            }
        }
    }

    private void mergeConnectedSpans(final List<Long[]> spanList, final TreeMap<Long, Point2D> timePointMap) {
        long t0 = timePointMap.firstKey();
        var t1 = timePointMap.lastKey() + cfg.getTailDuration();

        for (final var iter = spanList.iterator(); iter.hasNext();) {
            final var span = iter.next();
            if (t0 > span[0] && t1 < span[1]) {
                // swallowed
                return;
            }

            if (t0 < span[0] && t1 > span[1]) {
                // swallows
                iter.remove();
            } else if (t1 > span[0] && t1 < span[1]) {
                t1 = span[1];
                iter.remove();
            } else if (t0 < span[1] && t0 > span[0]) {
                t0 = span[0];
                iter.remove();
            }
        }

        spanList.add(new Long[]{t0, t1});
    }

    private Integer calculateZoomFactor(final RenderingContext rc, final int width) {
        final Integer zoom;

        if (cfg.getTmsUrlTemplate() != null && cfg.getZoom() == null) {
            // force using computed zoom
            final var userSpecifiedHeight = cfg.getHeight() != null;
            if (userSpecifiedHeight) {
                final int height = cfg.getHeight();
                final var zoom1 = (int) Math.floor(Math.log(Math.PI / 128.0 * (width - cfg.getMargin() * 2) / (maxX - minX)) / Math.log(2));
                final var zoom2 = (int) Math.floor(Math.log(Math.PI / 128.0 * (height - cfg.getMargin() * 2) / (maxY - minY)) / Math.log(2));
                zoom = Math.min(zoom1, zoom2);
            } else {
                zoom = (int) Math.floor(Math.log(Math.PI / 128.0 * (width - cfg.getMargin() * 2) / (maxX - minX)) / Math.log(2));
            }
            rc.setProgress1(0, String.format(resourceBundle.getString("renderer.progress.zoom"), zoom));
        } else {
            zoom = cfg.getZoom();
        }
        return zoom;
    }

    private double calculateScaleFactor(final int width, final Integer zoom) {
        return zoom == null
                ? (width - cfg.getMargin() * 2) / (maxX - minX)
                : (128.0 * (1 << zoom)) / Math.PI;
    }

    private void trimGpxData(final TreeMap<Long, Point2D> timePointMap, final TrackConfiguration trackConfiguration) {

        final var trimGpxStart = trackConfiguration.getTrimGpxStart();
        if (trimGpxStart != null && trimGpxStart > 0 && timePointMap.size() > 0) {
            final Long skipToTime = timePointMap.firstKey() + trimGpxStart;
            timePointMap.entrySet().removeIf(e -> e.getKey() < skipToTime);
        }

        final var trimGpxEnd = trackConfiguration.getTrimGpxEnd();
        if (trimGpxEnd != null && trimGpxEnd > 0 && timePointMap.size() > 0) {
            final Long skipAfterTime = timePointMap.lastKey() - trimGpxEnd;
            timePointMap.entrySet().removeIf(e -> e.getKey() > skipAfterTime);
        }
    }

    private void keepLastFrame(final RenderingContext rc, final FrameWriter frameWriter, final BufferedImage bi, final int frames,
                               final TreeMap<Long, Point2D> wpMap) throws UserException {
        final var keepLastFrame = cfg.getKeepLastFrame() != null && cfg.getKeepLastFrame() > 0;
        if (keepLastFrame) {
            drawWaypoints(bi, frames, wpMap);
            final var marker = drawMarker(bi, frames);
            if (font != null) {
                drawInfo(bi, frames, marker);
                drawComment(bi, marker);

                var att = resourceBundle.getString("configuration.attribution");
                if (cfg.getAttribution().equals(att)) {
                    drawAttribution(bi, att.replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION).replace("%MAP_ATTRIBUTION%", ""));
                } else {
                    drawAttribution(bi, cfg.getAttribution());
                }
            }
            final long ms = cfg.getKeepLastFrame();
            final var fps = Double.valueOf(cfg.getFps()).longValue();
            final var stillFrames = ms / 1_000 * fps;
            for (long stillFrame = 0; stillFrame < stillFrames; stillFrame++) {
                final var pct = (int) (100.0 * stillFrame / stillFrames);
                rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.keeplastframe"), stillFrame, stillFrames));
                frameWriter.addFrame(bi);
                if (rc.isCancelled1()) {
                    return;
                }
            }
        }
    }

    private void drawWaypoints(final BufferedImage bi, final int frame, final TreeMap<Long, Point2D> wpMap) {
        final var waypointSize = cfg.getWaypointSize();
        if (waypointSize == null || waypointSize == 0.0 || wpMap.isEmpty()) {
            return;
        }

        final var g2 = getGraphics(bi);

        final var t2 = getTime(frame);


        if (t2 >= wpMap.firstKey()) {
            for (final var p : wpMap.subMap(wpMap.firstKey(), t2).values()) {
                g2.setColor(Color.white);
                final var marker = createMarker(waypointSize, p);
                g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.fill(marker);
                g2.setColor(Color.black);
                g2.draw(marker);

                printText(g2, ((NamedPoint) p).getName(), (float) p.getX() + 8f, (float) p.getY() + 4f);
            }
        }
    }

    private Ellipse2D.Double createMarker(final Double size, final Point2D point) {
        return new Ellipse2D.Double(point.getX() - size / 2.0, point.getY() - size / 2.0, size, size);
    }

    private void toTimePointMap(final TreeMap<Long, Point2D> timePointMap, final int trackIndex, final List<LatLon> latLonList) throws UserException {
        long forcedTime = 0;

        final var trackConfiguration = cfg.getTrackConfigurationList().get(trackIndex);

        final var minLon = cfg.getMinLon();
        final var maxLon = cfg.getMaxLon();
        final var minLat = cfg.getMinLat();
        final var maxLat = cfg.getMaxLat();

        if (minLon != null) {
            minX = lonToX(minLon);
        }
        if (maxLon != null) {
            maxX = lonToX(maxLon);
        }
        if (maxLat != null) {
            minY = latToY(maxLat);
        }
        if (minLat != null) {
            maxY = latToY(minLat);
        }

        for (final var latLon : latLonList) {
            final var x = lonToX(latLon.getLon());
            final var y = latToY(latLon.getLat());

            if (minLon == null) {
                minX = Math.min(x, minX);
            }
            if (maxLat == null) {
                minY = Math.min(y, minY);
            }
            if (maxLon == null) {
                maxX = Math.max(x, maxX);
            }
            if (minLat == null) {
                maxY = Math.max(y, maxY);
            }

            long time;
            final var forcedPointInterval = trackConfiguration.getForcedPointInterval();
            if (forcedPointInterval != null) {
                forcedTime += forcedPointInterval;
                time = forcedTime;
            } else {
                time = latLon.getTime();
                if (time == Long.MIN_VALUE) {
                    final var filename = trackConfiguration.getInputGpx().getName();
                    throw new UserException(
                            String.format(resourceBundle.getString("error.missingtime"), filename));
                }
            }

            if (trackConfiguration.getTimeOffset() != null) {
                time += trackConfiguration.getTimeOffset();
            }

            final Point2D point;
            if (latLon instanceof Waypoint) {
                final var namedPoint = new NamedPoint();
                namedPoint.setLocation(x, y);
                namedPoint.setName(((Waypoint) latLon).getName());
                point = namedPoint;
            } else {
                point = new GpxPoint(x, y, latLon, time);
            }

            // hack to prevent overwriting existing (way)point with same time
            var freeTime = time;
            while (timePointMap.containsKey(freeTime)) {
                freeTime++;
            }
            timePointMap.put(freeTime, point);
        }
    }

    private void drawInfo(final BufferedImage bi, final int frame, final Point2D marker) throws UserException {
        if (Position.HIDDEN.equals(cfg.getInformationPosition())) {
            return;
        }

        final var dateString = dateFormat.format(getTime(frame));
        final var latLongString = getLatLonString(marker);
        final var speedString = SpeedUtil.getSpeedString(marker, getTime(frame), frame, cfg.getFps(), cfg.getSpeedUnit());
        final var graphics = getGraphics(bi);

        switch (cfg.getInformationPosition()) {
            case TOP_LEFT -> {
                printText(graphics, dateString, cfg.getInformationMargin(), cfg.getInformationMargin() + fontMetrics.getHeight() * 2);
                printText(graphics, latLongString, cfg.getInformationMargin(), cfg.getInformationMargin() + fontMetrics.getHeight());
                printText(graphics, speedString, cfg.getInformationMargin(), cfg.getInformationMargin());
            }
            case TOP_CENTER -> {
                printText(graphics, dateString, (float) (bi.getWidth() - fontMetrics.stringWidth(dateString)) / 2,
                        cfg.getInformationMargin() + fontMetrics.getHeight() * 2);
                printText(graphics, latLongString, (float) (bi.getWidth() - fontMetrics.stringWidth(latLongString)) / 2,
                        cfg.getInformationMargin() + fontMetrics.getHeight());
                printText(graphics, speedString, (float) (bi.getWidth() - fontMetrics.stringWidth(speedString)) / 2, cfg.getInformationMargin());
            }
            case TOP_RIGHT -> {
                printText(graphics, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.getInformationMargin(),
                        cfg.getInformationMargin() + fontMetrics.getHeight() * 2);
                printText(graphics, latLongString, bi.getWidth() - fontMetrics.stringWidth(latLongString) - cfg.getInformationMargin(),
                        cfg.getInformationMargin() + fontMetrics.getHeight());
                printText(graphics, speedString, bi.getWidth() - fontMetrics.stringWidth(speedString) - cfg.getInformationMargin(),
                        cfg.getInformationMargin());
            }
            case BOTTOM_LEFT -> {
                printText(graphics, dateString, cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin());
                printText(graphics, latLongString, cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight());
                printText(graphics, speedString, cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight() * 2);
            }
            case BOTTOM_CENTER -> {
                printText(graphics, dateString, (float) (bi.getWidth() - fontMetrics.stringWidth(dateString)) / 2,
                        bi.getHeight() - cfg.getInformationMargin());
                printText(graphics, latLongString, (float) (bi.getWidth() - fontMetrics.stringWidth(latLongString)) / 2,
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight());
                printText(graphics, speedString, (float) (bi.getWidth() - fontMetrics.stringWidth(speedString)) / 2,
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight() * 2);
            }
            case BOTTOM_RIGHT -> {
                printText(graphics, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin());
                printText(graphics, latLongString, bi.getWidth() - fontMetrics.stringWidth(latLongString) - cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight());
                printText(graphics, speedString, bi.getWidth() - fontMetrics.stringWidth(speedString) - cfg.getInformationMargin(),
                        bi.getHeight() - cfg.getInformationMargin() - fontMetrics.getHeight() * 2);
            }
            default -> throw new UserException("Invalid information position!");
        }
    }

    private void drawComment(final BufferedImage bi, final Point2D marker) throws UserException {
        final var cmt = getCommentString(marker);
        if (cmt.isBlank()) {
            return;
        }

        final var graphics = getGraphics(bi);

        switch (cfg.getCommentPosition()) {
            case TOP_LEFT -> printText(graphics, cmt, cfg.getCommentMargin(), cfg.getCommentMargin());
            case TOP_CENTER -> printText(graphics, cmt, (float) (bi.getWidth() - fontMetrics.stringWidth(cmt)) / 2, cfg.getCommentMargin());
            case TOP_RIGHT -> printText(graphics, cmt, bi.getWidth() - fontMetrics.stringWidth(cmt) - cfg.getCommentMargin(),
                    cfg.getCommentMargin());
            case BOTTOM_LEFT -> printText(graphics, cmt, cfg.getCommentMargin(),
                    bi.getHeight() - cfg.getCommentMargin() - fontMetrics.getHeight() * 2);
            case BOTTOM_CENTER -> printText(graphics, cmt, (float) (bi.getWidth() - fontMetrics.stringWidth(cmt)) / 2,
                    bi.getHeight() - cfg.getCommentMargin() - fontMetrics.getHeight() * 2);
            case BOTTOM_RIGHT -> printText(graphics, cmt, bi.getWidth() - fontMetrics.stringWidth(cmt) - cfg.getCommentMargin(),
                    bi.getHeight() - cfg.getCommentMargin() - fontMetrics.getHeight() * 2);
            default -> throw new UserException("Invalid comment position!");
        }
    }



    private String getLatLonString(final Point2D point) {
        if (point instanceof GpxPoint) {
            final var gpxPoint = (GpxPoint) point;
            final var latLon = gpxPoint.getLatLon();
            return String.format("%.4f, %.4f", latLon.getLat(), latLon.getLon()); //NON-NLS
        } else {
            return "";
        }
        return lastComment;
    }


    private void drawAttribution(final BufferedImage bi, final String attribution) throws UserException {
        if (Position.HIDDEN.equals(cfg.getAttributionPosition())) {
            return;
        }

        var hasSplit = false;

        if (attribution.trim().contains("\n")) {
            hasSplit = true;
        }

        if (hasSplit) {
            final var lines = attribution.trim().split("\n");
            switch (cfg.getAttributionPosition()) {
                case TOP_LEFT -> {
                    printText(getGraphics(bi), lines[0], cfg.getAttributionMargin(), cfg.getAttributionMargin() + fontMetrics.getHeight());
                    printText(getGraphics(bi), lines[1], cfg.getAttributionMargin(), cfg.getAttributionMargin() + fontMetrics.getHeight() * 2);
                }
                case TOP_CENTER -> {
                    printText(getGraphics(bi), lines[0], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[0])) / 2,
                            cfg.getAttributionMargin() + fontMetrics.getHeight());
                    printText(getGraphics(bi), lines[1], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[1])) / 2,
                            cfg.getAttributionMargin() + fontMetrics.getHeight() * 2);
                }
                case TOP_RIGHT -> {
                    printText(getGraphics(bi), lines[0], bi.getWidth() - fontMetrics.stringWidth(lines[0]) - cfg.getAttributionMargin(),
                            cfg.getAttributionMargin() + fontMetrics.getHeight());
                    printText(getGraphics(bi), lines[1], bi.getWidth() - fontMetrics.stringWidth(lines[1]) - cfg.getAttributionMargin(),
                            cfg.getAttributionMargin() + fontMetrics.getHeight() * 2);
                }
                case BOTTOM_LEFT -> {
                    printText(getGraphics(bi), lines[0], cfg.getAttributionMargin(), bi.getHeight() - cfg.getAttributionMargin());
                    printText(getGraphics(bi), lines[1], cfg.getAttributionMargin(), bi.getHeight() - cfg.getAttributionMargin() * 2);
                }
                case BOTTOM_CENTER -> {
                    printText(getGraphics(bi), lines[0], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[0])) / 2,
                            bi.getHeight() - cfg.getAttributionMargin());
                    printText(getGraphics(bi), lines[1], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[1])) / 2,
                            bi.getHeight() - cfg.getAttributionMargin() * 2);
                }
                case BOTTOM_RIGHT -> {
                    printText(getGraphics(bi), lines[0], bi.getWidth() - fontMetrics.stringWidth(lines[0]) - cfg.getAttributionMargin(),
                            bi.getHeight() - cfg.getAttributionMargin());
                    printText(getGraphics(bi), lines[1], bi.getWidth() - fontMetrics.stringWidth(lines[1]) - cfg.getAttributionMargin(),
                            bi.getHeight() - cfg.getAttributionMargin() * 2);
                }
                default -> throw new UserException("Invalid attribution position!");
            }
        } else {
            switch (cfg.getAttributionPosition()) {
                case TOP_LEFT -> printText(getGraphics(bi), attribution, cfg.getAttributionMargin(),
                        cfg.getAttributionMargin() + fontMetrics.getHeight());
                case TOP_CENTER -> printText(getGraphics(bi), attribution, (float) (bi.getWidth() - fontMetrics.stringWidth(attribution)) / 2,
                        cfg.getAttributionMargin() + fontMetrics.getHeight());
                case TOP_RIGHT -> printText(getGraphics(bi), attribution,
                        bi.getWidth() - fontMetrics.stringWidth(attribution) - cfg.getAttributionMargin(),
                        cfg.getAttributionMargin() + fontMetrics.getHeight());
                case BOTTOM_LEFT -> printText(getGraphics(bi), attribution,
                        cfg.getAttributionMargin(), bi.getHeight() - cfg.getAttributionMargin());
                case BOTTOM_CENTER -> printText(getGraphics(bi), attribution, (float) (bi.getWidth() - fontMetrics.stringWidth(attribution)) / 2,
                        bi.getHeight() - cfg.getAttributionMargin());
                case BOTTOM_RIGHT -> printText(getGraphics(bi), attribution,
                        bi.getWidth() - fontMetrics.stringWidth(attribution) - cfg.getAttributionMargin(),
                        bi.getHeight() - cfg.getAttributionMargin());
                default -> throw new UserException("Invalid attribution position!");
            }
        }
    }

    private Point2D drawMarker(final BufferedImage bi, final int frame) throws UserException {
        if (cfg.getMarkerSize() == null || cfg.getMarkerSize() == 0.0) {
            return null;
        }

        Point2D point = null;

        final var g2 = getGraphics(bi);
        final var t2 = getTime(frame);
        final var trackConfigurationList = cfg.getTrackConfigurationList();

        var i = 0;
        outer:
        for (final var timePointMapList : timePointMapListList) {
            final var trackConfiguration = trackConfigurationList.get(i++);
            for (final var timePointMap : timePointMapList) {
                final var ceilingEntry = timePointMap.ceilingEntry(t2);
                final var floorEntry = timePointMap.floorEntry(t2);
                if (floorEntry == null) {
                    continue;
                }

                point = floorEntry.getValue();
                if (t2 - floorEntry.getKey() <= cfg.getTailDuration()) {

                    g2.setColor(ceilingEntry == null ? Color.white : trackConfiguration.getColor());

                    final var trackIcon = trackConfiguration.getTrackIcon();
                    final var trackIconFile = trackConfiguration.getInputIcon();
                    if (trackIconFile != null && trackIconFile.exists() && trackIconFile.canRead()) {
                        try {
                            drawIconFileOnGraphics2D(point, g2, trackIconFile, trackConfiguration.isTrackIconMirrored());
                        } catch (final IOException e) {
                            throw new UserException(resourceBundle.getString("renderer.error.iconfile").formatted(trackIconFile), e);
                        }
                    } else if (trackIcon != null && !trackIcon.getKey().isEmpty()) {
                        try {
                            drawIconOnGraphics2D(point, g2, trackIcon, trackConfiguration.isTrackIconMirrored());
                        } catch (final IOException e) {
                            throw new UserException(resourceBundle.getString("renderer.error.icon"), e);
                        }
                    } else {
                        drawSimpleCircleOnGraphics2D(point, g2);
                    }

                    final var label = trackConfiguration.getLabel();
                    if (!label.isEmpty()) {
                        printText(g2, label, (float) point.getX() + 8f, (float) point.getY() + 4f);
                    }
                }

                continue outer; // NOPMD -- Continue the outer loop, not the inner one
            }
        }
        return point;
    }

    private void drawSimpleCircleOnGraphics2D(final Point2D point, final Graphics2D g2) {

        final double markerSize = cfg.getMarkerSize();

        final var marker = createMarker(markerSize, point);
        g2.setStroke(new BasicStroke(1f));
        g2.fill(marker);
        g2.setColor(Color.black);
        g2.draw(marker);
    }

    private void drawIconOnGraphics2D(final Point2D point, final Graphics2D g2, final TrackIcon trackIcon, final boolean mirrorTrackIcon)
            throws IOException {
        final var trackIconImage = ImageIO.read(getClass().getResource(trackIcon.getFilename()));
        drawImageOnGraphics2D(point, g2, trackIconImage, mirrorTrackIcon);
    }

    private void drawIconFileOnGraphics2D(final Point2D point, final Graphics2D g2, final File trackIconFile, final boolean mirrorTrackIcon)
            throws IOException {
        final var trackIconImage = ImageIO.read(trackIconFile);
        drawImageOnGraphics2D(point, g2, trackIconImage, mirrorTrackIcon);
    }

    private void drawImageOnGraphics2D(final Point2D point, final Graphics2D g2, final BufferedImage trackIconImage, final boolean mirrorTrackIcon)
            throws IOException {
        var image = trackIconImage;
        final var at = new AffineTransform();
        at.translate((int) point.getX() + 8f, (int) point.getY() + 4f);
        try {
            at.translate(-trackIconImage.getWidth() / 2d, -trackIconImage.getHeight() / 2d);
            if (mirrorTrackIcon) {
                final var tx = AffineTransform.getScaleInstance(-1, 1);
                tx.translate(-trackIconImage.getWidth(null), 0);
                final var op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
                image = op.filter(trackIconImage, null);
            }
        } catch (final Exception e) {
            throw new IOException(e.getMessage());
        }
        g2.drawImage(image, at, null);
    }

    private void paint(final BufferedImage bi, final int frame, final long backTime, final Color overrideColor, final Color preDrawTrackColor) {
        final var g2 = getGraphics(bi);

        final var time = getTime(frame);

        final var trackConfigurationList = cfg.getTrackConfigurationList();

        var i = 0;
        for (final var timePointMapList : timePointMapListList) {
            final var trackConfiguration = trackConfigurationList.get(i++);

            for (final var timePointMap : timePointMapList) {
                g2.setStroke(new BasicStroke(trackConfiguration.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                final var toTime = timePointMap.floorKey(time);

                if (toTime == null) {
                    continue;
                }

                Point2D prevPoint = null;

                if (backTime == 0) {
                    final var prevTime = getTime(frame - 1);
                    var fromTime = timePointMap.floorKey(prevTime);
                    if (fromTime == null) {
                        // try ceiling because we may be at beginning
                        fromTime = timePointMap.ceilingKey(prevTime);
                    }
                    if (fromTime == null) {
                        continue;
                    }

                    g2.setPaint(trackConfiguration.getColor());
                    for (final var entry : timePointMap.subMap(fromTime, true, toTime, true).entrySet()) {
                        if (prevPoint != null) {
                            g2.draw(new Line2D.Double(prevPoint, entry.getValue()));
                        }
                        prevPoint = entry.getValue();
                    }
                } else {
                    for (final var entry : timePointMap.subMap(toTime - backTime, true, toTime, true).entrySet()) {
                        if (prevPoint != null) {
                            var drawSegment = false;
                            if (preDrawTrackColor == null) {
                                final var ratio = (backTime - time + entry.getKey()) * 1f / backTime;
                                if (ratio > 0) {
                                    g2.setPaint(blendTailColor(trackConfiguration.getColor(), overrideColor, ratio));
                                    drawSegment = true;
                                }
                            } else {
                                g2.setColor(preDrawTrackColor);
                                drawSegment = true;
                            }

                            if (drawSegment) {
                                g2.draw(new Line2D.Double(prevPoint, entry.getValue()));
                            }
                        }
                        prevPoint = entry.getValue();
                    }
                }
            }
        }
    }

    private long getTime(final int frame) {
        return (long) Math.floor(minTime + frame / cfg.getFps() * MS * speedup);
    }

    private void printText(final Graphics2D g2, final String text, final float x, final float y) {
        final var frc = g2.getFontRenderContext();
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        final var height = g2.getFontMetrics(font).getHeight();

        final var lines = text == null ? new String[0] : text.split("\n");
        var yy = y - (lines.length - 1) * height;
        for (final var line : lines) {
            if (!line.isEmpty()) {
                final var tl = new TextLayout(line, font, frc);
                final var sha = tl.getOutline(AffineTransform.getTranslateInstance(x, yy));
                g2.setColor(Color.white);
                g2.fill(sha);
                g2.draw(sha);

                g2.setFont(font);
                g2.setColor(Color.black);
                g2.drawString(line, x, yy);
            }

            yy += height;
        }
    }

    private Graphics2D getGraphics(final BufferedImage bi) {
        final var g2 = (Graphics2D) bi.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return g2;
    }

    private static class NamedPoint extends Point2D.Double {
        @Serial
        private static final long serialVersionUID = 4011941819652468006L;

        private String name;

        public String getName() {
            return name;
        }

        public void setName(final String name) {
            this.name = name;
        }
    }

}
