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
import java.util.ArrayList;
import java.util.List;

public final class CommandLineConfigurationFactory {

	private final List<String> inputGpxList = new ArrayList<String>();
	
	private final List<String> labelList = new ArrayList<String>();
	
	private final List<Color> colorList = new ArrayList<Color>();
	
	private final List<Long> timeOffsetList = new ArrayList<Long>();
	
	private final List<Long> forcedPointIntervalList = new ArrayList<Long>();
	
	private final List<Float> lineWidthList = new ArrayList<Float>();
	

	public static Configuration createConfiguration(final String[] args) throws UserException {
		return new CommandLineConfigurationFactory().createConfigurationInt(args);
	}
	
	
	private CommandLineConfigurationFactory() {
	}


	/**
	 * @param args
	 * @throws UserException
	 */
	private Configuration createConfigurationInt(final String[] args) throws UserException {
		final Configuration.Builder cfg = Configuration.createBuilder();
		
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			
			try {
				if (arg.equals("--input")) {
					inputGpxList.add(args[++i]);
				} else if (arg.equals("--output")) {
					cfg.frameFilePattern(args[++i]);
				} else if (arg.equals("--label")) {
					labelList.add(args[++i]);
				} else if (arg.equals("--color")) {
					colorList.add(Color.decode(args[++i]));
				} else if (arg.equals("--margin")) {
					cfg.margin(Integer.parseInt(args[++i]));
				} else if (arg.equals("--time-offset")) {
					timeOffsetList.add(Long.valueOf(args[++i]));
				} else if (arg.equals("--forced-point-time-interval")) {
					final String s = args[++i];
					Long l;
					forcedPointIntervalList.add(s.isEmpty() ? null : (l = Long.valueOf(s)).longValue() == 0l ? null : l);
				} else if (arg.equals("--speedup")) {
					cfg.speedup(Double.parseDouble(args[++i]));
				} else if (arg.equals("--line-width")) {
					lineWidthList.add(Float.valueOf(args[++i]));
				} else if (arg.equals("--tail-duration")) {
					cfg.tailDuration(Long.parseLong(args[++i]) * 1000);
				} else if (arg.equals("--fps")) {
					cfg.fps(Double.parseDouble(args[++i]));
				} else if (arg.equals("--marker-size")) {
					cfg.markerSize(Double.parseDouble(args[++i]));
				} else if (arg.equals("--waypoint-size")) {
					cfg.waypointSize(Double.parseDouble(args[++i]));
				} else if (arg.equals("--width")) {
					cfg.width(Integer.valueOf(args[++i]));
				} else if (arg.equals("--height")) {
					cfg.height(Integer.valueOf(args[++i]));
				} else if (arg.equals("--zoom")) {
					cfg.zoom(Integer.parseInt(args[++i]));
				} else if (arg.equals("--font-size")) {
					cfg.fontSize(Integer.parseInt(args[++i]));
				} else if (arg.equals("--tms-url-template")) {
					cfg.tmsUrlTemplate(args[++i]);
				} else if (arg.equals("--background-map-visibility")) {
					cfg.backgroundMapVisibility(Float.parseFloat(args[++i]) / 100f);
				} else if (arg.equals("--total-time")) {
					cfg.totalTime(Long.valueOf(args[++i]));
				} else if (arg.equals("--keep-idle")) {
					cfg.skipIdle(false);
				} else if (arg.equals("--flashback-color")) {
					final long lv = Long.decode(args[++i]).longValue();
					cfg.flashbackColor(new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true));
				} else if (arg.equals("--flashback-duration")) {
					cfg.flashbackDuration(Float.parseFloat(args[++i]));
				} else if (arg.equals("--help")) {
					Help.printHelp();
					System.exit(0);
				} else {
					throw new UserException("unrecognised option " + arg + "\nrun program with --help option to print help");
				}
			} catch (final NumberFormatException e) {
				throw new UserException("invalid number for option " + arg);
			} catch (final ArrayIndexOutOfBoundsException e) {
				throw new UserException("missing parameter for option " + arg);
			}
		}
		
		normalizeColors();
		normalizeLineWidths();
		
//			private final List<String> labelList = new ArrayList<String>();
//			private final List<Long> timeOffsetList = new ArrayList<Long>();
//			private final List<Long> forcedPointIntervalList = new ArrayList<Long>();
		
		for (int i = 0, n = inputGpxList.size(); i < n; i++) {
			final TrackConfiguration.Builder tcb = TrackConfiguration.createBuilder();
			tcb.inputGpx(inputGpxList.get(i));
			tcb.color(colorList.get(i));
			tcb.lineWidth(lineWidthList.get(i));
			tcb.label(i < labelList.size() ? labelList.get(i) : "");
			tcb.timeOffset(i < timeOffsetList.size() ? timeOffsetList.get(i) : Long.valueOf(0));
			tcb.forcedPointInterval(i < forcedPointIntervalList.size() ? forcedPointIntervalList.get(i) : null);
			
			cfg.addTrackConfiguration(tcb.build());
		}
		
		return cfg.build();
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
	
}
