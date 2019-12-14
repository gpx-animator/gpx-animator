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

import sk.freemap.gpxAnimator.ui.TrackIcon;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.io.File;

@XmlAccessorType(XmlAccessType.FIELD)
public class TrackConfiguration {

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File inputGpx;

    private String label;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color color;

    private Long timeOffset;
    private Long forcedPointInterval;
    private Long trimGpxStart;
    private Long trimGpxEnd;
    private float lineWidth;

    @XmlJavaTypeAdapter(TrackIconXmlAdapter.class)
    private TrackIcon trackIcon;

    // for JAXB
    private TrackConfiguration() {
    }


    private TrackConfiguration(final File inputGpx, final String label, final Color color, final Long timeOffset, final Long forcedPointInterval, final Long trimGpxStart, final Long trimGpxEnd, final float lineWidth, final TrackIcon trackIcon) {
        this.inputGpx = inputGpx;
        this.label = label;
        this.color = color;
        this.timeOffset = timeOffset;
        this.forcedPointInterval = forcedPointInterval;
        this.trimGpxStart = trimGpxStart;
        this.trimGpxEnd = trimGpxEnd;
        this.lineWidth = lineWidth;
        this.trackIcon = trackIcon;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public TrackIcon getTrackIcon() {
        return trackIcon;
    }

    public File getInputGpx() {
        return inputGpx;
    }

    public String getLabel() {
        return label;
    }

    public Color getColor() {
        return color;
    }

    public Long getTimeOffset() {
        return timeOffset;
    }

    public Long getForcedPointInterval() {
        return forcedPointInterval;
    }

    public Long getTrimGpxStart() {
        return trimGpxStart;
    }

    public Long getTrimGpxEnd() {
        return trimGpxEnd;
    }

    public float getLineWidth() {
        return lineWidth;
    }

    public static class Builder {

        private File inputGpx;
        private String label;

        @XmlJavaTypeAdapter(ColorXmlAdapter.class)
        private Color color = Color.BLUE;

        private Long timeOffset;
        private Long forcedPointInterval;
        private Long trimGpxStart;
        private Long trimGpxEnd;
        private float lineWidth = 2f;
        private TrackIcon trackIcon = null;


        private Builder() {
        }


        public TrackConfiguration build() throws UserException {
            return new TrackConfiguration(inputGpx, label, color, timeOffset, forcedPointInterval, trimGpxStart, trimGpxEnd, lineWidth, trackIcon);
        }


        public Builder inputGpx(final File inputGpx) {
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


        public Builder timeOffset(final Long timeOffset) {
            this.timeOffset = timeOffset;
            return this;
        }


        public Builder forcedPointInterval(final Long forcedPointInterval) {
            this.forcedPointInterval = forcedPointInterval;
            return this;
        }


        public Builder trimGpxStart(final Long trimGpxStart) {
            this.trimGpxStart = trimGpxStart;
            return this;
        }


        public Builder trimGpxEnd(final Long trimGpxEnd) {
            this.trimGpxEnd = trimGpxEnd;
            return this;
        }


        public Builder lineWidth(final float lineWidth) {
            this.lineWidth = lineWidth;
            return this;
        }

        public Builder trackIcon(final TrackIcon trackIcon) {
            this.trackIcon = trackIcon;
            return this;
        }

    }

}
