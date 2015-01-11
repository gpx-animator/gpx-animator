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
				if (arg.equals("--gui")) {
					if (GraphicsEnvironment.isHeadless()) {
						throw new UserException("graphics is not supported in this environment");
					}
					forceGui = true;
				} else if (arg.equals("--input")) {
					inputGpxList.add(args[++i]);
// TODO				} else if (arg.equals("--configuration")) {
//					args[++i];
				} else if (arg.equals("--output")) {
					cfg.output(new File(args[++i]));
				} else if (arg.equals("--label")) {
					labelList.add(args[++i]);
				} else if (arg.equals("--color")) {
					colorList.add(Color.decode(args[++i]));
				} else if (arg.equals("--margin")) {
					cfg.margin(Integer.parseInt(args[++i]));
				} else if (arg.equals("--time-offset")) {
					final String s = args[++i].trim();
					timeOffsetList.add(s.isEmpty() ? null : Long.valueOf(s));
				} else if (arg.equals("--forced-point-time-interval")) {
					final String s = args[++i].trim();
					forcedPointIntervalList.add(s.isEmpty() ? null : Long.valueOf(s));
				} else if (arg.equals("--speedup")) {
					cfg.speedup(Double.parseDouble(args[++i]));
				} else if (arg.equals("--line-width")) {
					lineWidthList.add(Float.valueOf(args[++i]));
				} else if (arg.equals("--tail-duration")) {
					cfg.tailDuration(Long.parseLong(args[++i]));
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
				} else if (arg.equals("--attribution")) {
					cfg.attribution(args[++i]);
				} else if (arg.equals("--background-map-visibility")) {
					cfg.backgroundMapVisibility(Float.parseFloat(args[++i]));
				} else if (arg.equals("--total-time")) {
					final String s = args[++i].trim();
					cfg.totalTime(s.isEmpty() ? null : Long.valueOf(s));
				} else if (arg.equals("--keep-idle")) {
					cfg.skipIdle(false);
				} else if (arg.equals("--flashback-color")) {
					final long lv = Long.decode(args[++i]).longValue();
					cfg.flashbackColor(new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true));
				} else if (arg.equals("--flashback-duration")) {
					final String s = args[++i];
					cfg.flashbackDuration(s.trim().isEmpty() ? null : Long.parseLong(s));
				} else if (arg.equals("--help")) {
					System.out.println("GPX Animator 1.2.1");
					System.out.println("Copyright 2013 Martin Ždila, Freemap Slovakia");
					System.out.println();
					System.out.println("Usage:");
					final PrintWriter pw = new PrintWriter(System.out);
					Help.printHelp(new Help.PrintWriterOptionHelpWriter(pw));
					pw.flush();
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
