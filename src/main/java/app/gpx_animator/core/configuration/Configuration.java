/*
 *  Copyright Contributors to the GPX Animator project.
 *
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
package app.gpx_animator.core.configuration;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.adapter.ColorXmlAdapter;
import app.gpx_animator.core.configuration.adapter.FileXmlAdapter;
import app.gpx_animator.core.configuration.adapter.FontXmlAdapter;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.preferences.Preferences;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable, it uses XML transformation
public final class Configuration {

    private static final Color DEFAULT_BACKGROUND_COLOR = Color.white;
    private static final int DEFAULT_MARGIN = 20;
    private static final int DEFAULT_VIEWPORT_INERTIA = 50;
    public static final long DEFAULT_PHOTO_ANIMATION_DURATION = 700L;
    public static final Position DEFAULT_ATTRIBUTION_POSITION = Position.BOTTOM_LEFT;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private int margin = DEFAULT_MARGIN;
    private Integer width;
    private Integer height;
    private Integer zoom;

    private Integer viewportWidth;
    private Integer viewportHeight;
    private Integer viewportInertia = DEFAULT_VIEWPORT_INERTIA;

    private boolean preDrawTrack;

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
    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File backgroundImage;

    @XmlJavaTypeAdapter(ColorXmlAdapter.class)
    private Color flashbackColor;
    private Long flashbackDuration;

    private Long keepFirstFrame;
    private Long keepLastFrame;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File output;

    private String attribution;
    private Position attributionPosition = DEFAULT_ATTRIBUTION_POSITION;
    private int attributionMargin = DEFAULT_MARGIN;
    private Position informationPosition = Position.BOTTOM_RIGHT;
    private int informationMargin = DEFAULT_MARGIN;
    private Position commentPosition = Position.BOTTOM_CENTER;
    private int commentMargin = DEFAULT_MARGIN;

    @XmlJavaTypeAdapter(FontXmlAdapter.class)
    private Font font;

    private Double markerSize;

    @XmlJavaTypeAdapter(FontXmlAdapter.class)
    private Font waypointFont;
    private Double waypointSize;

    private Double minLon;
    private Double maxLon;
    private Double minLat;
    private Double maxLat;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File logo;
    private Position logoPosition = Position.TOP_LEFT;
    private int logoMargin = DEFAULT_MARGIN;

    @XmlJavaTypeAdapter(FileXmlAdapter.class)
    private File photoDirectory;
    private Long photoTime;
    private Long photoAnimationDuration = DEFAULT_PHOTO_ANIMATION_DURATION;

    private SpeedUnit speedUnit;

    private Long previewLength;

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
            final Integer viewportWidth, final Integer viewportHeight, final Integer viewportInertia,
            final Double speedup, final long tailDuration, final Color tailColor, final double fps, final Long totalTime,
            final float backgroundMapVisibility, final String tmsUrlTemplate, final boolean skipIdle,
            final Color backgroundColor, final File backgroundImage, final Color flashbackColor, final Long flashbackDuration,
            final boolean preDrawTrack, final Long keepFirstFrame, final Long keepLastFrame, final File output, final String attribution,
            final SpeedUnit speedUnit, final Font font, final Double markerSize, final Font waypointFont, final Double waypointSize,
            final Double minLon, final Double maxLon, final Double minLat, final Double maxLat,
            final File logo, final Position logoPosition, final int logoMargin,
            final Position attributionPosition, final int attributionMargin,
            final Position informationPosition, final int informationMargin,
            final Position commentPosition, final int commentMargin,
            final File photoDirectory, final Long photoTime, final Long photoAnimationDuration,
            final Long previewLength,
            final List<TrackConfiguration> trackConfigurationList) {

        this.margin = margin;
        this.width = width;
        this.height = height;
        this.zoom = zoom;
        this.viewportWidth = viewportWidth;
        this.viewportHeight = viewportHeight;
        this.viewportInertia = viewportInertia;
        this.speedup = speedup;
        this.tailDuration = tailDuration;
        this.tailColor = tailColor;
        this.fps = fps;
        this.totalTime = totalTime;
        this.backgroundMapVisibility = backgroundMapVisibility;
        this.tmsUrlTemplate = tmsUrlTemplate;
        this.skipIdle = skipIdle;
        this.preDrawTrack = preDrawTrack;
        this.backgroundColor = backgroundColor;
        this.backgroundImage = backgroundImage;
        this.flashbackColor = flashbackColor;
        this.flashbackDuration = flashbackDuration;
        this.keepFirstFrame = keepFirstFrame;
        this.keepLastFrame = keepLastFrame;
        this.output = output;
        this.attribution = attribution;
        this.font = font;
        this.markerSize = markerSize;
        this.waypointFont = waypointFont;
        this.waypointSize = waypointSize;
        this.trackConfigurationList = trackConfigurationList;
        this.minLon = minLon;
        this.maxLon = maxLon;
        this.minLat = minLat;
        this.maxLat = maxLat;
        this.logo = validateLogo(logo);
        this.logoPosition = logoPosition;
        this.logoMargin = logoMargin;
        this.attributionPosition = attributionPosition;
        this.attributionMargin = attributionMargin;
        this.informationPosition = informationPosition;
        this.informationMargin = informationMargin;
        this.commentPosition = commentPosition;
        this.commentMargin = commentMargin;
        this.photoDirectory = photoDirectory;
        this.photoTime = photoTime;
        this.photoAnimationDuration = photoAnimationDuration;
        this.speedUnit = speedUnit;
        this.previewLength = previewLength;
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

    public Integer getViewportWidth() {
        return viewportWidth;
    }

    public Integer getViewportHeight() {
        return viewportHeight;
    }

    public Integer getViewportInertia() {
        return viewportInertia;
    }

    public Integer getZoom() {
        return zoom;
    }

    public Double getSpeedup() {
        return speedup;
    }

    public SpeedUnit getSpeedUnit() {
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

    public boolean isPreDrawTrack() {
        return preDrawTrack;
    }

    public Color getBackgroundColor() {
        return backgroundColor;
    }

    public File getBackgroundImage() {
        return backgroundImage;
    }

    public Color getFlashbackColor() {
        return flashbackColor;
    }

    public Long getFlashbackDuration() {
        return flashbackDuration;
    }

    public Long getKeepFirstFrame() {
        return keepFirstFrame;
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

    public Font getFont() {
        return font != null ? font : Constants.DEFAULT_FONT;
    }

    public Double getMarkerSize() {
        return markerSize;
    }

    public Font getWaypointFont() {
        return waypointFont != null ? waypointFont : getFont();
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
        return validateLogo(logo);
    }

    public Position getLogoPosition() {
        return logoPosition;
    }

    public int getLogoMargin() {
        return logoMargin;
    }

    public Position getAttributionPosition() {
        return attributionPosition;
    }

    public int getAttributionMargin() {
        return attributionMargin;
    }

    public Position getInformationPosition() {
        return informationPosition;
    }

    public int getInformationMargin() {
        return informationMargin;
    }

    public Position getCommentPosition() {
        return commentPosition;
    }

    public int getCommentMargin() {
        return commentMargin;
    }

    public File getPhotoDirectory() {
        return photoDirectory;
    }

    public Long getPhotoTime() {
        return photoTime;
    }

    public Long getPhotoAnimationDuration() {
        return photoAnimationDuration;
    }

    public Long getPreviewLength() {
        return previewLength;
    }

    public List<TrackConfiguration> getTrackConfigurationList() {
        return Collections.unmodifiableList(trackConfigurationList);
    }

    private static File validateLogo(final File logo) {
        return logo != null && logo.isFile() ? logo : null;
    }

    public Configuration validate() throws UserException {
        final var errors = new ArrayList<String>();

        if (getMinLat() != null && getMaxLat() == null || getMaxLat() != null && getMinLat() == null) {
            errors.add(resourceBundle.getString("configuration.validation.latitude"));
        }
        if (getMinLon() != null && getMaxLon() == null || getMaxLon() != null && getMinLon() == null) {
            errors.add(resourceBundle.getString("configuration.validation.longitude"));
        }

        if (!errors.isEmpty()) {
            var message = errors.stream()
                    .map("- %s"::formatted)
                    .collect(Collectors.joining("\n"));
            throw new UserException("%s%n%s".formatted(resourceBundle.getString("configuration.validation.error"), message));
        }

        return this;
    }

    @SuppressWarnings({"PMD.AvoidFieldNameMatchingMethodName", "checkstyle:HiddenField", "UnusedReturnValue"}) // This is okay for the builder pattern
    public static final class Builder {
        private final ResourceBundle resourceBundle = Preferences.getResourceBundle();
        private final List<TrackConfiguration> trackConfigurationList = new ArrayList<>();
        private int margin = DEFAULT_MARGIN;
        private Integer height;
        private Integer width;
        private Integer zoom;
        private Integer viewportHeight;
        private Integer viewportWidth;
        private Integer viewportInertia = DEFAULT_VIEWPORT_INERTIA;
        private Double speedup = 1000.0;
        private long tailDuration = 3600000;
        private Color tailColor = Color.BLACK;
        private double fps = 30.0;
        private Long totalTime;
        private float backgroundMapVisibility = 0.5f;
        private String tmsUrlTemplate = "";
        private boolean skipIdle = true;
        private boolean preDrawTrack = false;
        private Color backgroundColor = DEFAULT_BACKGROUND_COLOR;
        private File backgroundImage;
        private Color flashbackColor = Color.white;
        private Long flashbackDuration = 250L;
        private Long keepFirstFrame;
        private Long keepLastFrame;
        private File output = new File(Preferences.getLastWorkingDir() + Preferences.FILE_SEPARATOR + "GPX-Animation.mp4");
        private String attribution = resourceBundle.getString("configuration.attribution");
        private Font font;
        private Double markerSize = 8.0;
        private Font waypointFont;
        private Double waypointSize = 6.0;
        private Double minLon;
        private Double maxLon;
        private Double minLat;
        private Double maxLat;
        private File logo;
        private Position logoPosition = Position.TOP_LEFT;
        private int logoMargin = DEFAULT_MARGIN;
        private Position attributionPosition = Position.BOTTOM_LEFT;
        private int attributionMargin = DEFAULT_MARGIN;
        private Position informationPosition = Position.BOTTOM_RIGHT;
        private int informationMargin = DEFAULT_MARGIN;
        private Position commentPosition = Position.BOTTOM_CENTER;
        private int commentMargin = DEFAULT_MARGIN;
        private File photoDirectory;
        private Long photoTime = 3_000L;
        private Long photoAnimationDuration = DEFAULT_PHOTO_ANIMATION_DURATION;
        private SpeedUnit speedUnit = SpeedUnit.KMH;
        private Long previewLength;


        public Configuration build() {
            return new Configuration(
                    margin, width, height, zoom,
                    viewportWidth, viewportHeight, viewportInertia,
                    speedup, tailDuration, tailColor, fps, totalTime,
                    backgroundMapVisibility, tmsUrlTemplate,
                    skipIdle, backgroundColor, backgroundImage, flashbackColor, flashbackDuration,
                    preDrawTrack, keepFirstFrame, keepLastFrame, output, attribution, speedUnit,
                    font, markerSize, waypointFont, waypointSize,
                    minLon, maxLon, minLat, maxLat,
                    logo, logoPosition, logoMargin,
                    attributionPosition, attributionMargin,
                    informationPosition, informationMargin,
                    commentPosition, commentMargin,
                    photoDirectory, photoTime, photoAnimationDuration,
                    previewLength,
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

        public Builder viewportHeight(final Integer viewportHeight) {
            this.viewportHeight = viewportHeight;
            return this;
        }

        public Builder viewportWidth(final Integer viewportWidth) {
            this.viewportWidth = viewportWidth;
            return this;
        }

        public Builder viewportInertia(final Integer viewportInertia) {
            this.viewportInertia = viewportInertia;
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

        public Builder backgroundImage(final File backgroundImage) {
            this.backgroundImage = backgroundImage;
            return this;
        }

        public Builder preDrawTrack(final boolean preDrawTrack) {
            this.preDrawTrack = preDrawTrack;
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

        public Builder keepFirstFrame(final Long keepFirstFrame) {
            this.keepFirstFrame = keepFirstFrame;
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

        public Builder font(final Font font) {
            this.font = font;
            return this;
        }

        public Builder markerSize(final Double markerSize) {
            this.markerSize = markerSize;
            return this;
        }

        public Builder waypointFont(final Font waypointFont) {
            this.waypointFont = waypointFont;
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
            this.logo = validateLogo(logo);
            return this;
        }

        public Builder logoPosition(final Position logoPosition) {
            this.logoPosition = logoPosition;
            return this;
        }

        public Builder logoMargin(final int logoMargin) {
            this.logoMargin = logoMargin;
            return this;
        }

        public Builder attributionPosition(final Position attributionPosition) {
            this.attributionPosition = attributionPosition;
            return this;
        }

        public Builder attributionMargin(final int attributionMargin) {
            this.attributionMargin = attributionMargin;
            return this;
        }

        public Builder informationPosition(final Position informationPosition) {
            this.informationPosition = informationPosition;
            return this;
        }

        public Builder informationMargin(final int informationMargin) {
            this.informationMargin = informationMargin;
            return this;
        }

        public Builder commentPosition(final Position commentPosition) {
            this.commentPosition = commentPosition;
            return this;
        }

        public Builder commentMargin(final int commentMargin) {
            this.commentMargin = commentMargin;
            return this;
        }

        public Builder photoDirectory(final File photoDirectory) {
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

        public Builder previewLength(final Long previewLength) {
            this.previewLength = previewLength;
            return this;
        }
    }

}
