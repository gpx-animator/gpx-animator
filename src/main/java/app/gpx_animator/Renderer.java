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

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import app.gpx_animator.frameWriter.FileFrameWriter;
import app.gpx_animator.frameWriter.FrameWriter;
import app.gpx_animator.frameWriter.VideoFrameWriter;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.ResourceBundle;
import java.util.TreeMap;

import static app.gpx_animator.Utils.isEqual;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class Renderer {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Renderer.class);

    private static final double MS = 1000d;

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final java.util.Map<Integer, Long> speedValues = new HashMap<>();

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
    
    private String topLeft = "Top Left";
    private String topCenter = "Top Center";
    private String topRight = "Top Right";
    private String bottomLeft = "Bottom Left";
    private String bottomCenter = "Bottom Center";
    private String bottomRight = "Bottom Right";

    private double speedup;

    public Renderer(final Configuration cfg) {
        this.cfg = cfg;
    }

    private GpxPoint lastSpeedPoint = null;

    private long calculateSpeedForDisplay(final GpxPoint point, final int frame) {
        final long speed = calculateSpeed(point, getTime(frame));
        speedValues.put(frame, speed);

        final long deleteBefore = frame - (Math.round(cfg.getFps())); // 1 second
        speedValues.keySet().removeIf((f) -> f < deleteBefore);

        return Math.round(speedValues.values().stream().mapToLong(Long::longValue).average().orElse(0));
    }

    private long calculateSpeed(final GpxPoint point, final long time) {
        final long timeout = time - 1_000 * 60; // 1 minute
        final long distance = calculateDistance(lastSpeedPoint, point);
        final double timeDiff = lastSpeedPoint == null ? 0 : point.getTime() - lastSpeedPoint.getTime();

        final long speed;
        if (distance > 0 && point.getTime() > timeout) {
            speed = Math.round((3_600 * distance) / timeDiff);
        } else {
            speed = 0;
        }

        lastSpeedPoint = point;
        return speed;
    }

    private static long calculateDistance(final GpxPoint point1, final GpxPoint point2) {
        if (point1 == null) {
            return 0;
        }

        final double lat1 = point1.getLatLon().getLat();
        final double lon1 = point1.getLatLon().getLon();
        final double lat2 = point2.getLatLon().getLat();
        final double lon2 = point2.getLatLon().getLon();

        if ((lat1 == lat2) && (lon1 == lon2)) {
            return 0;
        } else {
            final double theta = lon1 - lon2;
            final double dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            final double arcCosine = Math.acos(dist);
            final double degrees = Math.toDegrees(arcCosine);
            final double mi = degrees * 60 * 1.1515; // to miles
            final double km = mi * 1.609344; // to kilometers
            final double m = km * 1_000; // to meters
            return Math.round(m); // round to full meters
        }
    }

    private static double lonToX(final Double maxLon) {
        return Math.toRadians(maxLon);
    }

    private static double latToY(final double lat) {
        return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
    }

    private static Color blendTailColor(final Color tailColor, final Color trackColor, final float ratio) {
        double r = ((double) (1 - ratio)) * tailColor.getRed() + (double) ratio * trackColor.getRed();
        double g = ((double) (1 - ratio)) * tailColor.getGreen() + (double) ratio * trackColor.getGreen();
        double b = ((double) (1 - ratio)) * tailColor.getBlue() + (double) ratio * trackColor.getBlue();
        double a = Math.max(tailColor.getAlpha(), trackColor.getAlpha());

        return new Color((int) r, (int) g, (int) b, (int) a);
    }

    public void render(final RenderingContext rc) throws UserException {
        final List<Long[]> spanList = new ArrayList<>();
        final TreeMap<Long, Point2D> wpMap = new TreeMap<>();
        parseGPX(spanList, wpMap);

        final boolean userSpecifiedWidth = cfg.getWidth() != null;
        final int width = userSpecifiedWidth ? cfg.getWidth() : 800;
        final Integer zoom = calculateZoomFactor(rc, width);
        final double scale = calculateScaleFactor(width, zoom);

        minX -= cfg.getMargin() / scale;
        maxX += cfg.getMargin() / scale;
        minY -= cfg.getMargin() / scale;
        maxY += cfg.getMargin() / scale;

        if (userSpecifiedWidth) {
            final double ww = width - (maxX - minX) * scale;
            minX -= ww / scale / 2.0;
            maxX += ww / scale / 2.0;
        }

        if (cfg.getHeight() != null) {
            final double hh = cfg.getHeight() - (maxY - minY) * scale;
            minY -= hh / scale / 2.0;
            maxY += hh / scale / 2.0;
        }

        timePointMapListList.forEach((timePointMapList) -> timePointMapList
                            .forEach((timePointMap) -> translateCoordinatesToZeroZero(scale, timePointMap)));
        translateCoordinatesToZeroZero(scale, wpMap);

        final String frameFilePattern = cfg.getOutput().toString();
        //noinspection MagicCharacter
        final int dot = frameFilePattern.lastIndexOf('.');
        final String ext = dot == -1 ? null : frameFilePattern.substring(dot + 1).toLowerCase(Locale.getDefault());
        final boolean toImages = ext != null && (isEqual("png", ext) || isEqual("jpg", ext)); //NON-NLS

        final int realWidth = calculateRealWidth(userSpecifiedWidth, scale, toImages);
        final int realHeight = calculateRealHeight(scale, toImages);
        LOGGER.info("{}x{};{}", realWidth, realHeight, scale);

        final FrameWriter frameWriter = toImages
                ? new FileFrameWriter(frameFilePattern, ext, cfg.getFps())
                : new VideoFrameWriter(cfg.getOutput(), cfg.getFps(), realWidth, realHeight);

        final BufferedImage bi = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_3BYTE_BGR);
        final Graphics2D ga = (Graphics2D) bi.getGraphics();
        drawBackground(rc, zoom, bi, ga);

        if (cfg.getFontSize() > 0) {
            font = new Font(Font.MONOSPACED, Font.PLAIN, cfg.getFontSize()); // TODO https://github.com/zdila/gpx-animator/issues/154
            fontMetrics = ga.getFontMetrics(font);
        }

        speedup = cfg.getTotalTime() == null ? cfg.getSpeedup() : 1.0 * (maxTime - minTime) / cfg.getTotalTime();
        final int frames = (int) ((maxTime + cfg.getTailDuration() - minTime) * cfg.getFps() / (MS * speedup));
        final Photos photos = new Photos(cfg.getPhotoDirectory());

        float skip = -1f;
        for (int frame = 1; frame < frames; frame++) {
            if (rc.isCancelled1()) {
                return;
            }

            final Long time = getTime(frame);
            skip:
            if (cfg.isSkipIdle()) {
                for (final Long[] span : spanList) {
                    if (span[0] <= time && span[1] >= time) {
                        break skip;
                    }
                }
                rc.setProgress1((int) (100.0 * frame / frames),
                        String.format(resourceBundle.getString("renderer.progress.unusedframes"), frame, (frames - 1)));
                skip = 1f;
                continue;
            }

            final int pct = (int) (100.0 * frame / frames);
            rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.frame"), frame, (frames - 1)));

            paint(bi, frame, 0, null);
            final BufferedImage bi2 = Utils.deepCopy(bi);
            paint(bi2, frame, cfg.getTailDuration(), cfg.getTailColor());
            drawWaypoints(bi2, frame, wpMap);

            final Point2D marker = drawMarker(bi2, frame);
            if (font != null) {
                drawInfo(bi2, frame, marker);

                String att = resourceBundle.getString("configuration.attribution");
                String getAt = cfg.getAttribution();
                if (att.equals(getAt)) {
                    drawAttribution(bi2, att.replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION).replace("%MAP_ATTRIBUTION%", ""));
                } else {
                    drawAttribution(bi2, getAt);
                }
            }

            skip = renderFlashback(skip, bi2);
            frameWriter.addFrame(bi2);
            photos.render(time, cfg, bi2, frameWriter, rc, pct);
        }

        keepLastFrame(rc, frameWriter, bi, frames);
        frameWriter.close();

        if (!rc.isCancelled1()) {
            rc.setProgress1(100, "Finished");
            LOGGER.info("Done.");
        } else {
            LOGGER.info("Canceled.");
        }
    }

    private float renderFlashback(final float skip, final BufferedImage bi2) {
        final Color flashbackColor = cfg.getFlashbackColor();
        if (skip > 0f && flashbackColor.getAlpha() > 0 && cfg.getFlashbackDuration() != null && cfg.getFlashbackDuration() > 0) {
            final Graphics2D g2 = (Graphics2D) bi2.getGraphics();
            g2.setColor(new Color(flashbackColor.getRed(), flashbackColor.getGreen(), flashbackColor.getBlue(),
                    (int) (flashbackColor.getAlpha() * skip)));
            g2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());
            return (float) (skip - (1000f / cfg.getFlashbackDuration() / cfg.getFps()));
        }
        return skip;
    }

    private void drawBackground(final RenderingContext rc, final Integer zoom, final BufferedImage bi, final Graphics2D ga) throws UserException {
        if (cfg.getTmsUrlTemplate() == null) {
            final Color backgroundColor = cfg.getBackgroundColor();
            ga.setColor(backgroundColor);
            ga.fillRect(0, 0, bi.getWidth(), bi.getHeight());
        } else {
            MapUtil.drawMap(bi, cfg.getTmsUrlTemplate(), cfg.getBackgroundMapVisibility(), zoom, minX, maxX, minY, maxY, rc);
        }
        drawLogo(bi);
    }

    private void drawLogo(final BufferedImage bi) throws UserException {
        final File logo = cfg.getLogo();
        final String logoPosition = cfg.getLogoPosition();
        if (logo != null && logo.exists()) {
            final BufferedImage image;
            try {
                image = ImageIO.read(logo);
            } catch (final IOException e) {
                throw new UserException("Can't read logo: ".concat(e.getMessage()));
            }
            final Graphics2D g2 = getGraphics(bi);
            if (logoPosition.equals(topLeft)) {
                g2.drawImage(image, cfg.getMargin(), cfg.getMargin(), image.getWidth(), image.getHeight(), null);
            } else if (logoPosition.equals(topCenter)) {
                g2.drawImage(image, (bi.getWidth() - image.getWidth()) / 2, cfg.getMargin(), image.getWidth(), image.getHeight(), null);
            } else if (logoPosition.equals(topRight)) {
                g2.drawImage(image, bi.getWidth() - image.getWidth() - cfg.getMargin(), cfg.getMargin(), image.getWidth(), image.getHeight(), null);
            } else if (logoPosition.equals(bottomLeft)) {
                g2.drawImage(image, cfg.getMargin(), bi.getHeight() - image.getHeight() - cfg.getMargin(), image.getWidth(), image.getHeight(), null);
            } else if (logoPosition.equals(bottomCenter)) {
                g2.drawImage(image, (bi.getWidth() - image.getWidth()) / 2, bi.getHeight() - image.getHeight() - cfg.getMargin(), image.getWidth(),
                image.getHeight(), null);
            } else if (logoPosition.equals(bottomRight)) {
                g2.drawImage(image, bi.getWidth() - image.getWidth() - cfg.getMargin(), bi.getHeight() - image.getHeight() - cfg.getMargin(),
                image.getWidth(), image.getHeight(), null);
            }
        }
    }

    private void parseGPX(final List<Long[]> spanList, final TreeMap<Long, Point2D> wpMap) throws UserException {
        int trackIndex = -1;
        for (final TrackConfiguration trackConfiguration : cfg.getTrackConfigurationList()) {
            trackIndex++;

            final File inputGpxFile = trackConfiguration.getInputGpx();
            final GpxContentHandler gch = new GpxContentHandler();
            GpxParser.parseGpx(inputGpxFile, gch);

            final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<>();

            final List<List<LatLon>> pointLists = gch.getPointLists();
            if (pointLists.isEmpty()) {
                throw new UserException(resourceBundle.getString("renderer.error.notrack").formatted(inputGpxFile));
            }
            for (final List<LatLon> latLonList : pointLists) {
                sigmaRoxRepair(latLonList);
                final TreeMap<Long, Point2D> timePointMap = new TreeMap<>();
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
            final LatLon first = latLonList.get(0);
            final LatLon second = latLonList.get(1);
            if (first.getTime() == Long.MIN_VALUE && second.getTime() > Long.MIN_VALUE
                    && first.getLat() == second.getLat() && first.getLon() == second.getLon()) {
                latLonList.remove(0);
            }
        }
        if (latLonList.size() >= 2) {
            final LatLon last = latLonList.get(latLonList.size() - 1);
            final LatLon penultimate = latLonList.get(latLonList.size() - 2);
            if (last.getTime() == Long.MIN_VALUE && penultimate.getTime() > Long.MIN_VALUE
                    && last.getLat() == penultimate.getLat() && last.getLon() == penultimate.getLon()) {
                latLonList.remove(latLonList.size() - 1);
            }
        }
    }

    private int calculateRealHeight(final double scale, final boolean toImages) {
        int realHeight = (int) Math.round(((maxY - minY) * scale));
        if (realHeight % 2 != 0 && cfg.getHeight() == null && !toImages) {
            realHeight++;
        }
        return realHeight;
    }

    private int calculateRealWidth(final boolean userSpecifiedWidth, final double scale, final boolean toImages) {
        int realWidth = (int) Math.round(((maxX - minX) * scale));
        if (realWidth % 2 != 0 && !userSpecifiedWidth && !toImages) {
            realWidth++;
        }
        return realWidth;
    }

    private void translateCoordinatesToZeroZero(final double scale, final TreeMap<Long, Point2D> timePointMap) {
        if (!timePointMap.isEmpty()) {
            maxTime = Math.max(maxTime, timePointMap.lastKey());
            minTime = Math.min(minTime, timePointMap.firstKey());

            for (final Point2D point : timePointMap.values()) {
                point.setLocation((point.getX() - minX) * scale, (maxY - point.getY()) * scale);
            }
        }
    }

    private void mergeConnectedSpans(final List<Long[]> spanList, final TreeMap<Long, Point2D> timePointMap) {
        long t0 = timePointMap.firstKey();
        long t1 = timePointMap.lastKey() + cfg.getTailDuration();

        for (final Iterator<Long[]> iter = spanList.iterator(); iter.hasNext();) {
            final Long[] span = iter.next();
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
            final boolean userSpecifiedHeight = cfg.getHeight() != null;
            if (userSpecifiedHeight) {
                final int height = cfg.getHeight();
                final int zoom1 = (int) Math.floor(Math.log(Math.PI / 128.0 * (width - cfg.getMargin() * 2) / (maxX - minX)) / Math.log(2));
                final int zoom2 = (int) Math.floor(Math.log(Math.PI / 128.0 * (height - cfg.getMargin() * 2) / (maxY - minY)) / Math.log(2));
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

        final Long trimGpxStart = trackConfiguration.getTrimGpxStart();
        if (trimGpxStart != null && trimGpxStart > 0 && timePointMap.size() > 0) {
            final Long skipToTime = timePointMap.firstKey() + trimGpxStart;
            timePointMap.entrySet().removeIf(e -> e.getKey() < skipToTime);
        }

        final Long trimGpxEnd = trackConfiguration.getTrimGpxEnd();
        if (trimGpxEnd != null && trimGpxEnd > 0 && timePointMap.size() > 0) {
            final Long skipAfterTime = timePointMap.lastKey() - trimGpxEnd;
            timePointMap.entrySet().removeIf(e -> e.getKey() > skipAfterTime);
        }
    }

    private void keepLastFrame(final RenderingContext rc, final FrameWriter frameWriter, final BufferedImage bi, final int frames)
            throws UserException {
        final boolean keepLastFrame = cfg.getKeepLastFrame() != null && cfg.getKeepLastFrame() > 0;
        if (keepLastFrame) {
            final Point2D marker = drawMarker(bi, frames);
            if (font != null) {
                drawInfo(bi, frames, marker);

                String att = resourceBundle.getString("configuration.attribution");
                if (cfg.getAttribution().equals(att)) {
                    drawAttribution(bi, att.replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION).replace("%MAP_ATTRIBUTION%", ""));
                } else {
                    drawAttribution(bi, cfg.getAttribution());
                }
            }
            final long ms = cfg.getKeepLastFrame();
            final long fps = Double.valueOf(cfg.getFps()).longValue();
            final long stillFrames = ms / 1_000 * fps;
            for (long stillFrame = 0; stillFrame < stillFrames; stillFrame++) {
                final int pct = (int) (100.0 * stillFrame / stillFrames);
                rc.setProgress1(pct, String.format(resourceBundle.getString("renderer.progress.keeplastframe"), stillFrame, stillFrames));
                frameWriter.addFrame(bi);
                if (rc.isCancelled1()) {
                    return;
                }
            }
        }
    }

    private void drawWaypoints(final BufferedImage bi, final int frame, final TreeMap<Long, Point2D> wpMap) {
        final Double waypointSize = cfg.getWaypointSize();
        if (waypointSize == null || waypointSize == 0.0 || wpMap.isEmpty()) {
            return;
        }

        final Graphics2D g2 = getGraphics(bi);

        final long t2 = getTime(frame);


        if (t2 >= wpMap.firstKey()) {
            for (final Point2D p : wpMap.subMap(wpMap.firstKey(), t2).values()) {
                g2.setColor(Color.white);
                final Ellipse2D.Double marker = createMarker(waypointSize, p);
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

        final TrackConfiguration trackConfiguration = cfg.getTrackConfigurationList().get(trackIndex);

        final Double minLon = cfg.getMinLon();
        final Double maxLon = cfg.getMaxLon();
        final Double minLat = cfg.getMinLat();
        final Double maxLat = cfg.getMaxLat();

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

        for (final LatLon latLon : latLonList) {
            final double x = lonToX(latLon.getLon());
            final double y = latToY(latLon.getLat());

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
            final Long forcedPointInterval = trackConfiguration.getForcedPointInterval();
            if (forcedPointInterval != null) {
                forcedTime += forcedPointInterval;
                time = forcedTime;
            } else {
                time = latLon.getTime();
                if (time == Long.MIN_VALUE) {
                    final String filename = trackConfiguration.getInputGpx().getName();
                    throw new UserException(
                            String.format(resourceBundle.getString("error.missingtime"), filename));
                }
            }

            if (trackConfiguration.getTimeOffset() != null) {
                time += trackConfiguration.getTimeOffset();
            }

            final Point2D point;
            if (latLon instanceof Waypoint) {
                final NamedPoint namedPoint = new NamedPoint();
                namedPoint.setLocation(x, y);
                namedPoint.setName(((Waypoint) latLon).getName());
                point = namedPoint;
            } else {
                point = new GpxPoint(x, y, latLon, time);
            }

            // hack to prevent overwriting existing (way)point with same time
            long freeTime = time;
            while (timePointMap.containsKey(freeTime)) {
                freeTime++;
            }
            timePointMap.put(freeTime, point);
        }
    }

    private void drawInfo(final BufferedImage bi, final int frame, final Point2D marker) {
        final String dateString = dateFormat.format(getTime(frame));
        final String latLongString = getLatLonString(marker);
        final String speedString = getSpeedString(marker, frame);
        final Graphics2D graphics = getGraphics(bi);
        
        final String informationPosition = cfg.getInformationPosition();

        if (informationPosition.equals(topLeft)) {
            printText(graphics, dateString, cfg.getMargin(), cfg.getMargin() + fontMetrics.getHeight() * 2);
            printText(graphics, latLongString, cfg.getMargin(), cfg.getMargin() + fontMetrics.getHeight());
            printText(graphics, speedString, cfg.getMargin(), cfg.getMargin());
        } else if (informationPosition.equals(topCenter)) {
            printText(graphics, dateString, (float) (bi.getWidth() - fontMetrics.stringWidth(dateString)) / 2,
            cfg.getMargin() + fontMetrics.getHeight() * 2);
            printText(graphics, latLongString, (float) (bi.getWidth() - fontMetrics.stringWidth(latLongString)) / 2,
            cfg.getMargin() + fontMetrics.getHeight());
            printText(graphics, speedString, (float) (bi.getWidth() - fontMetrics.stringWidth(speedString)) / 2, cfg.getMargin());
        } else if (informationPosition.equals(topRight)) {
            printText(graphics, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.getMargin(),
            cfg.getMargin() + fontMetrics.getHeight() * 2);
            printText(graphics, latLongString, bi.getWidth() - fontMetrics.stringWidth(latLongString) - cfg.getMargin(),
            cfg.getMargin() + fontMetrics.getHeight());
            printText(graphics, speedString, bi.getWidth() - fontMetrics.stringWidth(speedString) - cfg.getMargin(), cfg.getMargin());
        } else if (informationPosition.equals(bottomLeft)) {
            printText(graphics, dateString, cfg.getMargin(), bi.getHeight() - cfg.getMargin());
            printText(graphics, latLongString, cfg.getMargin(), bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight());
            printText(graphics, speedString, cfg.getMargin(), bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight() * 2);
        } else if (informationPosition.equals(bottomCenter)) {
            printText(graphics, dateString, (float) (bi.getWidth() - fontMetrics.stringWidth(dateString)) / 2, bi.getHeight() - cfg.getMargin());
            printText(graphics, latLongString, (float) (bi.getWidth() - fontMetrics.stringWidth(latLongString)) / 2,
            bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight());
            printText(graphics, speedString, (float) (bi.getWidth() - fontMetrics.stringWidth(speedString)) / 2,
            bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight() * 2);
        } else if (informationPosition.equals(bottomRight)) {
            printText(graphics, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.getMargin(), bi.getHeight() - cfg.getMargin());
            printText(graphics, latLongString, bi.getWidth() - fontMetrics.stringWidth(latLongString) - cfg.getMargin(),
            bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight());
            printText(graphics, speedString, bi.getWidth() - fontMetrics.stringWidth(speedString) - cfg.getMargin(),
            bi.getHeight() - cfg.getMargin() - fontMetrics.getHeight() * 2);
        }
    }


    private String getSpeedString(final Point2D point, final int frame) {
        if (point instanceof GpxPoint) {
            final GpxPoint gpxPoint = (GpxPoint) point;
            final long speed = calculateSpeedForDisplay(gpxPoint, frame);
            return String.format("%d km/h", speed); //NON-NLS
        } else {
            return "";
        }
    }


    private String getLatLonString(final Point2D point) {
        if (point instanceof GpxPoint) {
            final GpxPoint gpxPoint = (GpxPoint) point;
            final LatLon latLon = gpxPoint.getLatLon();
            return String.format("%.4f, %.4f", latLon.getLat(), latLon.getLon()); //NON-NLS
        } else {
            return "";
        }
    }


    private void drawAttribution(final BufferedImage bi, final String attribution) {
        final String attributionPosition = cfg.getAttributionPosition();
        Boolean hasSplit = false;

        for (int i = 0; i < attribution.length(); i++) {
            if (attribution.contains("\n")) {
                hasSplit = true;
                break;
            }
        }

        if (hasSplit) {
            String[] lines = attribution.split("\n");
            if (attributionPosition.equals(topLeft)) {
                printText(getGraphics(bi), lines[0], cfg.getMargin(), cfg.getMargin() + fontMetrics.getHeight());
                printText(getGraphics(bi), lines[1], cfg.getMargin(), cfg.getMargin() + fontMetrics.getHeight() * 2);
            } else if (attributionPosition.equals(topCenter)) {
                printText(getGraphics(bi), lines[0], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[0])) / 2,
                cfg.getMargin() + fontMetrics.getHeight());
                printText(getGraphics(bi), lines[1], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[1])) / 2,
                cfg.getMargin() + fontMetrics.getHeight() * 2);
            } else if (attributionPosition.equals(topRight)) {
                printText(getGraphics(bi), lines[0], bi.getWidth() - fontMetrics.stringWidth(lines[0]) - cfg.getMargin(),
                cfg.getMargin() + fontMetrics.getHeight());
                printText(getGraphics(bi), lines[1], bi.getWidth() - fontMetrics.stringWidth(lines[1]) - cfg.getMargin(),
                cfg.getMargin() + fontMetrics.getHeight() * 2);
            } else if (attributionPosition.equals(bottomLeft)) {
                printText(getGraphics(bi), lines[0], cfg.getMargin(), bi.getHeight() - cfg.getMargin());
                printText(getGraphics(bi), lines[1], cfg.getMargin(), bi.getHeight() - cfg.getMargin() * 2);
            } else if (attributionPosition.equals(bottomCenter)) {
                printText(getGraphics(bi), lines[0], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[0])) / 2,
                bi.getHeight() - cfg.getMargin());
                printText(getGraphics(bi), lines[1], (float) (bi.getWidth() - fontMetrics.stringWidth(lines[1])) / 2,
                bi.getHeight() - cfg.getMargin() * 2);
            } else if (attributionPosition.equals(bottomRight)) {
                printText(getGraphics(bi), lines[0], bi.getWidth() - fontMetrics.stringWidth(lines[0]) - cfg.getMargin(),
                bi.getHeight() - cfg.getMargin());
                printText(getGraphics(bi), lines[1], bi.getWidth() - fontMetrics.stringWidth(lines[1]) - cfg.getMargin(),
                bi.getHeight() - cfg.getMargin() * 2);
            }
        } else {
            if (attributionPosition.equals(topLeft)) {
                printText(getGraphics(bi), attribution, cfg.getMargin(), cfg.getMargin() + fontMetrics.getHeight());
            } else if (attributionPosition.equals(topCenter)) {
                printText(getGraphics(bi), attribution, (float) (bi.getWidth() - fontMetrics.stringWidth(attribution)) / 2,
                cfg.getMargin() + fontMetrics.getHeight());
            } else if (attributionPosition.equals(topRight)) {
                printText(getGraphics(bi), attribution, bi.getWidth() - fontMetrics.stringWidth(attribution) - cfg.getMargin(),
            cfg.getMargin() + fontMetrics.getHeight());
            } else if (attributionPosition.equals(bottomLeft)) {
                printText(getGraphics(bi), attribution, cfg.getMargin(), bi.getHeight() - cfg.getMargin());
            } else if (attributionPosition.equals(bottomCenter)) {
                printText(getGraphics(bi), attribution, (float) (bi.getWidth() - fontMetrics.stringWidth(attribution)) / 2,
                bi.getHeight() - cfg.getMargin());
            } else if (attributionPosition.equals(bottomRight)) {
                printText(getGraphics(bi), attribution, bi.getWidth() - fontMetrics.stringWidth(attribution) - cfg.getMargin(),
                bi.getHeight() - cfg.getMargin());
            }
        }
    }

    
    private Point2D drawMarker(final BufferedImage bi, final int frame) {
        if (cfg.getMarkerSize() == null || cfg.getMarkerSize() == 0.0) {
            return null;
        }

        Point2D point = null;

        final Graphics2D g2 = getGraphics(bi);
        final long t2 = getTime(frame);
        final List<TrackConfiguration> trackConfigurationList = cfg.getTrackConfigurationList();

        int i = 0;
        outer:
        for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
            final TrackConfiguration trackConfiguration = trackConfigurationList.get(i++);
            for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
                final Entry<Long, Point2D> ceilingEntry = timePointMap.ceilingEntry(t2);
                final Entry<Long, Point2D> floorEntry = timePointMap.floorEntry(t2);
                if (floorEntry == null) {
                    continue;
                }

                point = floorEntry.getValue();
                if (t2 - floorEntry.getKey() <= cfg.getTailDuration()) {

                    g2.setColor(ceilingEntry == null ? Color.white : trackConfiguration.getColor());

                    final TrackIcon trackIcon = trackConfiguration.getTrackIcon();
                    if (trackIcon == null || trackIcon.getKey().isEmpty()) {
                        drawSimpleCircleOnGraphics2D(point, g2);
                    } else {
                        try {
                            drawIconOnGraphics2D(point, g2, trackIcon);
                        } catch (final IOException e) {
                            drawSimpleCircleOnGraphics2D(point, g2);
                        }
                    }

                    final String label = trackConfiguration.getLabel();
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

        final Ellipse2D.Double marker = createMarker(markerSize, point);
        g2.setStroke(new BasicStroke(1f));
        g2.fill(marker);
        g2.setColor(Color.black);
        g2.draw(marker);
    }

    private void drawIconOnGraphics2D(final Point2D point, final Graphics2D g2, final TrackIcon trackIcon) throws IOException {
        final BufferedImage icon = ImageIO.read(getClass().getResource(trackIcon.getFilename()));
        final AffineTransform at = new AffineTransform();
        at.translate((int) point.getX() + 8f, (int) point.getY() + 4f);
        at.translate(-icon.getWidth() / 2d, -icon.getHeight() / 2d);
        g2.drawImage(icon, at, null);
    }


    private void paint(final BufferedImage bi, final int frame, final long backTime, final Color overrideColor) {
        final Graphics2D g2 = getGraphics(bi);

        final long time = getTime(frame);

        final List<TrackConfiguration> trackConfigurationList = cfg.getTrackConfigurationList();

        int i = 0;
        for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
            final TrackConfiguration trackConfiguration = trackConfigurationList.get(i++);

            for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
                g2.setStroke(new BasicStroke(trackConfiguration.getLineWidth(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

                final Long toTime = timePointMap.floorKey(time);

                if (toTime == null) {
                    continue;
                }

                Point2D prevPoint = null;

                if (backTime == 0) {
                    final long prevTime = getTime(frame - 1);
                    Long fromTime = timePointMap.floorKey(prevTime);
                    if (fromTime == null) {
                        // try ceiling because we may be at beginning
                        fromTime = timePointMap.ceilingKey(prevTime);
                    }
                    if (fromTime == null) {
                        continue;
                    }

                    g2.setPaint(trackConfiguration.getColor());
                    for (final Entry<Long, Point2D> entry : timePointMap.subMap(fromTime, true, toTime, true).entrySet()) {
                        if (prevPoint != null) {
                            g2.draw(new Line2D.Double(prevPoint, entry.getValue()));
                        }
                        prevPoint = entry.getValue();
                    }
                } else {
                    for (final Entry<Long, Point2D> entry : timePointMap.subMap(toTime - backTime, true, toTime, true).entrySet()) {
                        if (prevPoint != null) {
                            final float ratio = (backTime - time + entry.getKey()) * 1f / backTime;
                            if (ratio > 0) {
                                g2.setPaint(blendTailColor(trackConfiguration.getColor(), overrideColor, ratio));
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
        final FontRenderContext frc = g2.getFontRenderContext();
        g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        final int height = g2.getFontMetrics(font).getHeight();

        final String[] lines = text == null ? new String[0] : text.split("\n");
        float yy = y - (lines.length - 1) * height;
        for (final String line : lines) {
            if (!line.isEmpty()) {
                final TextLayout tl = new TextLayout(line, font, frc);
                final Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x, yy));
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
        final Graphics2D g2 = (Graphics2D) bi.getGraphics();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return g2;
    }

    private static class NamedPoint extends Point2D.Double {
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
