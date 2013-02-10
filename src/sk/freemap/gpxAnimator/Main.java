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
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

public class Main {

	private static final double MS = 1000d;
	private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

	private int margin = 20;
	private double speedup = 1000.0;
	private long tailDuration = 3600;
	private double fps = 30.0;
	private boolean debug;
	private double totalTime = Double.NaN;
	private int width = 0;
	private int height = 0;
	private int zoom = 0;
	private float backgroundMapVisibility = 50f;
	private boolean skipIdle = true;

	private final List<List<TreeMap<Long, Point2D>>> timePointMapListList = new ArrayList<List<TreeMap<Long,Point2D>>>();
	private final List<String> inputGpxList = new ArrayList<String>();
	private final List<String> labelList = new ArrayList<String>();
	private final List<Color> colorList = new ArrayList<Color>();
	private final List<Long> timeOffsetList = new ArrayList<Long>();
	private final List<Long> forcedPointIntervalList = new ArrayList<Long>();
	
	private String frameFilePattern = "frame%08d.png";
	private String tmsUrlTemplate; // http://tile.openstreetmap.org/{zoom}/{x}/{y}.png, http://aio.freemap.sk/T/{zoom}/{x}/{y}.png

	private Font font;
	private FontMetrics fontMetrics;
	private int fontSize = 12;
	private final List<Float> lineWidthList = new ArrayList<Float>();
	
	private Color flashbackColor = Color.white;
	private float flashbackDuration = 250f;
	
	private long minTime = Long.MAX_VALUE;
	private double minX = Double.MAX_VALUE, maxX = Double.MIN_VALUE, minY = Double.MAX_VALUE, maxY = Double.MIN_VALUE;
	private double markerSize = 8.0;
	private double waypointSize = 6.0;

	
	/**
	 * @param args
	 * @throws SAXException
	 * @throws ParserConfigurationException
	 * @throws IOException
	 */
	public static void main(final String[] args) {
		final Main main = new Main(args);
		try {
			main.render();
		} catch (final UserException e) {
			if (main.debug) {
				e.printStackTrace();
			} else {
				System.err.println(e.getMessage());
			}
			System.exit(1);
		}
	}
	
	
	public Main(final String[] args) {
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			
			try {
				if (arg.equals("--input")) {
					inputGpxList.add(args[++i]);
				} else if (arg.equals("--output")) {
					frameFilePattern = args[++i];
				} else if (arg.equals("--label")) {
					labelList.add(args[++i]);
				} else if (arg.equals("--color")) {
					colorList.add(Color.decode(args[++i]));
				} else if (arg.equals("--margin")) {
					margin = Integer.parseInt(args[++i]);
				} else if (arg.equals("--time-offset")) {
					timeOffsetList.add(Long.parseLong(args[++i]));
				} else if (arg.equals("--forced-point-time-interval")) {
					forcedPointIntervalList.add(Long.parseLong(args[++i]));
				} else if (arg.equals("--speedup")) {
					speedup = Double.parseDouble(args[++i]);
				} else if (arg.equals("--line-width")) {
					lineWidthList.add(Float.parseFloat(args[++i]));
				} else if (arg.equals("--tail-duration")) {
					tailDuration = Long.parseLong(args[++i]);
				} else if (arg.equals("--fps")) {
					fps = Double.parseDouble(args[++i]);
				} else if (arg.equals("--marker-size")) {
					markerSize = Double.parseDouble(args[++i]);
				} else if (arg.equals("--waypoint-size")) {
					waypointSize = Double.parseDouble(args[++i]);
				} else if (arg.equals("--width")) {
					width = Integer.parseInt(args[++i]);
				} else if (arg.equals("--height")) {
					height = Integer.parseInt(args[++i]);
				} else if (arg.equals("--zoom")) {
					zoom = Integer.parseInt(args[++i]);
				} else if (arg.equals("--font-size")) {
					fontSize = Integer.parseInt(args[++i]);
				} else if (arg.equals("--debug")) {
					debug = true;
				} else if (arg.equals("--tms-url-template")) {
					tmsUrlTemplate = args[++i];
				} else if (arg.equals("--background-map-visibility")) {
					backgroundMapVisibility = Float.parseFloat(args[++i]);
				} else if (arg.equals("--total-time")) {
					totalTime = Double.parseDouble(args[++i]);
				} else if (arg.equals("--keep-idle")) {
					skipIdle = false;
				} else if (arg.equals("--flashback-color")) {
					final long lv = Long.decode(args[++i]).longValue();
					flashbackColor = new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true);
				} else if (arg.equals("--flashback-duration")) {
					flashbackDuration = Float.parseFloat(args[++i]);
				} else if (arg.equals("--help")) {
					Help.printHelp();
					System.exit(0);
				} else {
					System.err.println("unrecognised option " + arg);
					System.err.println("run program with --help option to print help");
					System.exit(1);
				}
			} catch (final NumberFormatException e) {
				System.err.println("invalid number for option " + arg);
				System.exit(1);
			} catch (final ArrayIndexOutOfBoundsException e) {
				System.err.println("missing parameter for option " + arg);
				System.exit(1);
			}
		}
	}


	private void validateOptions() throws UserException {
		if (inputGpxList.isEmpty()) {
			throw new UserException("missing input file");
		}
		
		if (String.format(frameFilePattern, 100).equals(String.format(frameFilePattern, 200))) {
			throw new UserException("--output must be pattern, for example frame%08d.png");
		}
		
		// TODO other validations
	}
	
	
	private void normalizeColors() {
		final int size = inputGpxList.size();
		final int size2 = colorList.size();
		if (size2 == 0) {
			for (int i = 0; i < size; i++) {
				colorList.add(Color.getHSBColor((float) i / size, 0.8f, 1f));
			}
		} else if (size2 < size) {
			for (int i = size2; i < size; i++) {
				colorList.add(colorList.get(i - size2));
			}
		}
	}
	
	
	private void normalizeLineWidths() {
		final int size = inputGpxList.size();
		final int size2 = lineWidthList.size();
		if (size2 == 0) {
			for (int i = 0; i < size; i++) {
				lineWidthList.add(2f);
			}
		} else if (size2 < size) {
			for (int i = size2; i < size; i++) {
				lineWidthList.add(lineWidthList.get(i - size2));
			}
		}
	}

	
	private void render() throws UserException {
		validateOptions();
		normalizeColors();
		normalizeLineWidths();
		
		final List<Long[]> spanList = new ArrayList<Long[]>();
		
		final TreeMap<Long, Point2D> wpMap = new TreeMap<Long, Point2D>();
		
		int i = -1;
		for (final String inputGpx : inputGpxList) {
			i++;
			
			final GpxContentHandler gch = new GpxContentHandler();
			GpxParser.parseGpx(inputGpx, gch);

			
			final List<TreeMap<Long, Point2D>> timePointMapList = new ArrayList<TreeMap<Long, Point2D>>();
						
			for (final List<LatLon> latLonList : gch.getPointLists()) {
				final TreeMap<Long, Point2D> timePointMap = toTimePointMap(i, latLonList);
				timePointMapList.add(timePointMap);

				wpMap.putAll(toTimePointMap(i, gch.getWaypointList()));

				Long t0 = timePointMap.firstKey();
				Long t1 = timePointMap.lastKey() + tailDuration * 1000;
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

		final boolean userSpecifiedWidth = width != 0;
		if (width == 0) {
			width = 800;
		}
		
		if (tmsUrlTemplate != null && zoom == 0) {
			// force using computed zoom
			zoom = (int) Math.floor(Math.log(Math.PI / 128.0 * (width - margin * 2) / (maxX - minX)) / Math.log(2));
			System.out.println("computed zoom is " + zoom);
		}
		
		final double scale = zoom == 0
				? (width - margin * 2) / (maxX - minX)
				: (128.0 * (1 << zoom)) / Math.PI;
		
		minX -= margin / scale;
		maxX += margin / scale;
		minY -= margin / scale;
		maxY += margin / scale;
		
		if (userSpecifiedWidth) {
			final double ww = width - (maxX - minX) * scale;
			minX -= ww / scale / 2.0;
			maxX += ww / scale / 2.0;
		}

		if (height != 0) {
			final double hh = height - (maxY - minY) * scale;
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
		
		if (tmsUrlTemplate == null) {
			ga.setColor(Color.white);
			ga.fillRect(0, 0, bi.getWidth(), bi.getHeight());
		} else {
			drawMap(minX, maxX, minY, maxY, bi);
		}
		
		if (fontSize > 0) {
			font = new Font(Font.MONOSPACED, Font.PLAIN, fontSize);
			fontMetrics = ga.getFontMetrics(font);
		}

		if (!Double.isNaN(totalTime)) {
			speedup = (maxTime - minTime) / totalTime;
		}

		final int frames = (int) ((maxTime + tailDuration * 1000 - minTime) * fps / (MS * speedup));
		
		int f = 0;
		float skip = -1f;
		for (int frame = 1; frame < frames; frame++) {
			final Long time = getTime(frame);
			skip: if (skipIdle) {
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
			paint(bi2, frame, tailDuration * 1000);
			if (waypointSize > 0.0 && !wpMap.isEmpty()) {
				drawWaypoints(bi2, frame, wpMap);
			}
			drawMarker(bi2, frame);

			if (fontSize > 0) {
				drawTime(bi2, frame);
			}
			
			if (skip > 0f && flashbackColor.getAlpha() > 0 && flashbackDuration > 0f) {
				final Graphics2D g2 = (Graphics2D) bi2.getGraphics();
				g2.setColor(new Color(flashbackColor.getRed(), flashbackColor.getGreen(), flashbackColor.getBlue(), (int) (flashbackColor.getAlpha() * skip)));
				g2.fillRect(0, 0, bi2.getWidth(), bi2.getHeight());
				skip -= 1000f / flashbackDuration / fps;
			}

			final File outputfile = new File(String.format(frameFilePattern, ++f));
		    try {
				ImageIO.write(bi2, "png", outputfile);
			} catch (final IOException e) {
				throw new UserException("error writing frame to " + outputfile);
			}
		}
		
		System.out.println("Done.");
		System.out.println("To encode generated frames you may run this command:");
		System.out.println("ffmpeg -i " + frameFilePattern + " -vcodec mpeg4 -b 3000k -r " + fps + " video.avi");
	}


	private void drawWaypoints(final BufferedImage bi, final int frame, final TreeMap<Long, Point2D> wpMap) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		final long t2 = getTime(frame);
		
		if (t2 >= wpMap.firstKey())
		for (final Point2D p : wpMap.subMap(wpMap.firstKey(), t2).values()) {
			g2.setColor(Color.white);
			final Ellipse2D.Double marker = new Ellipse2D.Double(p.getX() - waypointSize / 2.0, p.getY() - waypointSize / 2.0, waypointSize, waypointSize);
			g2.setStroke(new BasicStroke(1f));
			g2.fill(marker);
			g2.setColor(Color.black);
			g2.draw(marker);
			
			printText(g2, ((NamedPoint) p).name, (float) p.getX() + 8f, (float) p.getY() + 4f);
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
			if (i < forcedPointIntervalList.size() && !Long.valueOf(0L).equals(forcedPointIntervalList.get(i))) {
				forcedTime += forcedPointIntervalList.get(i);
				time = forcedTime;
			} else {
				time = latLon.getTime();
				if (time == Long.MIN_VALUE) {
					throw new UserException("missing time for point; specify --forced-point-time-interval option");
				}
			}
		
			if (i < timeOffsetList.size()) {
				time += timeOffsetList.get(i);
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


	private void drawMap(final double minX, final double maxX, final double minY, final double maxY, final BufferedImage bi) throws UserException {
		final Graphics2D ga = (Graphics2D) bi.getGraphics();

		final double tileDblX = lonToTileX(xToLon(minX));
		final int tileX = (int) Math.floor(tileDblX);
		final int offsetX = (int) Math.floor(256.0 * (tileX - tileDblX));

		final double tileDblY = latToTileY(yToLat(minY));
		final int tileY = (int) Math.floor(tileDblY);
		final int offsetY = (int) Math.floor(256.0 * (tileDblY - tileY));

		final int maxXtile = (int) Math.floor(lonToTileX(xToLon(maxX)));
		final int maxYtile = (int) Math.floor(latToTileY(yToLat(maxY)));
		for (int x = tileX; x <= maxXtile; x++) {
			for (int y = tileY; y >= maxYtile; y--) {
				final String url = tmsUrlTemplate
						.replace("{zoom}", Integer.toString(zoom))
						.replace("{x}", Integer.toString(x))
						.replace("{y}", Integer.toString(y));
				
				System.out.println("reading tile " + url);
				
				final BufferedImage tile;
				try {
					tile = ImageIO.read(new URL(url));
				} catch (final IOException e) {
					throw new UserException("error reading tile " + url);
				}
				
				// convert to RGB format
				final BufferedImage tile1 = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
				tile1.getGraphics().drawImage(tile, 0, 0, null);
				
				ga.drawImage(tile1,
						new RescaleOp(backgroundMapVisibility / 100f, (1f - backgroundMapVisibility / 100f) * 255f, null),
						256 * (x - tileX) + offsetX,
						bi.getHeight() - (256 * (tileY - y) + offsetY));
			}
		}
	}


	private double lonToTileX(final double lon) {
		return (lon + 180.0) / 360.0 * (1 << zoom);
	}


	private double latToTileY(final double lat) {
		return (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);
	}


	private static double xToLon(final double x) {
		return Math.toDegrees(x);
	}


	private static double yToLat(final double y) {
		return Math.toDegrees(2.0 * (Math.atan(Math.exp(y)) - Math.PI / 4.0));
	}


	private void drawTime(final BufferedImage bi, final int frame) {
		final Graphics2D g2 = (Graphics2D) bi.getGraphics();
		final String dateString = DATE_FORMAT.format(new Date(getTime(frame)));
		printText(g2, dateString, bi.getWidth() - fontMetrics.stringWidth(dateString) - margin, bi.getHeight() - margin);
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
				g2.setColor(ceilingEntry == null ? Color.white : colorList.get(i));
				final Ellipse2D.Double marker = new Ellipse2D.Double(p.getX() - markerSize / 2.0, p.getY() - markerSize / 2.0, markerSize, markerSize);
				g2.setStroke(new BasicStroke(1f));
				g2.fill(marker);
				g2.setColor(Color.black);
				g2.draw(marker);
				
				if (i < labelList.size()) {
					printText(g2, labelList.get(i), (float) p.getX() + 8f, (float) p.getY() + 4f);
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
				g2.setStroke(new BasicStroke(lineWidthList.get(i), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
				
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
					
					g2.setPaint(colorList.get(i));
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
								final Color color = colorList.get(i);
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
		return (long) Math.floor(minTime + frame / fps * MS * speedup);
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
