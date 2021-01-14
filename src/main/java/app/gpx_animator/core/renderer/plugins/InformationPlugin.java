package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.data.gpx.GpxPoint;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.TextRenderer;
import app.gpx_animator.core.util.RenderUtil;
import app.gpx_animator.core.util.SpeedUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.text.DateFormat;

public final class InformationPlugin extends TextRenderer implements RendererPlugin {

    private final transient DateFormat dateFormat = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM);

    private final transient Position position;
    private final transient int margin;
    private final transient double fps;
    private final transient SpeedUnit speedUnit;

    private transient long minTime;
    private transient double speedup;

    public InformationPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());
        this.position = configuration.getInformationPosition();
        this.margin = configuration.getInformationMargin();
        this.fps = configuration.getFps();
        this.speedUnit = configuration.getSpeedUnit();
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
        final var dateString = dateFormat.format(time);
        final var latLongString = getLatLonString(marker);
        final var speedString = SpeedUtil.getSpeedString(marker, time, frame, fps, speedUnit);
        final var text = "%s\n%s\n%s".formatted(speedString, latLongString, dateString);
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

}
