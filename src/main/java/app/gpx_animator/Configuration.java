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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable, it uses XML transformation
public final class Configuration {

    private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
    public static final long DEFAULT_PHOTO_ANIMATION_DURATION = 700L;

    private int margin;
    private Integer width;
    private Integer height;
    private Integer zoom;

    private Double speedup;
    private long tailDuration;
    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color tailColor;
    private double fps;
    private Long totalTime;

    private float backgroundMapVisibility;
    private String tmsUrlTemplate;

    private boolean skipIdle;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color flashbackColor;
    private Long flashbackDuration;

    private Long keepLastFrame;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File output;
    private String attribution;
    private Position attributionPosition = Position.BOTTOM_LEFT;
    private Position informationPosition = Position.BOTTOM_RIGHT;

    private int fontSize;
    private String fontName;
    private String fontStyle;

    private Double markerSize;
    private Double waypointSize;

    private Double minLon;
    private Double maxLon;
    private Double minLat;
    private Double maxLat;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File logo;
    private Position logoPosition = Position.TOP_LEFT;

    private String photoDirectory;
    private Long photoTime;
    private Long photoAnimationDuration = DEFAULT_PHOTO_ANIMATION_DURATION;

    //@XmlJavaTypeAdapter(SpeedUnitAdapter.class)
    private SpeedUnit speedUnit;

    @XmlElementWrapper
    @XmlElement(name = "trackConfiguration") //NON-NLS
    private List<TrackConfiguration> trackConfigurationList;


    // for JAXB
    @SuppressWarnings("unused")
    private Configuration() {
    }

    @SuppressWarnings("checkstyle:ParameterNumber")
    private Configuration(
            final int margin, final Integer width, final Integer height, final Integer zoom,
            final Double speedup, final long tailDuration, final Color tailColor, final double fps, final Long totalTime,
            final float backgroundMapVisibility, final String tmsUrlTemplate, final boolean skipIdle,
            final Color backgroundColor, final Color flashbackColor, final Long flashbackDuration,
            final Long keepLastFrame, final File output, final String attribution, final SpeedUnit speedUnit,
            final int fontSize, final String fontName, final String fontStyle, final Double markerSize, final Double waypointSize,
            final Double minLon, final Double maxLon, final Double minLat, final Double maxLat,
            final File logo, final Position logoPosition, final Position attributionPosition, final Position informationPosition,
            final String photoDirectory, final Long photoTime, final Long photoAnimationDuration,
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
        this.backgroundColor = backgroundColor;
        this.flashbackColor = flashbackColor;
        this.flashbackDuration = flashbackDuration;
        this.keepLastFrame = keepLastFrame;
        this.output = output;
        this.attribution = attribution;
        this.fontSize = fontSize;
        this.fontStyle = fontStyle;
        this.fontName = fontName;
        this.markerSize = markerSize;
        this.waypointSize = waypointSize;
        this.trackConfigurationList = trackConfigurationList;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.logo = logo;
        this.logoPosition = logoPosition;
        this.attributionPosition = attributionPosition;
        this.informationPosition = informationPosition;
        this.photoDirectory = photoDirectory;
        this.photoTime = photoTime;
        this.photoAnimationDuration = photoAnimationDuration;
        this.speedUnit = speedUnit;
    }

    public static Builder createBuilder() {
        return new Builder();
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

    public SpeedUnit getSpeedUnit(){
        return speedUnit;
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

    public Color getBackgroundColor() {
        return backgroundColor;
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

    public String getFontStyle() {
        return fontStyle;
    }

    public String getFontName() {
        return fontName;
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

    public File getLogo() {
        return logo;
    }

    public Position getLogoPosition() {
        return logoPosition;
    }

    public Position getAttributionPosition() {
        return attributionPosition;
    }

    public Position getInformationPosition() {
        return informationPosition;
    }

    public String getPhotoDirectory() {
        return photoDirectory;
    }

    public Long getPhotoTime() {
        return photoTime;
    }

    public Long getPhotoAnimationDuration() {
        return photoAnimationDuration;
    }

    public List<TrackConfiguration> getTrackConfigurationList() {
        return trackConfigurationList;
    }

    @SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"}) // for debugging
    @Override
    public String toString() { // TODO Use a library for this kind of tasks, maybe Lombok.
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
                + ", backgroundColor=" + backgroundColor
                + ", flashbackColor=" + flashbackColor
                + ", flashbackDuration=" + flashbackDuration
                + ", output=" + output
                + ", fontSize=" + fontSize
                + ", fontName=" + fontName
                + ", fontStyle=" + fontStyle
                + ", markerSize=" + markerSize
                + ", waypointSize=" + waypointSize
                + ", keepLastFrame=" + keepLastFrame
                + ", logo=" + logo
                + ", logoPosition=" + logoPosition
                + ", attributionPosition=" + attributionPosition
                + ", informationPosition=" + informationPosition
                + ", photoDirectory=" + photoDirectory
                + ", photoTime=" + photoTime
                + ", photoAnimationDuration=" + photoAnimationDuration
                + ", trackConfigurationList=" + trackConfigurationList
                + ", unitOfSpeed=" + speedUnit
                + "]";
    }

    @SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "checkstyle:HiddenField", "UnusedReturnValue"}) // This is okay for the builder pattern
    public static final class Builder {
        private final ResourceBundle resourceBundle = Preferences.getResourceBundle();
        private final List<TrackConfiguration> trackConfigurationList = new ArrayList<>();
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
        private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
        private Color flashbackColor = Color.white;
        private Long flashbackDuration = 250L;
        private Long keepLastFrame;
        private File output = new File(Preferences.getLastWorkingDir() + Preferences.FILE_SEPARATOR + "GPX-Animation.mp4");
        private String attribution = resourceBundle.getString("configuration.attribution");
        private int fontSize = 12;
        private String fontStyle;
        private String fontName;
        private Double markerSize = 8.0;
        private Double waypointSize = 6.0;
        private Double minLon;
        private Double maxLon;
        private Double minLat;
        private Double maxLat;
        private File logo;
        private Position logoPosition;
        private Position attributionPosition;
        private Position informationPosition;
        private String photoDirectory;
        private Long photoTime = 3_000L;
        private Long photoAnimationDuration = DEFAULT_PHOTO_ANIMATION_DURATION;
        private SpeedUnit speedUnit;


        public Configuration build() {
            return new Configuration(
                    margin, width, height, zoom,
                    speedup, tailDuration, tailColor, fps, totalTime,
                    backgroundMapVisibility, tmsUrlTemplate,
                    skipIdle, backgroundColor, flashbackColor, flashbackDuration,
                    keepLastFrame, output, attribution, speedUnit,
                    fontSize, fontName, fontStyle, markerSize, waypointSize,
                    minLon, maxLon, minLat, maxLat,
                    logo, logoPosition, attributionPosition, informationPosition,
                    photoDirectory, photoTime, photoAnimationDuration,
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

        public Builder tailColor(final Color tailColor) {
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

        public Builder backgroundColor(final Color backgroundColor) {
            this.backgroundColor = backgroundColor;
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

        public Builder fontName(final String fontName) {
            this.fontName = fontName;
            return this;
        }

        public Builder fontStyle(final String fontStyle) {
            this.fontStyle = fontStyle;
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

        public Builder logo(final File logo) {
            this.logo = logo;
            return this;
        }

        public Builder logoPosition(final Position logoPosition) {
            this.logoPosition = logoPosition;
            return this;
        }

        public Builder attributionPosition(final Position attributionPosition) {
            this.attributionPosition = attributionPosition;
            return this;
        }

        public Builder informationPosition(final Position informationPosition) {
            this.informationPosition = informationPosition;
            return this;
        }

        public Builder photoDirectory(final String photoDirectory) {
            this.photoDirectory = photoDirectory;
            return this;
        }

        public Builder photoTime(final Long photoTime) {
            this.photoTime = photoTime;
            return this;
        }

        public Builder photoAnimationDuration(final Long photoAnimationDuration) {
            this.photoAnimationDuration = photoAnimationDuration;
            return this;
        }

        public Builder addTrackConfiguration(final TrackConfiguration trackConfiguration) {
            this.trackConfigurationList.add(trackConfiguration);
            return this;
        }

        public Builder speedUnit(final SpeedUnit speedUnit) {
            this.speedUnit = speedUnit;
            return this;
        }
    }

}
