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
package app.gpx_animator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.io.File;

@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable, it uses XML transformation
public final class TrackConfiguration {

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
    private boolean flipIcon;
    private File inputIcon;

    @XmlJavaTypeAdapter(TrackIconXmlAdapter.class)
    private TrackIcon trackIcon;

    @SuppressWarnings("unused") // Needed for JAXB deserialization from saved XML files
    private TrackConfiguration() {
    }


    @SuppressWarnings("checkstyle:ParameterNumber") // TODO This is too much and just a temporary solution not to break the build...
    private TrackConfiguration(final File inputGpx, final String label, final Color color, final Long timeOffset, final Long forcedPointInterval,
                               final Long trimGpxStart, final Long trimGpxEnd, final float lineWidth, final TrackIcon trackIcon,
                               final File inputIcon, final boolean flipIcon) {
        this.inputGpx = inputGpx;
        this.label = label;
        this.color = color;
        this.timeOffset = timeOffset;
        this.forcedPointInterval = forcedPointInterval;
        this.trimGpxStart = trimGpxStart;
        this.trimGpxEnd = trimGpxEnd;
        this.lineWidth = lineWidth;
        this.trackIcon = trackIcon;
        this.inputIcon = inputIcon;
        this.flipIcon = flipIcon;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public TrackIcon getTrackIcon() {
        return trackIcon;
    }

    public boolean getFlipIcon() {
        return flipIcon;
    }

    public File getInputGpx() {
        return inputGpx;
    }

    public File getInputIcon() {
        return inputIcon;
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

    @SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "checkstyle:HiddenField", "UnusedReturnValue"}) // This is okay for the builder pattern
    public static final class Builder {

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
        private File inputIcon;
        private boolean flipIcon = false;


        private Builder() {
        }


        public TrackConfiguration build() {
            return new TrackConfiguration(
                inputGpx, label, color, timeOffset, forcedPointInterval, trimGpxStart, trimGpxEnd, lineWidth, trackIcon, inputIcon, flipIcon
            );
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

        public Builder inputIcon(final File inputIcon) {
            this.inputIcon = inputIcon;
            return this;
        }

        public Builder flipIcon(final boolean flipIcon) {
            this.flipIcon = flipIcon;
            return this;
        }

    }

}
