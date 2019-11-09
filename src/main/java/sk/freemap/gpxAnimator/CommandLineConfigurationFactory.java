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

import java.awt.Color;
import java.awt.GraphicsEnvironment;
import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public final class CommandLineConfigurationFactory {

	private final List<String> inputGpxList = new ArrayList<String>();
	
	private final List<String> labelList = new ArrayList<String>();
	
	private final List<Color> colorList = new ArrayList<Color>();
	
	private final List<Long> timeOffsetList = new ArrayList<Long>();
	
	private final List<Long> forcedPointIntervalList = new ArrayList<Long>();
	
	private final List<Float> lineWidthList = new ArrayList<Float>();

	private final boolean gui;

	
	private final Configuration configuration;
	

	public CommandLineConfigurationFactory(final String[] args) throws UserException {
		final Configuration.Builder cfg = Configuration.createBuilder();

		boolean forceGui = false;

		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			
			try {
				final Option option = arg.startsWith("--") ? Option.fromName(arg.substring(2)) : null;
				
				if (option == null) {
					throw new UserException("unrecognised option " + arg
							+ "\nrun program with --help option to print help");
				} else {
					switch (option) {
					case ATTRIBUTION:
						cfg.attribution(args[++i]);
						break;
					case BACKGROUND_MAP_VISIBILITY:
						cfg.backgroundMapVisibility(Float.parseFloat(args[++i]));
						break;
					case COLOR:
						colorList.add(Color.decode(args[++i]));
						break;
					case FLASHBACK_COLOR:
						final long lv = Long.decode(args[++i]).longValue();
						cfg.flashbackColor(new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true));
						break;
					case FLASHBACK_DURATION:
						final String s = args[++i];
						cfg.flashbackDuration(s.trim().isEmpty() ? null : Long.parseLong(s));
						break;
					case FONT_SIZE:
						cfg.fontSize(Integer.parseInt(args[++i]));
						break;
					case FORCED_POINT_TIME_INTERVAL:
						final String s1 = args[++i].trim();
						forcedPointIntervalList.add(s1.isEmpty() ? null : Long.valueOf(s1));
						break;
					case FPS:
						cfg.fps(Double.parseDouble(args[++i]));
						break;
					case GUI:
						if (GraphicsEnvironment.isHeadless()) {
							throw new UserException("graphics is not supported in this environment");
						}
						forceGui = true;
						break;
					case HEIGHT:
						cfg.height(Integer.valueOf(args[++i]));
						break;
					case HELP:
						System.out.println("GPX Animator " + Constants.VERSION);
						System.out.println("Copyright " + Constants.YEAR + " Martin Ždila, Freemap Slovakia");
						System.out.println();
						System.out.println("Usage:");
						final PrintWriter pw = new PrintWriter(System.out);
						Help.printHelp(new Help.PrintWriterOptionHelpWriter(pw));
						pw.flush();
						System.exit(0);
						break;
					case INPUT:
						inputGpxList.add(args[++i]);
						break;
					case KEEP_IDLE:
						cfg.skipIdle(false);
						break;
					case LABEL:
						labelList.add(args[++i]);
						break;
					case LINE_WIDTH:
						lineWidthList.add(Float.valueOf(args[++i]));
						break;
					case MARGIN:
						cfg.margin(Integer.parseInt(args[++i]));
						break;
					case MARKER_SIZE:
						cfg.markerSize(Double.parseDouble(args[++i]));
						break;
					case MAX_LAT:
						cfg.maxLat(Double.parseDouble(args[++i]));
						break;
					case MAX_LON:
						cfg.maxLon(Double.parseDouble(args[++i]));
						break;
					case MIN_LAT:
						cfg.minLat(Double.parseDouble(args[++i]));
						break;
					case MIN_LON:
						cfg.minLon(Double.parseDouble(args[++i]));
						break;
					case OUTPUT:
						cfg.output(new File(args[++i]));
						break;
					case PHOTOS:
						cfg.photos(new File(args[++i]));
						break;
					case PHOTO_TIME:
						cfg.photoTime(Long.parseLong(args[++i]));
						break;
					case SPEEDUP:
						cfg.speedup(Double.parseDouble(args[++i]));
						break;
					case TAIL_DURATION:
						cfg.tailDuration(Long.parseLong(args[++i]));
						break;
					case TILE_CACHE_PATH:
						cfg.tileCachePath(args[++i]);
						break;
					case TILE_CACHE_TIME_LIMIT:
						cfg.tileCacheTimeLimit(Long.parseLong(args[++i]));
						break;
					case TIME_OFFSET:
						final String s2 = args[++i].trim();
						timeOffsetList.add(s2.isEmpty() ? null : Long.valueOf(s2));
						break;
					case TMS_URL_TEMPLATE:
						cfg.tmsUrlTemplate(args[++i]);
						break;
					case TOTAL_TIME:
						final String s3 = args[++i].trim();
						cfg.totalTime(s3.isEmpty() ? null : Long.valueOf(s3));
						break;
					case WAYPOINT_SIZE:
						cfg.waypointSize(Double.parseDouble(args[++i]));
						break;
					case WIDTH:
						cfg.width(Integer.valueOf(args[++i]));
						break;
					case ZOOM:
						cfg.zoom(Integer.parseInt(args[++i]));
						break;
					default:
						throw new AssertionError();
					}
					
// TODO				--configuration : args[++i];
				}
			} catch (final NumberFormatException e) {
				throw new UserException("invalid number for option " + arg);
			} catch (final ArrayIndexOutOfBoundsException e) {
				throw new UserException("missing parameter for option " + arg);
			}
		}
		
		normalizeColors();
		normalizeLineWidths();
		
		for (int i = 0, n = inputGpxList.size(); i < n; i++) {
			final TrackConfiguration.Builder tcb = TrackConfiguration.createBuilder();
			tcb.inputGpx(new File(inputGpxList.get(i)));
			tcb.color(colorList.get(i));
			tcb.lineWidth(lineWidthList.get(i));
			tcb.label(i < labelList.size() ? labelList.get(i) : "");
			tcb.timeOffset(i < timeOffsetList.size() ? timeOffsetList.get(i) : Long.valueOf(0));
			tcb.forcedPointInterval(i < forcedPointIntervalList.size() ? forcedPointIntervalList.get(i) : null);
			
			cfg.addTrackConfiguration(tcb.build());
		}
		
		gui = args.length == 0 || forceGui;
		
		configuration = cfg.build();
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
	
	
	public Configuration getConfiguration() {
		return configuration;
	}
	
	
	public boolean isGui() {
		return gui;
	}
	
}
