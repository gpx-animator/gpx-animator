/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.configuration;

import app.gpx_animator.core.configuration.adapter.ColorXmlAdapter;
import app.gpx_animator.core.configuration.adapter.FileXmlAdapter;
import app.gpx_animator.core.configuration.adapter.TrackIconXmlAdapter;
import app.gpx_animator.core.data.TrackIcon;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.awt.Color;
import java.io.File;

@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable, it uses XML transformation
public final class TrackConfiguration {

    public static final Color DEFAULT_PRE_DRAW_TRACK_COLOR = Color.lightGray;
    public static final float DEFAULT_PRE_DRAW_LINE_WIDTH = 2f;
    public static final float DEFAULT_LINE_WIDTH = 2f;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File inputGpx;

    private String label;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color color;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color preDrawTrackColor = DEFAULT_PRE_DRAW_TRACK_COLOR;

    private Long timeOffset;
    private Long forcedPointInterval;
    private Long trimGpxStart;
    private Long trimGpxEnd;
    private float lineWidth;
    private float preDrawLineWidth = DEFAULT_PRE_DRAW_LINE_WIDTH;
    private File inputIcon;
    private boolean mirrorTrackIcon;

    @XmlJavaTypeAdapter(TrackIconXmlAdapter.class)
    private TrackIcon trackIcon;

    private File inputEndIcon;
    private boolean mirrorTrackEndIcon;

    @XmlJavaTypeAdapter(TrackIconXmlAdapter.class)
    private TrackIcon trackEndIcon;


    @SuppressWarnings("unused") // Needed for JAXB deserialization from saved XML files
    private TrackConfiguration() {
    }


    @SuppressWarnings({"checkstyle:ParameterNumber", "java:S107"})
    private TrackConfiguration(final File inputGpx, final String label, final Color color, final Color preDrawTrackColor, final Long timeOffset,
                               final Long forcedPointInterval, final Long trimGpxStart, final Long trimGpxEnd, final float lineWidth,
                               final float preDrawLineWidth, final TrackIcon trackIcon, final File inputIcon, final boolean mirrorTrackIcon,
                               final TrackIcon trackEndIcon, final File inputEndIcon, final boolean mirrorTrackEndIcon) {
        this.inputGpx = inputGpx;
        this.label = label;
        this.color = color;
        this.preDrawTrackColor = preDrawTrackColor;
        this.timeOffset = timeOffset;
        this.forcedPointInterval = forcedPointInterval;
        this.trimGpxStart = trimGpxStart;
        this.trimGpxEnd = trimGpxEnd;
        this.lineWidth = lineWidth;
        this.preDrawLineWidth = preDrawLineWidth;
        this.trackIcon = trackIcon;
        this.inputIcon = inputIcon;
        this.mirrorTrackIcon = mirrorTrackIcon;
        this.inputEndIcon = inputEndIcon;
        this.mirrorTrackEndIcon = mirrorTrackEndIcon;
        this.trackEndIcon = trackEndIcon;
    }

    public static Builder createBuilder() {
        return new Builder();
    }

    public TrackIcon getTrackIcon() {
        return trackIcon;
    }

    public boolean isTrackIconMirrored() {
        return mirrorTrackIcon;
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

    public Color getPreDrawTrackColor() {
        return preDrawTrackColor;
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

    public float getPreDrawLineWidth() {
        return preDrawLineWidth;
    }

    public File getInputEndIcon() {
        return inputEndIcon;
    }

    public boolean isMirrorTrackEndIcon() {
        return mirrorTrackEndIcon;
    }

    public TrackIcon getTrackEndIcon() {
        return trackEndIcon;
    }


    @SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "checkstyle:HiddenField", "UnusedReturnValue"}) // This is okay for the builder pattern
    public static final class Builder {

        private File inputGpx;
        private String label;

        private Color color = Color.BLUE;
        private Color preDrawTrackColor = DEFAULT_PRE_DRAW_TRACK_COLOR;

        private Long timeOffset;
        private Long forcedPointInterval;
        private Long trimGpxStart;
        private Long trimGpxEnd;
        private float lineWidth = DEFAULT_LINE_WIDTH;
        private float preDrawLineWidth = DEFAULT_PRE_DRAW_LINE_WIDTH;
        private TrackIcon trackIcon = null;
        private File inputIcon;
        private boolean mirrorTrackIcon = false;

        private TrackIcon trackEndIcon = null;
        private File inputEndIcon;
        private boolean mirrorTrackEndIcon = false;

        private Builder() {
        }


        public TrackConfiguration build() {
            return new TrackConfiguration(
                    inputGpx, label, color, preDrawTrackColor, timeOffset, forcedPointInterval, trimGpxStart, trimGpxEnd, lineWidth, preDrawLineWidth,
                    trackIcon, inputIcon, mirrorTrackIcon, trackEndIcon, inputEndIcon, mirrorTrackEndIcon
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


        public Builder preDrawTrackColor(final Color preDrawTrackColor) {
            this.preDrawTrackColor = preDrawTrackColor;
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

        public Builder preDrawLineWidth(final float preDrawLineWidth) {
            this.preDrawLineWidth = preDrawLineWidth;
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

        public Builder mirrorTrackIcon(final boolean mirrorTrackIcon) {
            this.mirrorTrackIcon = mirrorTrackIcon;
            return this;
        }

        public Builder trackEndIcon(final TrackIcon trackEndIcon) {
            this.trackEndIcon = trackEndIcon;
            return this;
        }

        public Builder inputEndIcon(final File inputEndIcon) {
            this.inputEndIcon = inputEndIcon;
            return this;
        }

        public Builder mirrorTrackEndIcon(final boolean mirrorTrackEndIcon) {
            this.mirrorTrackEndIcon = mirrorTrackEndIcon;
            return this;
        }
    }

}
