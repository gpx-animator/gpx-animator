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

public final class CommandLineConfigurationFactory {

	private CommandLineConfigurationFactory() {
		throw new AssertionError();
	}
	
	
	/**
	 * @param args
	 * @throws UserException
	 */
	public static Configuration createConfiguration(final String[] args) throws UserException {
		final Configuration cfg = new Configuration();
		
		for (int i = 0; i < args.length; i++) {
			final String arg = args[i];
			
			try {
				if (arg.equals("--input")) {
					cfg.inputGpxList.add(args[++i]);
				} else if (arg.equals("--output")) {
					cfg.frameFilePattern = args[++i];
				} else if (arg.equals("--label")) {
					cfg.labelList.add(args[++i]);
				} else if (arg.equals("--color")) {
					cfg.colorList.add(Color.decode(args[++i]));
				} else if (arg.equals("--margin")) {
					cfg.margin = Integer.parseInt(args[++i]);
				} else if (arg.equals("--time-offset")) {
					cfg.timeOffsetList.add(Long.parseLong(args[++i]));
				} else if (arg.equals("--forced-point-time-interval")) {
					cfg.forcedPointIntervalList.add(Long.parseLong(args[++i]));
				} else if (arg.equals("--speedup")) {
					cfg.speedup = Double.parseDouble(args[++i]);
				} else if (arg.equals("--line-width")) {
					cfg.lineWidthList.add(Float.parseFloat(args[++i]));
				} else if (arg.equals("--tail-duration")) {
					cfg.tailDuration = Long.parseLong(args[++i]);
				} else if (arg.equals("--fps")) {
					cfg.fps = Double.parseDouble(args[++i]);
				} else if (arg.equals("--marker-size")) {
					cfg.markerSize = Double.parseDouble(args[++i]);
				} else if (arg.equals("--waypoint-size")) {
					cfg.waypointSize = Double.parseDouble(args[++i]);
				} else if (arg.equals("--width")) {
					cfg.width = Integer.parseInt(args[++i]);
				} else if (arg.equals("--height")) {
					cfg.height = Integer.parseInt(args[++i]);
				} else if (arg.equals("--zoom")) {
					cfg.zoom = Integer.parseInt(args[++i]);
				} else if (arg.equals("--font-size")) {
					cfg.fontSize = Integer.parseInt(args[++i]);
				} else if (arg.equals("--tms-url-template")) {
					cfg.tmsUrlTemplate = args[++i];
				} else if (arg.equals("--background-map-visibility")) {
					cfg.backgroundMapVisibility = Float.parseFloat(args[++i]);
				} else if (arg.equals("--total-time")) {
					cfg.totalTime = Double.parseDouble(args[++i]);
				} else if (arg.equals("--keep-idle")) {
					cfg.skipIdle = false;
				} else if (arg.equals("--flashback-color")) {
					final long lv = Long.decode(args[++i]).longValue();
					cfg.flashbackColor = new Color(lv < Integer.MAX_VALUE ? (int) lv : (int) (0xffffffff00000000L | lv), true);
				} else if (arg.equals("--flashback-duration")) {
					cfg.flashbackDuration = Float.parseFloat(args[++i]);
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
		
		return cfg;
	}

}
