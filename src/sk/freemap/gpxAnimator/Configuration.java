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

public class Configuration {

	int margin = 20;
	double speedup = 1000.0;
	long tailDuration = 3600;
	double fps = 30.0;
	double totalTime = Double.NaN;
	int width = 0;
	int height = 0;
	int zoom = 0;
	float backgroundMapVisibility = 50f;
	boolean skipIdle = true;
	String frameFilePattern = "frame%08d.png";
	String tmsUrlTemplate; // http://tile.openstreetmap.org/{zoom}/{x}/{y}.png, http://aio.freemap.sk/T/{zoom}/{x}/{y}.png
	final List<String> inputGpxList = new ArrayList<String>();
	final List<String> labelList = new ArrayList<String>();
	final List<Color> colorList = new ArrayList<Color>();
	final List<Long> timeOffsetList = new ArrayList<Long>();
	final List<Long> forcedPointIntervalList = new ArrayList<Long>();
	int fontSize = 12;
	final List<Float> lineWidthList = new ArrayList<Float>();
	
	Color flashbackColor = Color.white;
	float flashbackDuration = 250f;
	double markerSize = 8.0;
	double waypointSize = 6.0;
	
	
	public void validateOptions() throws UserException {
		if (inputGpxList.isEmpty()) {
			throw new UserException("missing input file");
		}
		
		if (String.format(frameFilePattern, 100).equals(String.format(frameFilePattern, 200))) {
			throw new UserException("--output must be pattern, for example frame%08d.png");
		}
		
		// TODO other validations
	}


	public void normalizeColors() {
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


	public void normalizeLineWidths() {
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