package sk.freemap.gpxAnimator;

import java.awt.geom.Point2D;

class GpxPoint extends Point2D.Double {

    private static final long serialVersionUID = -1060001559630478467L;
    
    private LatLon latLon;

    GpxPoint(final double x, final double y, final LatLon latLon) {
        super(x, y);
        this.latLon = latLon;
    }

    LatLon getLatLon() {
        return latLon;
    }
}
