package sk.freemap.gpxAnimator;

import java.awt.geom.Point2D;

class GpxPoint extends Point2D.Double {

    private static final long serialVersionUID = -1060001559230478467L;

    private final LatLon latLon;
    private final long time;

    GpxPoint(final double x, final double y, final LatLon latLon, final long time) {
        super(x, y);
        this.latLon = latLon;
        this.time = time;
    }

    LatLon getLatLon() {
        return latLon;
    }

    long getTime() {
        return time;
    }
}
