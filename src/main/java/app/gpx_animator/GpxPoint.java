package app.gpx_animator;

import java.awt.geom.Point2D;
import java.io.Serial;

public final class GpxPoint extends Point2D.Double {

    @Serial
    private static final long serialVersionUID = -1060001559230478467L;

    private final LatLon latLon;
    private final long time;

    public GpxPoint(final double x, final double y, final LatLon latLon, final long time) {
        super(x, y);
        this.latLon = latLon;
        this.time = time;
    }

    public LatLon getLatLon() {
        return latLon;
    }

    public long getTime() {
        return time;
    }
}
