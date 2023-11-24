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
package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.data.gpx.GpxPoint;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.TextRenderer;
import app.gpx_animator.core.util.RenderUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

// Plugins are loaded using reflection
public final class InformationPlugin extends TextRenderer implements RendererPlugin {

    private final DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private final String information;
    private final Position position;
    private final int margin;
    private final double fps;
    private final SpeedUnit speedUnit;
    private final boolean showDateTime;

    private long minTime;
    private double speedup;
    private int frames;
    private final long gpsTimeout;

    private final Map<Integer, Double> speedValues = new HashMap<>();
    private GpxPoint lastSpeedPoint = null;

    public InformationPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());
        this.information = configuration.getInformation();
        this.position = configuration.getInformationPosition();
        this.margin = configuration.getInformationMargin();
        this.fps = configuration.getFps();
        this.speedUnit = configuration.getSpeedUnit();
        this.gpsTimeout = configuration.getGpsTimeout();
        this.showDateTime = configuration.getTrackConfigurationList().stream()
                .noneMatch(tc -> tc.getForcedPointInterval() != null);
    }

    @Override
    public void setMetadata(@NonNull final Metadata metadata) {
        this.minTime = metadata.minTime();
        this.speedup = metadata.speedup();
        this.frames = metadata.frames();
    }

    @Override
    public int getOrder() {
        return 1_000;
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage image) {
        if (marker == null || Position.HIDDEN.equals(position)) {
            // information should not be visible
            return;
        }

        final var time = RenderUtil.getTime(frame, minTime, fps, speedup);
        final var dateTimeString = showDateTime ? dateFormat.format(time) : "";
        final var latLongString = getLatLonString(marker);
        final var speedString = getSpeedString(marker, time, frame);

        final var gpsTime = getTime(marker);    //TODO --Get strings from resource
        var gpsDateTimeString = "Unknown";
        var gpsDiffTime = 0L;
        var gpsDiffTimeString = "";
        var gpsStatus = false;
        var gpsLostTimeString = "";
        var gpsStatusString = "NOTIME";           //TODO --Get strings from resource
        if (gpsTime > 0) {
            gpsDiffTime = time - gpsTime;
            gpsDateTimeString = dateFormat.format(gpsTime);
            gpsDiffTimeString = DurationFormatUtils.formatDuration(gpsDiffTime, "+H:mm:ss");
            gpsStatus = gpsTimeout <= 0 || gpsDiffTime < gpsTimeout;
            gpsLostTimeString = gpsStatus ? "" : gpsDiffTimeString;
            gpsStatusString = gpsStatus ? "OK" : "LOST";      //TODO --Get strings from resource
        }

        final var text = information
                .replace("%SPEED%", speedString)                    // Speed
                .replace("%LATLON%", latLongString)                 // (last) GPS postion
                .replace("%DATETIME%", dateTimeString)              // Frame (real) time
                .replace("%GPSDATETIME%", gpsDateTimeString)        // (last) GPS position time
                .replace("%GPSDIFFTIME%", gpsDiffTimeString)        // Difference between frame time and last GPS time
                .replace("%GPSLOSTTIME%", gpsLostTimeString)        // Difference between frame time and last GPS time if GSP LOST
                .replace("%GPSSTATUS%", gpsStatusString);           // GPS status only [OK/LOST]
        renderText(text, position, margin, image);
    }

    private String getLatLonString(@NonNull final Point2D point) {
        if (point instanceof GpxPoint gpxPoint) {
            final var trackPoint = gpxPoint.getTrackPoint();
            return String.format("%.4f, %.4f", trackPoint.getLatitude(), trackPoint.getLongitude()); //NON-NLS
        } else {
            return "";
        }
    }

    public String getSpeedString(final Point2D point, final long time, final int frame) {
        if (point instanceof GpxPoint gpxPoint) {
            final var speed = calculateSpeedForDisplay(gpxPoint, time, frame);
            if (speedUnit.isDisplayMinutes()) {
                final var format = "%d:%02d %s";
                return format.formatted((int) speed, (int) ((speed - (int) speed) * 60), speedUnit.getAbbreviation()); // Display minutes and seconds
            } else {
                final var format = speed > 10 ? "%.0f %s" : "%.1f %s"; // with 1 decimal below 10, no decimals 10 and above
                return format.formatted(speed, speedUnit.getAbbreviation());
            }
        } else {
            return "";
        }
    }

    private long getTime(@NonNull final Point2D point) {
        if (point instanceof GpxPoint gpxPoint) {
            return gpxPoint.getTime();
        } else {
            return 0;
        }
    }

    private double calculateSpeedForDisplay(final GpxPoint point, final long time, final int frame) {
        if (frame == frames) {
            return 0.0; // for the last frame always zero
        }

        final var speed = point.getSpeed() != null
                ? point.getSpeed() * 3.6 // mps to kmh
                : calculateSpeed(lastSpeedPoint, point, time);
        lastSpeedPoint = point;
        speedValues.put(frame, speed);

        final var deleteBefore = frame - (Math.round(fps)); // 1 second
        speedValues.keySet().removeIf(f -> f < deleteBefore);

        return speedUnit.convertSpeed(Math.round(speedValues.values().stream().mapToDouble(Double::doubleValue).average().orElse(0)));
    }


    private double calculateSpeed(@Nullable GpxPoint lastPoint, final GpxPoint point, final long time) {
        final var timeout = time - 1_000 * 60; // 1 minute // TODO use speedup and fps
        if(lastPoint == null) {
            return 0;
        }
        final var distance = calculateDistance(lastPoint, point);
        final double timeDiff = point.getTime() - lastPoint.getTime();

        final double speed;
        if (distance > 0 && point.getTime() > timeout) {
            speed = timeDiff > 0 ? Math.round((3_600 * distance) / timeDiff) : 0;
        } else {
            speed = 0;
        }

        return speed;
    }


    private long calculateDistance(final GpxPoint point1, final GpxPoint point2) {
        final var lat1 = point1.getTrackPoint().getLatitude();
        final var lon1 = point1.getTrackPoint().getLongitude();
        final var lat2 = point2.getTrackPoint().getLatitude();
        final var lon2 = point2.getTrackPoint().getLongitude();

        if ((lat1.equals(lat2)) && (lon1.equals(lon2))) {
            return 0;
        } else {
            final var theta = lon1 - lon2;
            final var dist = Math.sin(Math.toRadians(lat1)) * Math.sin(Math.toRadians(lat2))
                    + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) * Math.cos(Math.toRadians(theta));
            final var arcCosine = Math.acos(dist);
            final var degrees = Math.toDegrees(arcCosine);
            final var mi = degrees * 60 * 1.1515; // to miles
            final var km = mi * 1.609344; // to kilometers
            final var m = km * 1_000; // to meters
            return Math.round(m); // round to full meters
        }
    }

}
