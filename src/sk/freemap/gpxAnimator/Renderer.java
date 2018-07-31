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
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import sk.freemap.gpxAnimator.frameWriter.FileFrameWriter;
import sk.freemap.gpxAnimator.frameWriter.FrameWriter;
import sk.freemap.gpxAnimator.frameWriter.VideoFrameWriter;


public class Renderer {

	private static final double MS = 1000d;
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private final Configuration cfg;

	private final List<List<TreeMap<Long, Point2D>>> timePointMapListList = new ArrayList<List<TreeMap<Long,Point2D>>>();

	private Font font;
	private FontMetrics fontMetrics;

	private long minTime = Long.MAX_VALUE;
	private double minX = Double.POSITIVE_INFINITY, maxX = Double.NEGATIVE_INFINITY, minY = Double.POSITIVE_INFINITY, maxY = Double.NEGATIVE_INFINITY;

	private double speedup;


	public Renderer(final Configuration cfg) throws UserException {
		this.cfg = cfg;
	}


	public void render(final RenderingContext rc) throws UserException {
		final List<Long[]> spanList = new ArrayList<Long[]>();

		final TreeMap<Long, Point2D> wpMap = new TreeMap<Long, Point2D>();

		int i = -1;
		for (final TrackConfiguration trackConfiguration : cfg.getTrackConfigurationList()) {
			i++;

			final GpxContentHandler gch = new GpxContentHandler();

			GpxParser.parseGpx(trackConfiguration.getInputGpx(), gch);

			final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<TreeMap<Long, Point2D>>();

			for (final List<LatLon> latLonList : gch.getPointLists()) {
				final TreeMap<Long, Point2D> timePointMap = new TreeMap<Long,Point2D>();
				toTimePointMap(timePointMap, i, latLonList);
				timePointMapList.add(timePointMap);

				toTimePointMap(wpMap, i, gch.getWaypointList());

				Long t0 = timePointMap.firstKey();
				Long t1 = timePointMap.lastKey() + cfg.getTailDuration();
				test: { // code in the block merges connected spans; it is currently not important to do this
					for (final Iterator<Long[]> iter = spanList.iterator(); iter.hasNext(); ) {
						final Long[] span = iter.next();
						if (t0 > span[0] && t1 < span[1]) {
							// swallowed
							break test;
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

					spanList.add(new Long[] { t0, t1 });
				}
			}
			Collections.reverse(timePointMapList); // reversing because of last known location drawing
			timePointMapListList.add(timePointMapList);
		}

		final boolean userSpecifiedWidth = cfg.getWidth() != null;
		final int width = userSpecifiedWidth ? cfg.getWidth() : 800;

		final Integer zoom;

		if (cfg.getTmsUrlTemplate() != null && cfg.getZoom() == null) {
			// force using computed zoom
			zoom = (int) Math.floor(Math.log(Math.PI / 128.0 * (width - cfg.getMargin() * 2) / (maxX - minX)) / Math.log(2));
			rc.setProgress1(0, "computed zoom is " + zoom);
		} else {
			zoom = cfg.getZoom();
		}

		final double scale = zoom == null
				? (width - cfg.getMargin() * 2) / (maxX - minX)
				: (128.0 * (1 << zoom)) / Math.PI;

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

		long maxTime = Long.MIN_VALUE;

		// translate to 0,0
		for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
			for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
				maxTime = Math.max(maxTime, timePointMap.lastKey());
				minTime = Math.min(minTime, timePointMap.firstKey());

				for (final Point2D point : timePointMap.values()) {
					point.setLocation((point.getX() - minX) * scale, (maxY - point.getY()) * scale);
				}
			}
		}

		if (!wpMap.isEmpty()) {
			maxTime = Math.max(maxTime, wpMap.lastKey());
			minTime = Math.min(minTime, wpMap.firstKey());

			for (final Point2D point : wpMap.values()) {
				point.setLocation((point.getX() - minX) * scale, (maxY - point.getY()) * scale);
			}
		}

		final String frameFilePattern = cfg.getOutput().toString();
		final int dot = frameFilePattern.lastIndexOf('.');
		final String ext = dot == -1 ? null : frameFilePattern.substring(dot + 1);
		final boolean toImages = "png".equalsIgnoreCase(ext) || "jpg".equalsIgnoreCase(ext);

		int realWidth = (int) Math.round(((maxX - minX) * scale));
		int realHeight = (int) Math.round(((maxY - minY) * scale));

		// align width and height to 2 for videos
		if (realWidth % 2 == 1 && !userSpecifiedWidth && !toImages) {
			realWidth++;
		}
		if (realHeight % 2 == 1 && cfg.getHeight() == null && !toImages) {
			realHeight++;
		}

		final BufferedImage bi = new BufferedImage(realWidth, realHeight, BufferedImage.TYPE_3BYTE_BGR);

		final FrameWriter frameWriter = toImages
				? new FileFrameWriter(frameFilePattern, ext, cfg.getFps())
				: new VideoFrameWriter(cfg.getOutput(), cfg.getFps(), realWidth, realHeight);

		final Graphics2D ga = (Graphics2D) bi.getGraphics();

		System.out.println(realWidth + "x" + realHeight + ";" + scale);

		if (cfg.getTmsUrlTemplate() == null) {
			ga.setColor(Color.white);
			ga.fillRect(0, 0, realWidth, realHeight);
		} else {
			Map.drawMap(bi, cfg.getTmsUrlTemplate(), cfg.getBackgroundMapVisibility(), zoom, minX, maxX, minY, maxY, rc);
		}

		if (cfg.getFontSize() > 0) {
			font = new Font(Font.MONOSPACED, Font.PLAIN, cfg.getFontSize());
			fontMetrics = ga.getFontMetrics(font);
		}

		speedup = cfg.getTotalTime() == null ? cfg.getSpeedup() : 1.0 * (maxTime - minTime) / cfg.getTotalTime();

		final int frames = (int) ((maxTime + cfg.getTailDuration() - minTime) * cfg.getFps() / (MS * speedup));

		float skip = -1f;
		for (int frame = 1; frame < frames; frame++) {
			if (rc.isCancelled1()) {
				return;
			}

			final Long time = getTime(frame);
			skip: if (cfg.isSkipIdle()) {
				for (final Long[] span : spanList) {
					if (span[0] <= time && span[1] >= time) {
						break skip;
					}
				}
				rc.setProgress1((int) (100.0 * frame / frames), "Skipping unused Frame: " + frame + "/" + (frames - 1));
				skip = 1f;
				continue;
			}

			rc.setProgress1((int) (100.0 * frame / frames), "Rendering Frame: " + frame + "/" + (frames - 1));

			paint(bi, frame, 0);

			final BufferedImage bi2 = Utils.deepCopy(bi);

			paint(bi2, frame, cfg.getTailDuration());

			drawWaypoints(bi2, frame, wpMap);

			drawMarker(bi2, frame);

			if (font != null) {
				drawTime(bi2, frame);
				drawAttribution(bi2, cfg.getAttribution());
			}

			final Color flashbackColor = cfg.getFlashbackColor();
			if (skip > 0f && flashbackColor.getAlpha() > 0 && cfg.getFlashbackDuration() != null && cfg.getFlashbackDuration() > 0) {
				final Graphics2D g2 = (Graphics2D) bi2.getGraphics();
				g2.setColor(new Color(flashbackColor.getRed(), flashbackColor.getGreen(), flashbackColor.getBlue(), (int) (flashbackColor.getAlpha() * skip)));
				g2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());
				skip -= 1000f / cfg.getFlashbackDuration() / cfg.getFps();
			}

			frameWriter.addFrame(bi2);
		}

		frameWriter.close();

		System.out.println("Done.");
	}


	private void drawWaypoints(final BufferedImage bi, final int frame, final TreeMap<Long, Point2D> wpMap) {
		final Double waypointSize = cfg.getWaypointSize();
		if (waypointSize == null || waypointSize.doubleValue() == 0.0 || wpMap.isEmpty()) {
			return;
		}

		final Graphics2D g2 = getGraphics(bi);

		final long t2 = getTime(frame);


		if (t2 >= wpMap.firstKey()) {
			for (final Point2D p : wpMap.subMap(wpMap.firstKey(), t2).values()) {
				g2.setColor(Color.white);
				final Ellipse2D.Double marker = new Ellipse2D.Double(p.getX() - waypointSize / 2.0, p.getY() - waypointSize / 2.0, waypointSize, waypointSize);
				g2.setStroke(new BasicStroke(1f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				g2.fill(marker);
				g2.setColor(Color.black);
				g2.draw(marker);

				printText(g2, ((NamedPoint) p).name, (float) p.getX() + 8f, (float) p.getY() + 4f);
			}
		}
	}


	private static class NamedPoint extends Point2D.Double {
		private static final long serialVersionUID = 4011941819652468006L;
		String name;
	}


	private void toTimePointMap(final TreeMap<Long, Point2D> timePointMap, final int i, final List<LatLon> latLonList) throws UserException {
		long forcedTime = 0;

		final TrackConfiguration trackConfiguration = cfg.getTrackConfigurationList().get(i);

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
					throw new UserException("missing time for point; specify --forced-point-time-interval option");
				}
			}

			if (trackConfiguration.getTimeOffset() != null) {
				time += trackConfiguration.getTimeOffset();
			}

			final Point2D point;
			if (latLon instanceof Waypoint) {
				final NamedPoint namedPoint = new NamedPoint();
				namedPoint.setLocation(x, y);
				namedPoint.name = ((Waypoint) latLon).getName();
				point = namedPoint;
			} else {
				point = new Point2D.Double(x, y);
			}

			// hack to prevent overwriting existing (way)point with same time
			long freeTime = time;
			while (timePointMap.containsKey(freeTime)) {
				freeTime++;
			}
			timePointMap.put(freeTime, point);
		}
	}


	private static double lonToX(final Double maxLon) {
		return Math.toRadians(maxLon);
	}


	private static double latToY(final double lat) {
		return Math.log(Math.tan(Math.PI / 4 + Math.toRadians(lat) / 2));
	}


	private void drawTime(final BufferedImage bi, final int frame) {
		final String dateString = DATE_FORMAT.format(new Date(getTime(frame)));
		printText(getGraphics(bi), dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.getMargin(),
				bi.getHeight() - cfg.getMargin());
	}


	private void drawAttribution(final BufferedImage bi, final String attribution) {
		printText(getGraphics(bi), attribution, cfg.getMargin(), bi.getHeight() - cfg.getMargin());
	}


	private void drawMarker(final BufferedImage bi, final int frame) {
		if (cfg.getMarkerSize() == null || cfg.getMarkerSize().doubleValue() == 0.0) {
			return;
		}

		final Graphics2D g2 = getGraphics(bi);

		final long t2 = getTime(frame);

		final double markerSize = cfg.getMarkerSize();

		final List<TrackConfiguration> trackConfigurationList = cfg.getTrackConfigurationList();

		int i = 0;
		outer: for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
			final TrackConfiguration trackConfiguration = trackConfigurationList.get(i++);
			for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
				final Entry<Long, Point2D> ceilingEntry = timePointMap.ceilingEntry(t2);
				final Entry<Long, Point2D> floorEntry = timePointMap.floorEntry(t2);
				if (floorEntry == null) {
					continue;
				}

				final Point2D p = floorEntry.getValue();
				if (t2 - floorEntry.getKey() <= cfg.getTailDuration()) { // TODO make configurable
					g2.setColor(ceilingEntry == null ? Color.white : trackConfiguration.getColor());
					final Ellipse2D.Double marker = new Ellipse2D.Double(
							p.getX() - markerSize / 2.0,
							p.getY() - markerSize / 2.0,
							markerSize,
							markerSize);
					g2.setStroke(new BasicStroke(1f));
					g2.fill(marker);
					g2.setColor(Color.black);
					g2.draw(marker);

					final String label = trackConfiguration.getLabel();
					if (!label.isEmpty()) {
						printText(g2, label, (float) p.getX() + 8f, (float) p.getY() + 4f);
					}
				}

				continue outer;
			}
		}
	}


	private void paint(final BufferedImage bi, final int frame, final long backTime) {
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
					final long prevTime =  getTime(frame - 1);
					Long fromTime = timePointMap.floorKey(prevTime);
					if (fromTime == null) {
						// try ceiling because we may be at beginning
						fromTime = timePointMap.ceilingKey(prevTime);
					}
					if (fromTime == null) {
						continue;
					}

					g2.setPaint(trackConfiguration.getColor());
					for (final Entry<Long, Point2D> entry: timePointMap.subMap(fromTime, true, toTime, true).entrySet()) {
						if (prevPoint != null) {
							g2.draw(new Line2D.Double(prevPoint, entry.getValue()));
						}
						prevPoint = entry.getValue();
					}
				} else {
					for (final Entry<Long, Point2D> entry: timePointMap.subMap(toTime - backTime, true, toTime, true).entrySet()) {
						if (prevPoint != null) {
							final float ratio = (backTime - time + entry.getKey()) * 1f / backTime;
							if (ratio > 0) {
								final Color color = trackConfiguration.getColor();
								final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
								final Color c = Color.getHSBColor(hsb[0], hsb[1], (1f - ratio) * hsb[2]);
								g2.setPaint(new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (ratio * (255 - color.getAlpha()) + color.getAlpha())));
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

}
