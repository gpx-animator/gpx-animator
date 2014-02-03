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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
	
	private int margin;
	private Integer width;
	private Integer height;
	private Integer zoom;
	
	private Double speedup;
	private long tailDuration;
	private double fps;
	private Long totalTime;
	
	private float backgroundMapVisibility;
	private String tmsUrlTemplate;

	private boolean skipIdle;
	
	@XmlJavaTypeAdapter(ColorXmlAdapter.class)
	private Color flashbackColor;
	private Long flashbackDuration;
	
	private String output;
	private String attribution;
	
	private int fontSize;
	private Double markerSize;
	private Double waypointSize;


	@XmlElementWrapper
	@XmlElement(name = "trackConfiguration")
	private List<TrackConfiguration> trackConfigurationList;
	
	
	// for JAXB
	@SuppressWarnings("unused")
	private Configuration() {
		
	}
	
	public Configuration(
			final int margin, final Integer width, final Integer height, final Integer zoom,
			final Double speedup, final long tailDuration, final double fps, final Long totalTime,
			final float backgroundMapVisibility, final String tmsUrlTemplate,
			final boolean skipIdle, final Color flashbackColor, final Long flashbackDuration,
			final String output, final String attribution,
			final int fontSize, final Double markerSize, final Double waypointSize,
			final List<TrackConfiguration> trackConfigurationList) {
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
		this.output = output;
		this.attribution = attribution;
		this.fontSize = fontSize;
		this.markerSize = markerSize;
		this.waypointSize = waypointSize;
		this.trackConfigurationList = trackConfigurationList;
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
	
	
	public Color getFlashbackColor() {
		return flashbackColor;
	}
	
	
	public Long getFlashbackDuration() {
		return flashbackDuration;
	}
	
	
	public String getOutput() {
		return output;
	}
	
	
	public String getAttribution() {
		return attribution;
	}
	
	
	public int getFontSize() {
		return fontSize;
	}
	
	
	public Double getMarkerSize() {
		return markerSize;
	}
	
	
	public Double getWaypointSize() {
		return waypointSize;
	}

	
	public List<TrackConfiguration> getTrackConfigurationList() {
		return trackConfigurationList;
	}
	
	
	public static Builder createBuilder() {
		return new Builder();
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
		
		private float backgroundMapVisibility = 0.5f;
		private String tmsUrlTemplate;

		private boolean skipIdle = true;
		private Color flashbackColor = Color.white;
		private Long flashbackDuration = 250l;
		
		private String output = "video.mp4"; // frame%08d.png
		private String attribution = "Created by GPX Animator 1.2.0\n%MAP_ATTRIBUTION%";
		
		private int fontSize = 12;
		private Double markerSize = 8.0;
		private Double waypointSize = 6.0;

		private final List<TrackConfiguration> trackConfigurationList = new ArrayList<TrackConfiguration>();
		

		public Configuration build() throws UserException {
			return new Configuration(
					margin, width, height, zoom,
					speedup, tailDuration, fps, totalTime,
					backgroundMapVisibility, tmsUrlTemplate,
					skipIdle, flashbackColor, flashbackDuration,
					output, attribution,
					fontSize, markerSize, waypointSize,
					Collections.unmodifiableList(trackConfigurationList)
			);
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

		public Builder flashbackDuration(final Long flashbackDuration) {
			this.flashbackDuration = flashbackDuration;
			return this;
		}

		public Builder output(final String output) {
			this.output = output;
			return this;
		}
		
		public Builder attribution(final String attribution) {
			this.attribution = attribution;
			return this;
		}

		public Builder fontSize(final int fontSize) {
			this.fontSize = fontSize;
			return this;
		}

		public Builder markerSize(final Double markerSize) {
			this.markerSize = markerSize;
			return this;
		}

		public Builder waypointSize(final Double waypointSize) {
			this.waypointSize = waypointSize;
			return this;
		}

		public Builder addTrackConfiguration(final TrackConfiguration trackConfiguration) {
			this.trackConfigurationList.add(trackConfiguration);
			return this;
		}
	}


	@Override
	public String toString() {
		return "Configuration [margin=" + margin
				+ ", width=" + width
				+ ", height=" + height
				+ ", zoom=" + zoom
				+ ", speedup=" + speedup
				+ ", tailDuration=" + tailDuration
				+ ", fps=" + fps + ", totalTime=" + totalTime
				+ ", backgroundMapVisibility=" + backgroundMapVisibility
				+ ", tmsUrlTemplate=" + tmsUrlTemplate
				+ ", skipIdle=" + skipIdle
				+ ", flashbackColor=" + flashbackColor
				+ ", flashbackDuration=" + flashbackDuration
				+ ", output=" + output
				+ ", fontSize=" + fontSize
				+ ", markerSize=" + markerSize
				+ ", waypointSize=" + waypointSize
				+ ", trackConfigurationList=" + trackConfigurationList
				+ "]";
	}
	

}