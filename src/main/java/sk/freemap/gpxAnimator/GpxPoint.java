package sk.freemap.gpxAnimator;

import java.awt.geom.Point2D;

class GpxPoint extends Point2D.Double {

    private static final long serialVersionUID = -1060001559630478467L;

    private final LatLon latLon;
    private final double time;
    private final double speed;

    GpxPoint(final double x, final double y, final LatLon latLon, final double time, final double speed) {
        super(x, y);
        this.latLon = latLon;
        this.time = time;
        this.speed = speed;
    }

    LatLon getLatLon() {
        return latLon;
    }

    double getTime() {
        return time;
    }

    double getSpeed() {
        return speed;
    }
}
