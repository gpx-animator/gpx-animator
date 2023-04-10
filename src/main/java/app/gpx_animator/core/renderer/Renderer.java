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
package app.gpx_animator.core.renderer;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.configuration.TrackConfiguration;
import app.gpx_animator.core.data.TrackIcon;
import app.gpx_animator.core.data.entity.MyPoint;
import app.gpx_animator.core.data.entity.TrackPoint;
import app.gpx_animator.core.data.entity.TrackSegment;
import app.gpx_animator.core.data.entity.WayPoint;
import app.gpx_animator.core.data.gpx.GpxContentHandler;
import app.gpx_animator.core.data.gpx.GpxParser;
import app.gpx_animator.core.data.gpx.GpxPoint;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.framewriter.FileFrameWriter;
import app.gpx_animator.core.renderer.framewriter.FrameWriter;
import app.gpx_animator.core.renderer.framewriter.NullFrameWriter;
import app.gpx_animator.core.renderer.framewriter.VideoFrameWriter;
import app.gpx_animator.core.renderer.plugins.RendererPlugin;
import app.gpx_animator.core.util.PluginUtil;
import app.gpx_animator.core.util.RenderUtil;
import app.gpx_animator.core.util.Utils;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
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
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;
import static app.gpx_animator.core.util.Utils.isEqual;
import static java.util.Objects.requireNonNull;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class Renderer {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    private static final double MS = 1000d;

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final Configuration cfg;

    private final List<List<TreeMap<Long, Point2D>>> timePointMapListList = new ArrayList<>();

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

    public Renderer(@NonNull final Configuration cfg) throws UserException {
        this.cfg = cfg.validate();
        this.recentMarkers = new LinkedList<>();
    }

    private static double lonToX(final Double lon) {
        return Math.toRadians(lon);
    }

    private static double latToY(final double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
    }

    private static Color blendTailColor(@NonNull final Color tailColor,
                                        @NonNull final Color trackColor,
                                        final float ratio) {
        var r = ((double) (1 - ratio)) * tailColor.getRed() + (double) ratio * trackColor.getRed();
        var g = ((double) (1 - ratio)) * tailColor.getGreen() + (double) ratio * trackColor.getGreen();
        var b = ((double) (1 - ratio)) * tailColor.getBlue() + (double) ratio * trackColor.getBlue();
        double a = Math.max(tailColor.getAlpha(), trackColor.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    @SuppressWarnings({ "checkstyle:InnerAssignment" }) // Checkstyle 8.37 can't handle the enhanced switch properly
    public void render(@NonNull final RenderingContext rc) throws UserException {
        final var renderStartTime = LocalDateTime.now();

        final List<Long[]> spanList = new ArrayList<>();
        final var wpMap = new TreeMap<Long, Point2D>();
        parseGPX(spanList, wpMap, rc);

        final var userSpecifiedWidth = cfg.getWidth() != null;
        final var width = userSpecifiedWidth ? cfg.getWidth() : 800;
        final var zoom = calculateZoomFactor(rc, width);
        final var scale = calculateScaleFactor(width, zoom);

        calculateMinMaxValues(userSpecifiedWidth, width, scale);

        timePointMapListList.forEach(timePointMapList -> timePointMapList
                            .forEach(timePointMap -> translateCoordinatesToZeroZero(scale, timePointMap)));
        translateCoordinatesToZeroZero(scale, wpMap);

        final var frameFilePattern = cfg.getOutput().toString();
        //noinspection MagicCharacter
        final var dot = frameFilePattern.lastIndexOf('.');
        final var ext = dot == -1 ? null : frameFilePattern.substring(dot + 1).toLowerCase(Locale.getDefault());
        final var toImages = ext != null && (isEqual("png", ext) || isEqual("jpg", ext)); //NON-NLS

        final var realWidth = calculateRealWidth(userSpecifiedWidth, scale, toImages);
        final var realHeight = calculateRealHeight(scale, toImages);

        var viewportWidth = cfg.getViewportWidth() == null ? realWidth : cfg.getViewportWidth();
        if (viewportWidth > realWidth) {
            viewportWidth = realWidth;
        }
        var viewportHeight = cfg.getViewportHeight() == null ? realHeight : cfg.getViewportHeight();
        if (viewportHeight > realHeight) {
            viewportHeight = realHeight;
        }

        LOGGER.info("{} x {}; {} x {}; {}", realWidth, realHeight, viewportWidth, viewportHeight, scale);

        final var frameWriter = cfg.isPreview() ? new NullFrameWriter() : toImages
                ? new FileFrameWriter(frameFilePattern, ext, cfg.getFps())
                : new VideoFrameWriter(cfg.getOutput(), cfg.getVideoCodec(), cfg.getFps(), viewportWidth, viewportHeight);

        final var bi = createBufferedImage(realWidth, realHeight, zoom);

        final var plugins = PluginUtil.getAvailablePlugins(cfg, frameWriter, rc);
        final var frames = calculateSpeedupAndReturnFrames(plugins);
        final var metadata = new Metadata(zoom, minX, maxX, minY, maxY, minTime, maxTime, speedup, frames);

        for (final var plugin : plugins) {
            plugin.setMetadata(metadata);
        }

        drawBackground(plugins, bi);
        preDrawTracks(bi, frames);
        renderFrames(plugins, bi, realWidth, realHeight, viewportWidth, viewportHeight, frameWriter, frames, spanList, wpMap, rc, renderStartTime);

        frameWriter.close();

        final var renderFinishTime = LocalDateTime.now();
        final var runtimeSeconds = ChronoUnit.SECONDS.between(renderStartTime, renderFinishTime);

        if (!rc.isCancelled1()) {
            rc.setProgress1(100, "Finished in %d seconds".formatted(runtimeSeconds)); // TODO i18n
            if (!cfg.isPreview()) {
                if (toImages) {
                    LOGGER.info("Done in {} seconds. Images written to {}", runtimeSeconds, frameFilePattern);
                } else {
                    LOGGER.info("Done in {} seconds. Movie written to {}", runtimeSeconds, cfg.getOutput());
                }
            }
            for (final var plugin : plugins) {
                plugin.renderingFinished();
            }
        } else {
            LOGGER.info("Canceled after {} seconds.", runtimeSeconds);
            for (final var plugin : plugins) {
                plugin.renderingCanceled();
            }
        }
    }

    private void calculateMinMaxValues(final boolean userSpecifiedWidth,
                                       final int width,
                                       final double scale) {
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
    }

    @SuppressWarnings({ "ParameterNumber", "java:S107", "java:S3776" }) // TODO refactoring in progress
    private void renderFrames(@NonNull final List<RendererPlugin> plugins,
                              @NonNull final BufferedImage bi,
                              final int realWidth,
                              final int realHeight,
                              final int viewportWidth,
                              final int viewportHeight,
                              @NonNull final FrameWriter frameWriter,
                              final int frames,
                              @NonNull final List<Long[]> spanList,
                              @NonNull final TreeMap<Long, Point2D> wpMap,
                              @NonNull final RenderingContext rc,
                              @NonNull final LocalDateTime renderStartTime) throws UserException {
        final var remainingTimeCalculator = new RemainingTimeCalculator(renderStartTime, frames);
        final var stopAfterFrame = cfg.getPreviewLength() == null
                ? cfg.isPreview() ? 1 : Long.MAX_VALUE
                : cfg.getPreviewLength() * cfg.getFps() / 1_000;
        BufferedImage lastRenderedFrame = null;
        var skip = -1f;
        for (var frame = 1; frame <= frames; frame++) {
            if (rc.isCancelled1()) {
                return;
            }

            if (frame > stopAfterFrame) {
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
                        String.format(resourceBundle.getString("renderer.progress.unusedframes"), frame, frames));
                skip = 1f;
                continue;
            }

            final var pct = (int) (100.0 * frame / frames);
            rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.frame"),
                    frame, frames, remainingTimeCalculator.getSecondsLeft(frame)));

            paint(bi, frame, 0, null, false);
            final var bi2 = Utils.deepCopy(bi);
            paint(bi2, frame, cfg.getTailDuration(), cfg.getTailColor(), false);
            drawWaypoints(bi2, frame, wpMap);

            final var marker = drawMarker(bi2, frame);

            skip = renderFlashback(skip, bi2);

            // apply viewport over bi2 (which could be the full viewport)
            final var viewportImage = applyViewport(bi2, marker, realWidth, realHeight, viewportWidth, viewportHeight);

            for (final var plugin : plugins) {
                plugin.renderFrame(frame, marker, viewportImage);
            }

            frameWriter.addFrame(viewportImage);

            if (frame == 1) { // NOPMD - AvoidLiteralsInIfCondition - this number never changes
                keepFrame(plugins, rc, frameWriter, viewportImage, frame, wpMap, cfg.getKeepFirstFrame());
            }

            lastRenderedFrame = viewportImage;
        }
        keepFrame(plugins, rc, frameWriter, lastRenderedFrame, frames, wpMap, cfg.getKeepLastFrame());
    }

    private int calculateSpeedupAndReturnFrames(@NonNull final List<RendererPlugin> plugins) throws UserException {
        final var totalTime = cfg.getTotalTime() == null ? 0 : cfg.getTotalTime();
        final var tailDuration = cfg.isTailColorFadeout() ? cfg.getTailDuration() : 0;
        final var fps = cfg.getFps();

        if (totalTime == 0) {
            speedup = cfg.getSpeedup();
            return (int) ((maxTime + tailDuration - minTime) * fps / (MS * speedup));
        } else {
            final var keepFirstFrame = cfg.getKeepFirstFrame() == null ? 0 : cfg.getKeepFirstFrame();
            final var keepLastFrame = cfg.getKeepLastFrame() == null ? 0 : cfg.getKeepLastFrame();
            final var animationTime = totalTime - keepFirstFrame - keepLastFrame;
            final var pluginFrames = plugins.stream().mapToInt(RendererPlugin::getAdditionalFrameCount).sum();
            final var frames = (int) Math.round(animationTime / MS * fps) - pluginFrames;
            speedup = (maxTime * fps + tailDuration * fps - minTime * fps) / (frames * MS);

            if (frames <= 0) {
                final var addTime = (Math.abs(frames) + 1) / fps;
                throw new UserException(resourceBundle.getString("renderer.error.addtime").formatted(addTime));
            }

            return frames;
        }
    }

    private void preDrawTracks(@NonNull final BufferedImage bi,
                               final int frames) {
        if (cfg.isPreDrawTrack()) {
            paint(bi, frames, getTime(frames) - getTime(0), null, true);
        }
    }

    private BufferedImage createBufferedImage(final int width,
                                              final int height,
                                              @Nullable final Integer zoom) throws UserException {
        try {
            return new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
        } catch (final IllegalArgumentException | NegativeArraySizeException e) {
            // these exceptions only occour when the dimension for the BufferedImage are too height
            throw new UserException(resourceBundle.getString("renderer.error.mapsize").formatted(width, height, zoom));
        }
    }

    private BufferedImage applyViewport(@NonNull final BufferedImage bi,
                                        @Nullable final Point2D marker,
                                        final int realWidth,
                                        final int realHeight,
                                        final int viewportWidth,
                                        final int viewportHeight) {
        if (marker == null || (viewportHeight == realHeight && viewportWidth == realWidth)) {
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
        final var xAvg = recentMarkersXSum / this.recentMarkers.size();
        final var yAvg = recentMarkersYSum / this.recentMarkers.size();

        // top-left (x,y) coords of viewport with boundaries protected
        var x = xAvg - viewportWidth / 2.0;
        var y = yAvg - viewportHeight / 2.0;
        if (x < 0) {
            x = 0;
        } else if ((x + viewportWidth) > realWidth) {
            x = (double) realWidth - viewportWidth;
        }

        if (y < 0) {
            y = 0;
        } else if ((y + viewportHeight) > realHeight) {
            y = (double) realHeight - viewportHeight;
        }
        return bi.getSubimage((int) x, (int) y, viewportWidth, viewportHeight);
    }

    private float renderFlashback(final float skip,
                                  @NonNull final BufferedImage bi2) {
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

    private void drawBackground(@NonNull final List<RendererPlugin> plugins,
                                @NonNull final BufferedImage bi)
            throws UserException {
        for (final var plugin : plugins) {
            plugin.renderBackground(bi);
        }
    }

    private void parseGPX(@NonNull final List<Long[]> spanList,
                          @NonNull final TreeMap<Long, Point2D> wpMap, @NonNull final RenderingContext rc) throws UserException {
        var trackIndex = -1;
        for (final var trackConfiguration : cfg.getTrackConfigurationList()) {
            trackIndex++;

            final var inputGpxFile = trackConfiguration.getInputGpx();
            rc.setProgress1(0, "Parsing GPX file \"%s\"...".formatted(inputGpxFile.getName())); // TODO i18n
            final var gch = new GpxContentHandler();
            GpxParser.parseGpx(inputGpxFile, gch);

            final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<>();

            final var track = gch.getTrack();
            final var trackSegments = track != null ? track.getTrackSegments() : null;
            if (trackSegments == null || trackSegments.isEmpty()
                    || trackSegments.stream().map(TrackSegment::getTrackPoints).mapToInt(List::size).sum() == 0) {
                throw new UserException(resourceBundle.getString("renderer.error.notrack").formatted(inputGpxFile));
            }
            for (final var trackSegment : trackSegments) {
                final var timePointMap = new TreeMap<Long, Point2D>();
                toTimePointMap(timePointMap, trackIndex, trackSegment.getTrackPoints(), Long.MIN_VALUE);
                trimGpxData(timePointMap, trackConfiguration);
                timePointMapList.add(timePointMap);
                var oldestTimeAsDefaultForWaypoints = timePointMap.keySet().stream().min(Long::compareTo).orElse(Long.MIN_VALUE);
                toTimePointMap(wpMap, trackIndex, gch.getWayPoints(), oldestTimeAsDefaultForWaypoints);
                mergeConnectedSpans(spanList, timePointMap);
            }

            Collections.reverse(timePointMapList); // reversing because of last known location drawing
            timePointMapListList.add(timePointMapList);
        }
    }

    private int calculateRealHeight(final double scale,
                                    final boolean toImages) {
        var realHeight = (int) Math.round(((maxY - minY) * scale));
        if (realHeight % 2 != 0 && cfg.getHeight() == null && !toImages) {
            realHeight++;
        }
        return realHeight;
    }

    private int calculateRealWidth(final boolean userSpecifiedWidth,
                                   final double scale,
                                   final boolean toImages) {
        var realWidth = (int) Math.round(((maxX - minX) * scale));
        if (realWidth % 2 != 0 && !userSpecifiedWidth && !toImages) {
            realWidth++;
        }
        return realWidth;
    }

    private void translateCoordinatesToZeroZero(final double scale,
                                                @NonNull final TreeMap<Long, Point2D> timePointMap) {
        if (!timePointMap.isEmpty()) {
            maxTime = Math.max(maxTime, timePointMap.lastKey());
            minTime = Math.min(minTime, timePointMap.firstKey());

            for (final var point : timePointMap.values()) {
                point.setLocation((point.getX() - minX) * scale, (maxY - point.getY()) * scale);
            }
        }
    }

    private void mergeConnectedSpans(@NonNull final List<Long[]> spanList,
                                     @NonNull final TreeMap<Long, Point2D> timePointMap) {
        if (timePointMap.isEmpty()) {
            return;
        }

        long t0 = timePointMap.firstKey();
        var t1 = timePointMap.lastKey() + (cfg.isTailColorFadeout() ? cfg.getTailDuration() : 0);

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

    private Integer calculateZoomFactor(@NonNull final RenderingContext rc,
                                        final int width) {
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

    private double calculateScaleFactor(final int width,
                                        final Integer zoom) {
        return zoom == null
                ? (width - cfg.getMargin() * 2) / (maxX - minX)
                : (128.0 * (1 << zoom)) / Math.PI;
    }

    private void trimGpxData(@NonNull final TreeMap<Long, Point2D> timePointMap,
                             @NonNull final TrackConfiguration trackConfiguration) {

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

    private void keepFrame(@NonNull final List<RendererPlugin> plugins,
                           @NonNull final RenderingContext rc,
                           @NonNull final FrameWriter frameWriter,
                           @Nullable final BufferedImage bi,
                           final int frames,
                           @NonNull final TreeMap<Long, Point2D> wpMap,
                           @Nullable final Long keepFrame) throws UserException {
        if (bi != null && keepFrame != null && keepFrame > 0) {
            drawWaypoints(bi, frames, wpMap);
            final var marker = drawMarker(bi, frames);

            for (final var plugin : plugins) {
                plugin.renderFrame(frames, marker, bi);
            }

            final var fps = Math.round(cfg.getFps());
            final var stillFrames = keepFrame / 1_000 * fps;
            for (long stillFrame = 0; stillFrame < stillFrames; stillFrame++) {
                final var pct = (int) (100.0 * stillFrame / stillFrames);
                rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.keepframe"), stillFrame, stillFrames));
                frameWriter.addFrame(bi);
                if (rc.isCancelled1()) {
                    return;
                }
            }
        }
    }

    private void drawWaypoints(@NonNull final BufferedImage bi,
                               final int frame,
                               @NonNull final TreeMap<Long, Point2D> wpMap) {
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

                printText(g2, ((NamedPoint) p).getName(), (float) p.getX() + 8f, (float) p.getY() + 4f, cfg.getWaypointFont());
            }
        }
    }

    private Ellipse2D.Double createMarker(@NonNull final Double size,
                                          @NonNull final Point2D point) {
        return new Ellipse2D.Double(point.getX() - size / 2.0, point.getY() - size / 2.0, size, size);
    }

    private void toTimePointMap(@NonNull final TreeMap<Long, Point2D> timePointMap,
                                final int trackIndex,
                                @NonNull final List<? extends MyPoint> gpxPoints,
                                final long defaultTimeIfMissing) throws UserException {
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
        if (minLat != null) {
            minY = latToY(minLat);
        }
        if (maxLat != null) {
            maxY = latToY(maxLat);
        }

        for (final var gpxPoint : gpxPoints) {
            final var x = lonToX(gpxPoint.getLongitude());
            final var y = latToY(gpxPoint.getLatitude());

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
                time = gpxPoint.getTime() == Long.MIN_VALUE ? defaultTimeIfMissing : gpxPoint.getTime();
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
            if (gpxPoint instanceof WayPoint wayPoint) {
                final var namedPoint = new NamedPoint();
                namedPoint.setLocation(x, y);
                namedPoint.setName(wayPoint.getName());
                point = namedPoint;
            } else if (gpxPoint instanceof TrackPoint trackPoint) {
                point = new GpxPoint(x, y, trackPoint, time, trackPoint.getSpeed());
            } else {
                point = null;
            }

            if (point != null) {
                // hack to prevent overwriting existing (way)point with same time
                var freeTime = time;
                while (timePointMap.containsKey(freeTime)) {
                    freeTime++;
                }
                timePointMap.put(freeTime, point);
            }
        }
    }

    private Point2D drawMarker(@NonNull final BufferedImage bi,
                               final int frame) throws UserException {
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
                    printText(g2, label, (float) point.getX() + 8f, (float) point.getY() + 4f, cfg.getFont());
                }

                continue outer; // NOPMD -- Continue the outer loop, not the inner one
            }
        }
        return point;
    }

    private void drawSimpleCircleOnGraphics2D(@NonNull final Point2D point,
                                              @NonNull final Graphics2D g2) {

        final double markerSize = cfg.getMarkerSize();

        final var marker = createMarker(markerSize, point);
        g2.setStroke(new BasicStroke(1f));
        g2.fill(marker);
        g2.setColor(Color.black);
        g2.draw(marker);
    }

    private void drawIconOnGraphics2D(@NonNull final Point2D point,
                                      @NonNull final Graphics2D g2,
                                      @NonNull final TrackIcon trackIcon,
                                      final boolean mirrorTrackIcon)
            throws IOException {
        final var trackIconImage = ImageIO.read(requireNonNull(getClass().getResource(trackIcon.getFilename())));
        drawImageOnGraphics2D(point, g2, trackIconImage, mirrorTrackIcon);
    }

    private void drawIconFileOnGraphics2D(@NonNull final Point2D point,
                                          @NonNull final Graphics2D g2,
                                          @NonNull final File trackIconFile,
                                          final boolean mirrorTrackIcon)
            throws IOException {
        final var trackIconImage = ImageIO.read(trackIconFile);
        drawImageOnGraphics2D(point, g2, trackIconImage, mirrorTrackIcon);
    }

    private void drawImageOnGraphics2D(@NonNull final Point2D point,
                                       @NonNull final Graphics2D g2,
                                       @NonNull final BufferedImage trackIconImage,
                                       final boolean mirrorTrackIcon)
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

    private void paint(@NonNull final BufferedImage bi,
                       final int frame,
                       final long backTime,
                       @Nullable final Color overrideColor,
                       final boolean isPreDrawTrack) {
        final var g2 = getGraphics(bi);

        final var time = getTime(frame);

        final var trackConfigurationList = cfg.getTrackConfigurationList();

        var i = 0;
        for (final var timePointMapList : timePointMapListList) {
            final var trackConfiguration = trackConfigurationList.get(i++);

            for (final var timePointMap : timePointMapList) {
                final var lineWidth = isPreDrawTrack ? trackConfiguration.getPreDrawLineWidth() : trackConfiguration.getLineWidth();
                g2.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

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
                    if (!cfg.isTailColorFadeout() && toTime == maxTime) {
                        continue;
                    }
                    for (final var entry : timePointMap.subMap(toTime - backTime, true, toTime, true).entrySet()) {
                        if (prevPoint != null) {
                            var drawSegment = false;
                            if (isPreDrawTrack) {
                                g2.setColor(trackConfiguration.getPreDrawTrackColor());
                                drawSegment = true;
                            } else {
                                final var ratio = (backTime - time + entry.getKey()) * 1f / backTime;
                                if (overrideColor != null && ratio > 0) {
                                    g2.setPaint(blendTailColor(trackConfiguration.getColor(), overrideColor, ratio));
                                    drawSegment = true;
                                }
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
        return RenderUtil.getTime(frame, minTime, cfg.getFps(), speedup);
    }

    private void printText(@NonNull final Graphics2D g2,
                           @Nullable final String text,
                           final float x,
                           final float y,
                           @NonNull final Font font) {
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

        @Override
        @SuppressFBWarnings("NP_METHOD_PARAMETER_TIGHTENS_ANNOTATION")
        public boolean equals(@Nullable final Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            if (!super.equals(o)) {
                return false;
            }

            final var that = (NamedPoint) o;
            return Objects.equals(name, that.name);
        }

        @Override
        public int hashCode() {
            int result = super.hashCode();
            result = 31 * result + (name != null ? name.hashCode() : 0);
            return result;
        }
    }

    private static class RemainingTimeCalculator {

        private final NumberFormat numberFormat;
        private final LocalDateTime renderStartTime;
        private final long frames;

        private long lastRuntime;
        private String lastResult;

        RemainingTimeCalculator(@NonNull final LocalDateTime renderStartTime,
                                final long frames) {
            this.numberFormat = NumberFormat.getNumberInstance(Locale.getDefault());
            this.renderStartTime = renderStartTime;
            this.frames = frames;
        }

        private String getSecondsLeft(final int frame) {
            final var runtime = ChronoUnit.SECONDS.between(renderStartTime, LocalDateTime.now());
            if (runtime > lastRuntime) {
                lastRuntime = runtime;
                final var fps = runtime > 10 ? frame / runtime : 0; // 10 seconds warmup
                final var secondsLeft = fps > 0 ? (frames - frame) / fps : -1;
                lastResult = secondsLeft == -1 ? "?" : numberFormat.format(secondsLeft);
            }
            return lastResult;
        }


    }
}
