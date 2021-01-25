package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.data.gpx.GpxPoint;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.TextRenderer;
import app.gpx_animator.core.util.RenderUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.Map;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class InformationPlugin extends TextRenderer implements RendererPlugin {

    private final transient DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private final transient Position position;
    private final transient int margin;
    private final transient double fps;
    private final transient SpeedUnit speedUnit;
    private final transient boolean showDateTime;

    private transient long minTime;
    private transient double speedup;

    private final transient Map<Integer, Long> speedValues = new HashMap<>();
    private transient GpxPoint lastSpeedPoint = null;

    public InformationPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());
        this.position = configuration.getInformationPosition();
        this.margin = configuration.getInformationMargin();
        this.fps = configuration.getFps();
        this.speedUnit = configuration.getSpeedUnit();
        this.showDateTime = configuration.getTrackConfigurationList().stream()
                .noneMatch(tc -> tc.getForcedPointInterval() != null);
    }

    @Override
    public void setMetadata(@NonNull final Metadata metadata) {
        this.minTime = metadata.minTime();
        this.speedup = metadata.speedup();
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
        final var dateString = showDateTime ? dateFormat.format(time) : "";
        final var latLongString = getLatLonString(marker);
        final var speedString = getSpeedString(marker, time, frame);
        final var text = "%s\n%s\n%s".formatted(speedString, latLongString, dateString).trim();
        renderText(text, position, margin, image);
    }

    private String getLatLonString(@NonNull final Point2D point) {
        if (point instanceof GpxPoint gpxPoint) {
            final var latLon = gpxPoint.getLatLon();
            return String.format("%.4f, %.4f", latLon.getLat(), latLon.getLon()); //NON-NLS
        } else {
            return "";
        }
    }

    public String getSpeedString(final Point2D point, final long time, final int frame) {
        if (point instanceof GpxPoint gpxPoint) {
            final var speed = calculateSpeedForDisplay(gpxPoint, time, frame);
            final var format = speed > 10 ? "%.0f %s" : "%.1f %s"; // with 1 decimal below 10, no decimals 10 and above
            return format.formatted(speed, speedUnit.getAbbreviation());
        } else {
            return "";
        }
    }


    private double calculateSpeedForDisplay(final GpxPoint point, final long time, final int frame) {
        final var speed = calculateSpeed(point, time);
        speedValues.put(frame, speed);

        final var deleteBefore = frame - (Math.round(fps)); // 1 second
        speedValues.keySet().removeIf((f) -> f < deleteBefore);

        return speedUnit.convertSpeed(Math.round(speedValues.values().stream().mapToLong(Long::longValue).average().orElse(0)));
    }


    private long calculateSpeed(final GpxPoint point, final long time) {
        final var timeout = time - 1_000 * 60; // 1 minute // TODO use speedup and fps
        final var distance = calculateDistance(lastSpeedPoint, point);
        final double timeDiff = lastSpeedPoint == null ? 0 : point.getTime() - lastSpeedPoint.getTime();

        final long speed;
        if (distance > 0 && point.getTime() > timeout) {
            speed = Math.round((3_600 * distance) / timeDiff);
        } else {
            speed = 0;
        }

        lastSpeedPoint = point;
        return speed;
    }


    private long calculateDistance(final GpxPoint point1, final GpxPoint point2) {
        if (point1 == null) {
            return 0;
        }

        final var lat1 = point1.getLatLon().getLat();
        final var lon1 = point1.getLatLon().getLon();
        final var lat2 = point2.getLatLon().getLat();
        final var lon2 = point2.getLatLon().getLon();

        if ((lat1 == lat2) && (lon1 == lon2)) {
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
