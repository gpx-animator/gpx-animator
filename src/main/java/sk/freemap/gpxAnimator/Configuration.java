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

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class Configuration {
	
	private int margin;
	private Integer width;
	private Integer height;
	private Integer zoom;
	
	private Double speedup;
	private long tailDuration;
	private Color tailColor;
	private double fps;
	private Long totalTime;
	
	private float backgroundMapVisibility;
	private String tmsUrlTemplate;

	private boolean skipIdle;
	
	@XmlJavaTypeAdapter(ColorXmlAdapter.class)
	private Color flashbackColor;
	private Long flashbackDuration;

	private Long keepLastFrame;

	@XmlJavaTypeAdapter(FileXmlAdapter.class)
	private File output;
	private String attribution;
	
	private int fontSize;
	private Double markerSize;
	private Double waypointSize;

	private Double minLon;
	private Double maxLon;
	private Double minLat;
	private Double maxLat;

	private File photos;
	private Long photoTime;
	
	private String tileCachePath;
	private Long tileCacheTimeLimit;    // Time limit is in seconds

	@XmlElementWrapper
	@XmlElement(name = "trackConfiguration")
	private List<TrackConfiguration> trackConfigurationList;
	
	
	// for JAXB
	@SuppressWarnings("unused")
	private Configuration() {
		
	}
	
	public Configuration(
			final int margin, final Integer width, final Integer height, final Integer zoom,
			final Double speedup, final long tailDuration, final Color tailColor, final double fps, final Long totalTime,
			final float backgroundMapVisibility, final String tmsUrlTemplate,
			final boolean skipIdle, final Color flashbackColor, final Long flashbackDuration,
			final Long keepLastFrame, final File output, final String attribution,
			final int fontSize, final Double markerSize, final Double waypointSize,
			final Double minLon, final Double maxLon, final Double minLat, final Double maxLat,
			final File photos, final Long photoTime,
			final String tileCachePath, final Long tileCacheTimeLimit,
			final List<TrackConfiguration> trackConfigurationList) {
		
		this.margin = margin;
		this.width = width;
		this.height = height;
		this.zoom = zoom;
		this.speedup = speedup;
		this.tailDuration = tailDuration;
		this.tailColor = tailColor;
		this.fps = fps;
		this.totalTime = totalTime;
		this.backgroundMapVisibility = backgroundMapVisibility;
		this.tmsUrlTemplate = tmsUrlTemplate;
		this.skipIdle = skipIdle;
		this.flashbackColor = flashbackColor;
		this.flashbackDuration = flashbackDuration;
		this.keepLastFrame = keepLastFrame;
		this.output = output;
		this.attribution = attribution;
		this.fontSize = fontSize;
		this.markerSize = markerSize;
		this.waypointSize = waypointSize;
		this.trackConfigurationList = trackConfigurationList;
		this.minLon = minLon;
		this.maxLon = maxLon;
		this.minLat = minLat;
		this.maxLat = maxLat;
		this.photos = photos;
		this.photoTime = photoTime;
		this.tileCachePath = tileCachePath;
		this.tileCacheTimeLimit = tileCacheTimeLimit;
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


	public Color getTailColor() {
		return tailColor;
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
	
	
	public Long getKeepLastFrame() {
		return keepLastFrame;
	}
	
	
	public File getOutput() {
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

	public Double getMinLon() {
		return minLon;
	}


	public Double getMaxLon() {
		return maxLon;
	}


	public Double getMinLat() {
		return minLat;
	}


	public Double getMaxLat() {
		return maxLat;
	}


	public File getPhotos() {
		return photos;
	}


	public Long getPhotoTime() {
		return photoTime;
	}


    public String getTileCachePath() {
        return tileCachePath;
    }


    public Long getTileCacheTimeLimit() {
        return tileCacheTimeLimit == null ? 0 : tileCacheTimeLimit;
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
		private Color tailColor = Color.BLACK;
		private double fps = 30.0;
		private Long totalTime;
		
		private float backgroundMapVisibility = 0.5f;
		private String tmsUrlTemplate;

		private boolean skipIdle = true;
		private Color flashbackColor = Color.white;
		private Long flashbackDuration = 250l;

		private Long keepLastFrame;

		private File output = new File("video.mp4"); // frame%08d.png
		private String attribution = "Created by GPX Animator " + Constants.VERSION + "\n%MAP_ATTRIBUTION%";
		
		private int fontSize = 12;
		private Double markerSize = 8.0;
		private Double waypointSize = 6.0;

		private Double minLon;
		private Double maxLon;
		private Double minLat;
		private Double maxLat;

		private File photos;
		private Long photoTime = 3_000L;
		
		private String tileCachePath;
		private long tileCacheTimeLimit = 12L * 60L * 60L; // Default is 12 hours

		private final List<TrackConfiguration> trackConfigurationList = new ArrayList<TrackConfiguration>();
		

		public Configuration build() throws UserException {
			return new Configuration(
					margin, width, height, zoom,
					speedup, tailDuration, tailColor, fps, totalTime,
					backgroundMapVisibility, tmsUrlTemplate,
					skipIdle, flashbackColor, flashbackDuration,
					keepLastFrame, output, attribution,
					fontSize, markerSize, waypointSize,
					minLon,	maxLon,	minLat,	maxLat,
					photos, photoTime,
					tileCachePath, tileCacheTimeLimit,

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

		public Builder tailColor(Color tailColor) {
			this.tailColor = tailColor;
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

		public Builder keepLastFrame(final Long keepLastFrame) {
			this.keepLastFrame = keepLastFrame;
			return this;
		}

		public Builder output(final File output) {
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

		public Builder minLat(final Double minLat) {
			this.minLat = minLat;
			return this;
		}

		public Builder maxLat(final Double maxLat) {
			this.maxLat = maxLat;
			return this;
		}

		public Builder minLon(final Double minLon) {
			this.minLon = minLon;
			return this;
		}

		public Builder maxLon(final Double maxLon) {
			this.maxLon = maxLon;
			return this;
		}

		public Builder photos(final File photos) {
			this.photos = photos;
			return this;
		}

		public Builder photoTime(final Long photoTime) {
			this.photoTime = photoTime;
			return this;
		}

		public Builder tileCachePath(final String tileCachePath) {
			this.tileCachePath = tileCachePath;
			return this;
		}

		public Builder tileCacheTimeLimit(final Long tileCacheTimeLimit) {
			this.tileCacheTimeLimit = tileCacheTimeLimit;
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
				+ ", tailColor=" + tailColor
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
				+ ", photos=" + photos
				+ ", photoTime=" + photoTime
				+ ", tileCachePath=" + tileCachePath
				+ ", tileCacheTimeLimit=" + tileCacheTimeLimit
				+ ", trackConfigurationList=" + trackConfigurationList
				+ "]";
	}

}
