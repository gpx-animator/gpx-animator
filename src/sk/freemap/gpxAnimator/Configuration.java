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
import java.util.Collections;
import java.util.List;

public class Configuration {
	
	private final int margin;
	private final Integer width;
	private final Integer height;
	private final Integer zoom;
	
	private final Double speedup;
	private final long tailDuration ;
	private final double fps;
	private final Long totalTime;
	
	private final float backgroundMapVisibility;
	private final String tmsUrlTemplate;

	private final boolean skipIdle;
	private final Color flashbackColor;
	private final float flashbackDuration;
	
	private final String frameFilePattern;
	
	private final int fontSize;
	private final double markerSize;
	private final double waypointSize;

	private final List<String> inputGpxList;
	private final List<String> labelList;
	private final List<Color> colorList;
	private final List<Long> timeOffsetList;
	private final List<Long> forcedPointIntervalList;
	private final List<Float> lineWidthList;
	
	
	public Configuration(
			final int margin, final Integer width, final Integer height, final Integer zoom,
			final Double speedup, final long tailDuration, final double fps, final Long totalTime,
			final float backgroundMapVisibility, final String tmsUrlTemplate,
			final boolean skipIdle, final Color flashbackColor, final float flashbackDuration,
			final String frameFilePattern,
			final int fontSize, final double markerSize, final double waypointSize,
			final List<String> inputGpxList, final List<String> labelList, final List<Color> colorList, final List<Long> timeOffsetList,
			final List<Long> forcedPointIntervalList, final List<Float> lineWidthList) {
		this.margin = margin;
		this.width = width;
		this.height = height;
		this.zoom = zoom;
		this.speedup = speedup;
		this.tailDuration = tailDuration;
		this.fps = fps;
		this.totalTime = totalTime;
		this.backgroundMapVisibility = backgroundMapVisibility;
		this.tmsUrlTemplate = tmsUrlTemplate;
		this.skipIdle = skipIdle;
		this.flashbackColor = flashbackColor;
		this.flashbackDuration = flashbackDuration;
		this.frameFilePattern = frameFilePattern;
		this.fontSize = fontSize;
		this.markerSize = markerSize;
		this.waypointSize = waypointSize;
		this.inputGpxList = inputGpxList;
		this.labelList = labelList;
		this.colorList = colorList;
		this.timeOffsetList = timeOffsetList;
		this.forcedPointIntervalList = forcedPointIntervalList;
		this.lineWidthList = lineWidthList;
	}


	public int getMargin() {
		return margin;
	}
	
	
	public Integer getWidth() {
		return width;
	}
	
	
	public Integer getHeight() {
		return height;
	}
	
	
	public Integer getZoom() {
		return zoom;
	}
	
	
	public Double getSpeedup() {
		return speedup;
	}
	
	
	public long getTailDuration() {
		return tailDuration;
	}
	
	
	public double getFps() {
		return fps;
	}
	
	
	public Long getTotalTime() {
		return totalTime;
	}
	
	
	public float getBackgroundMapVisibility() {
		return backgroundMapVisibility;
	}
	
	
	public String getTmsUrlTemplate() {
		return tmsUrlTemplate;
	}
	
	
	public boolean isSkipIdle() {
		return skipIdle;
	}
	
	
	public static Builder createBuilder() {
		return new Builder();
	}
	
	
	public Color getFlashbackColor() {
		return flashbackColor;
	}
	
	
	public float getFlashbackDuration() {
		return flashbackDuration;
	}
	
	
	public String getFrameFilePattern() {
		return frameFilePattern;
	}
	
	
	public int getFontSize() {
		return fontSize;
	}
	
	
	public double getMarkerSize() {
		return markerSize;
	}
	
	
	public double getWaypointSize() {
		return waypointSize;
	}

	public List<String> getInputGpxList() {
		return inputGpxList;
	}

	
	public List<String> getLabelList() {
		return labelList;
	}

	
	public List<Color> getColorList() {
		return colorList;
	}
	
	
	public List<Long> getTimeOffsetList() {
		return timeOffsetList;
	}

	
	public List<Long> getForcedPointIntervalList() {
		return forcedPointIntervalList;
	}

	
	public List<Float> getLineWidthList() {
		return lineWidthList;
	}
	
	
	public static class Builder {
		private int margin = 20;
		private Integer height;
		private Integer width;
		private Integer zoom;
		
		private Double speedup = 1000.0;
		private long tailDuration = 3600000;
		private double fps = 30.0;
		private Long totalTime;
		
		private float backgroundMapVisibility = 50f;
		private String tmsUrlTemplate; // http://tile.openstreetmap.org/{zoom}/{x}/{y}.png, http://aio.freemap.sk/T/{zoom}/{x}/{y}.png

		private boolean skipIdle = true;
		private Color flashbackColor = Color.white;
		private float flashbackDuration = 250f;
		
		private String frameFilePattern = "frame%08d.png";
		
		private int fontSize = 12;
		private double markerSize = 8.0;
		private double waypointSize = 6.0;

		private final List<String> inputGpxList = new ArrayList<String>();
		private final List<String> labelList = new ArrayList<String>();
		private final List<Color> colorList = new ArrayList<Color>();
		private final List<Long> timeOffsetList = new ArrayList<Long>();
		private final List<Long> forcedPointIntervalList = new ArrayList<Long>();
		private final List<Float> lineWidthList = new ArrayList<Float>();
		

		public Configuration build() throws UserException {
			validateOptions();
			normalizeColors();
			normalizeLineWidths();
			
			return new Configuration(
					margin, height, width, zoom,
					speedup, tailDuration, fps, totalTime,
					backgroundMapVisibility, tmsUrlTemplate,
					skipIdle, flashbackColor, flashbackDuration,
					frameFilePattern,
					fontSize, markerSize, waypointSize,
					Collections.unmodifiableList(inputGpxList),
					Collections.unmodifiableList(labelList),
					Collections.unmodifiableList(colorList),
					Collections.unmodifiableList(timeOffsetList),
					Collections.unmodifiableList(forcedPointIntervalList),
					Collections.unmodifiableList(lineWidthList)
			);
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


		public Builder margin(final int margin) {
			this.margin = margin;
			return this;
		}

		public Builder height(final Integer height) {
			this.height = height;
			return this;
		}
		
		public Builder width(final Integer width) {
			this.width = width;
			return this;
		}
		
		public Builder zoom(final Integer zoom) {
			this.zoom = zoom;
			return this;
		}
		
		public Builder speedup(final Double speedup) {
			this.speedup = speedup;
			return this;
		}
		
		public Builder tailDuration(final long tailDuration) {
			this.tailDuration = tailDuration;
			return this;
		}
		
		public Builder fps(final double fps) {
			this.fps = fps;
			return this;
		}
		
		public Builder totalTime(final Long totalTime) {
			this.totalTime = totalTime;
			return this;
		}

		public Builder backgroundMapVisibility(final float backgroundMapVisibility) {
			this.backgroundMapVisibility = backgroundMapVisibility;
			return this;
		}

		public Builder tmsUrlTemplate(final String tmsUrlTemplate) {
			this.tmsUrlTemplate = tmsUrlTemplate;
			return this;
		}

		public Builder skipIdle(final boolean skipIdle) {
			this.skipIdle = skipIdle;
			return this;
		}

		public Builder flashbackColor(final Color flashbackColor) {
			this.flashbackColor = flashbackColor;
			return this;
		}

		public Builder flashbackDuration(final float flashbackDuration) {
			this.flashbackDuration = flashbackDuration;
			return this;
		}

		public Builder frameFilePattern(final String frameFilePattern) {
			this.frameFilePattern = frameFilePattern;
			return this;
		}

		public Builder fontSize(final int fontSize) {
			this.fontSize = fontSize;
			return this;
		}

		public Builder markerSize(final double markerSize) {
			this.markerSize = markerSize;
			return this;
		}

		public Builder waypointSize(final double waypointSize) {
			this.waypointSize = waypointSize;
			return this;
		}

		public Builder addInputGpx(final String inputGpx) {
			inputGpxList.add(inputGpx);
			return this;
		}

		public Builder addLabel(final String label) {
			labelList.add(label);
			return this;
		}

		public Builder addColor(final Color color) {
			colorList.add(color);
			return this;
		}

		public Builder addTimeOffset(final Long timeOffset) {
			timeOffsetList.add(timeOffset);
			return this;
		}

		public Builder addForcedPointInterval(final Long forcedPointInterval) {
			forcedPointIntervalList.add(forcedPointInterval);
			return this;
		}

		public Builder addLineWidth(final Float lineWidth) {
			lineWidthList.add(lineWidth);
			return this;
		}
		
	}
	

	@Override
	public String toString() {
		return "Configuration [margin=" + margin + ", width=" + width + ", height=" + height + ", zoom=" + zoom + ", speedup=" + speedup + ", tailDuration=" + tailDuration + ", fps=" + fps + ", totalTime=" + totalTime
				+ ", backgroundMapVisibility=" + backgroundMapVisibility + ", tmsUrlTemplate=" + tmsUrlTemplate + ", skipIdle=" + skipIdle + ", flashbackColor=" + flashbackColor + ", flashbackDuration=" + flashbackDuration
				+ ", frameFilePattern=" + frameFilePattern + ", fontSize=" + fontSize + ", markerSize=" + markerSize + ", waypointSize=" + waypointSize + ", inputGpxList=" + inputGpxList + ", labelList=" + labelList + ", colorList="
				+ colorList + ", timeOffsetList=" + timeOffsetList + ", forcedPointIntervalList=" + forcedPointIntervalList + ", lineWidthList=" + lineWidthList + "]";
	}

	
	
}