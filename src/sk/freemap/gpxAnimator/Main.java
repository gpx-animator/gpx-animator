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
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;

public class Main {

	private static final double MS = 1000d;
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private final Configuration cfg;
	
	private final List<List<TreeMap<Long, Point2D>>> timePointMapListList = new ArrayList<List<TreeMap<Long,Point2D>>>();

	private Font font;
	private FontMetrics fontMetrics;
	
	private long minTime = Long.MAX_VALUE;
	private double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;

	
	public static void main(final String[] args) {
		try {
			new Main(args).render();
		} catch (final UserException e) {
			System.err.println(e.getMessage());
			System.exit(1);
		}
	}
	
	
	public Main(final String[] args) throws UserException {
		cfg = CommandLineConfigurationFactory.createConfiguration(args);
		cfg.validateOptions();
		cfg.normalizeColors();
		cfg.normalizeLineWidths();
	}


	private void render() throws UserException {
		final List<Long[]> spanList = new ArrayList<Long[]>();
		
		final TreeMap<Long, Point2D> wpMap = new TreeMap<Long, Point2D>();
		
		int i = -1;
		for (final String inputGpx : cfg.inputGpxList) {
			i++;
			
			final GpxContentHandler gch = new GpxContentHandler();
			GpxParser.parseGpx(inputGpx, gch);
			
			final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<TreeMap<Long, Point2D>>();
						
			for (final List<LatLon> latLonList : gch.getPointLists()) {
				final TreeMap<Long, Point2D> timePointMap = toTimePointMap(i, latLonList);
				timePointMapList.add(timePointMap);

				wpMap.putAll(toTimePointMap(i, gch.getWaypointList()));

				Long t0 = timePointMap.firstKey();
				Long t1 = timePointMap.lastKey() + cfg.tailDuration * 1000;
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

		final boolean userSpecifiedWidth = cfg.width != 0;
		if (cfg.width == 0) {
			cfg.width = 800;
		}
		
		if (cfg.tmsUrlTemplate != null && cfg.zoom == 0) {
			// force using computed zoom
			cfg.zoom = (int) Math.floor(Math.log(Math.PI / 128.0 * (cfg.width - cfg.margin * 2) / (maxX - minX)) / Math.log(2));
			System.out.println("computed zoom is " + cfg.zoom);
		}
		
		final double scale = cfg.zoom == 0
				? (cfg.width - cfg.margin * 2) / (maxX - minX)
				: (128.0 * (1 << cfg.zoom)) / Math.PI;
		
		minX -= cfg.margin / scale;
		maxX += cfg.margin / scale;
		minY -= cfg.margin / scale;
		maxY += cfg.margin / scale;
		
		if (userSpecifiedWidth) {
			final double ww = cfg.width - (maxX - minX) * scale;
			minX -= ww / scale / 2.0;
			maxX += ww / scale / 2.0;
		}

		if (cfg.height != 0) {
			final double hh = cfg.height - (maxY - minY) * scale;
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
		
		final BufferedImage bi = new BufferedImage(
				(int) ((maxX - minX) * scale),
				(int) ((maxY - minY) * scale),
				BufferedImage.TYPE_INT_RGB);
		
		final Graphics2D ga = (Graphics2D) bi.getGraphics();
		
		if (cfg.tmsUrlTemplate == null) {
			ga.setColor(Color.white);
			ga.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		} else {
			Map.drawMap(bi, cfg.tmsUrlTemplate, cfg.backgroundMapVisibility, cfg.zoom, minX, maxX, minY, maxY);
		}
		
		if (cfg.fontSize > 0) {
			font = new Font(Font.MONOSPACED, Font.PLAIN, cfg.fontSize);
			fontMetrics = ga.getFontMetrics(font);
		}

		if (!Double.isNaN(cfg.totalTime)) {
			cfg.speedup = (maxTime - minTime) / cfg.totalTime;
		}

		final int frames = (int) ((maxTime + cfg.tailDuration * 1000 - minTime) * cfg.fps / (MS * cfg.speedup));
		
		int f = 0;
		float skip = -1f;
		for (int frame = 1; frame < frames; frame++) {
			final Long time = getTime(frame);
			skip: if (cfg.skipIdle) {
				for (final Long[] span : spanList) {
					if (span[0] <= time && span[1] >= time) {
						break skip;
					}
				}
				System.out.println("Skipping unused frame: " + frame + "/" + (frames - 1));
				skip = 1f;
				continue;
			}
			
			System.out.println("Frame: " + frame + "/" + (frames - 1));
			paint(bi, frame, 0);
			
			final BufferedImage bi2 = Utils.deepCopy(bi);
			
			paint(bi2, frame, cfg.tailDuration * 1000);
			
			if (cfg.waypointSize > 0.0 && !wpMap.isEmpty()) {
				drawWaypoints(bi2, frame, wpMap);
			}
			
			if (cfg.markerSize > 0.0) {
				drawMarker(bi2, frame);
			}

			if (cfg.fontSize > 0) {
				drawTime(bi2, frame);
			}
			
			if (skip > 0f && cfg.flashbackColor.getAlpha() > 0 && cfg.flashbackDuration > 0f) {
				final Graphics2D g2 = (Graphics2D) bi2.getGraphics();
				g2.setColor(new Color(cfg.flashbackColor.getRed(), cfg.flashbackColor.getGreen(), cfg.flashbackColor.getBlue(), (int) (cfg.flashbackColor.getAlpha() * skip)));
				g2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());
				skip -= 1000f / cfg.flashbackDuration / cfg.fps;
			}

			final File outputfile = new File(String.format(cfg.frameFilePattern, ++f));
		    try {
				ImageIO.write(bi2, "png", outputfile);
			} catch (final IOException e) {
				throw new UserException("error writing frame to " + outputfile);
			}
		}
		
		System.out.println("Done.");
		System.out.println("To encode generated frames you may run this command:");
		System.out.println("ffmpeg -i " + cfg.frameFilePattern + " -vcodec mpeg4 -b 3000k -r " + cfg.fps + " video.avi");
	}


	private void drawWaypoints(final BufferedImage bi, final int frame, final TreeMap<Long, Point2D> wpMap) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final long t2 = getTime(frame);
		
		if (t2 >= wpMap.firstKey()) {
			for (final Point2D p : wpMap.subMap(wpMap.firstKey(), t2).values()) {
				g2.setColor(Color.white);
				final Ellipse2D.Double marker = new Ellipse2D.Double(p.getX() - cfg.waypointSize / 2.0, p.getY() - cfg.waypointSize / 2.0, cfg.waypointSize, cfg.waypointSize);
				g2.setStroke(new BasicStroke(1f));
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
	
	
	private TreeMap<Long, Point2D> toTimePointMap(final int i, final List<LatLon> latLonList) throws UserException {
		long forcedTime = 0;

		final TreeMap<Long, Point2D> timePointMap = new TreeMap<Long, Point2D>();
		for (final LatLon latLon : latLonList) {
			final double x = Math.toRadians(latLon.getLon());
			final double y = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(latLon.getLat()) / 2));
			
			minX = Math.min(x, minX);
			minY = Math.min(y, minY);
			maxX = Math.max(x, maxX);
			maxY = Math.max(y, maxY);

			long time;
			if (i < cfg.forcedPointIntervalList.size() && !Long.valueOf(0L).equals(cfg.forcedPointIntervalList.get(i))) {
				forcedTime += cfg.forcedPointIntervalList.get(i);
				time = forcedTime;
			} else {
				time = latLon.getTime();
				if (time == Long.MIN_VALUE) {
					throw new UserException("missing time for point; specify --forced-point-time-interval option");
				}
			}
		
			if (i < cfg.timeOffsetList.size()) {
				time += cfg.timeOffsetList.get(i);
			}
			
			
			if (latLon instanceof Waypoint) {
				final NamedPoint namedPoint = new NamedPoint();
				namedPoint.setLocation(x, y);
				namedPoint.name = ((Waypoint) latLon).getName();
				timePointMap.put(time, namedPoint);
			} else {
				timePointMap.put(time, new Point2D.Double(x, y));
			}
		}
		return timePointMap;
	}



	private void drawTime(final BufferedImage bi, final int frame) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		final String dateString = DATE_FORMAT.format(new Date(getTime(frame)));
		printText(g2, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - cfg.margin, bi.getHeight() - cfg.margin);
	}


	private void drawMarker(final BufferedImage bi, final int frame) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final long t2 = getTime(frame);
		
		int i = -1;
		outer: for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
			i++;
			for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
				final Entry<Long, Point2D> ceilingEntry = timePointMap.ceilingEntry(t2);
				final Entry<Long, Point2D> floorEntry = timePointMap.floorEntry(t2);
				if (floorEntry == null) {
					continue;
				}
				
				final Point2D p = floorEntry.getValue();
				g2.setColor(ceilingEntry == null ? Color.white : cfg.colorList.get(i));
				final Ellipse2D.Double marker = new Ellipse2D.Double(p.getX() - cfg.markerSize / 2.0, p.getY() - cfg.markerSize / 2.0, cfg.markerSize, cfg.markerSize);
				g2.setStroke(new BasicStroke(1f));
				g2.fill(marker);
				g2.setColor(Color.black);
				g2.draw(marker);
				
				if (i < cfg.labelList.size()) {
					printText(g2, cfg.labelList.get(i), (float) p.getX() + 8f, (float) p.getY() + 4f);
				}
				
				continue outer;
			}
		}
	}
	

	private void paint(final BufferedImage bi, final int frame, final long backTime) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
		final long time = getTime(frame);
		
		int i = -1;
		for (final List<TreeMap<Long, Point2D>> timePointMapList : timePointMapListList) {
			i++;
			for (final TreeMap<Long, Point2D> timePointMap : timePointMapList) {
				g2.setStroke(new BasicStroke(cfg.lineWidthList.get(i), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
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
					
					g2.setPaint(cfg.colorList.get(i));
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
								final Color color = cfg.colorList.get(i);
								final float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), new float[3]);
								g2.setPaint(Color.getHSBColor(hsb[0], hsb[1], (1f - ratio) * hsb[2]));
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
		return (long) Math.floor(minTime + frame / cfg.fps * MS * cfg.speedup);
	}
	
	
	private void printText(final Graphics2D g2, final String text, final float x, final float y) {
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

		final FontRenderContext frc = g2.getFontRenderContext();
		g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));

		final TextLayout tl = new TextLayout(text, font, frc);
		final Shape sha = tl.getOutline(AffineTransform.getTranslateInstance(x, y));
		g2.setColor(Color.white);
		g2.fill(sha);
		g2.draw(sha);
		
		g2.setFont(font);
		g2.setColor(Color.black);
		g2.drawString(text, x, y);
	}

}
