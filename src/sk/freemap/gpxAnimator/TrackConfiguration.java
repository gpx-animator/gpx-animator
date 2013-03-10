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

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlAccessorType(XmlAccessType.FIELD)
public class TrackConfiguration {

	private String inputGpx;
	private String label;
	
	@XmlJavaTypeAdapter(ColorXmlAdapter.class)
	private Color color;
	
	private long timeOffset;
	private Long forcedPointInterval;
	private float lineWidth;
	
	
	// for JAXB
	private TrackConfiguration() {
	}
	
	
	private TrackConfiguration(final String inputGpx, final String label, final Color color, final long timeOffset, final Long forcedPointInterval, final float lineWidth) {
		this.inputGpx = inputGpx;
		this.label = label;
		this.color = color;
		this.timeOffset = timeOffset;
		this.forcedPointInterval = forcedPointInterval;
		this.lineWidth = lineWidth;
	}


	public String getInputGpx() {
		return inputGpx;
	}
	
	
	public String getLabel() {
		return label;
	}
	
	
	public Color getColor() {
		return color;
	}
	
	
	public long getTimeOffset() {
		return timeOffset;
	}
	
	
	public Long getForcedPointInterval() {
		return forcedPointInterval;
	}
	
	
	public float getLineWidth() {
		return lineWidth;
	}
	
	
	public static Builder createBuilder() {
		return new Builder();
	}

	
	public static class Builder {
		
		private String inputGpx = "";
		private String label = "";
		
		@XmlJavaTypeAdapter(ColorXmlAdapter.class)
		private Color color = Color.BLUE;
		
		private long timeOffset = 0l;
		private Long forcedPointInterval;
		private float lineWidth = 2f;
		
		
		private Builder() {
		}
		
		
		public TrackConfiguration build() throws UserException {
			return new TrackConfiguration(inputGpx, label, color, timeOffset, forcedPointInterval, lineWidth);
		}
		
		
		public Builder inputGpx(final String inputGpx) {
			this.inputGpx = inputGpx;
			return this;
		}
		
		
		public Builder label(final String label) {
			this.label = label;
			return this;
		}
		
		
		public Builder color(final Color color) {
			this.color = color;
			return this;
		}
		
		
		public Builder timeOffset(final long timeOffset) {
			this.timeOffset = timeOffset;
			return this;
		}
		
		
		public Builder forcedPointInterval(final Long forcedPointInterval) {
			this.forcedPointInterval = forcedPointInterval;
			return this;
		}
		
		
		public Builder lineWidth(final float lineWidth) {
			this.lineWidth = lineWidth;
			return this;
		}
		
	}

}
