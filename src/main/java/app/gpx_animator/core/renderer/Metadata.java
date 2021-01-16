package app.gpx_animator.core.renderer;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

@SuppressFBWarnings("EQ_UNUSUAL") // TODO SpotBugs 4.2.0 does not know how to handle the equals method of Java records #305
public record Metadata(int zoom, double minX, double maxX, double minY, double maxY, long minTime, long maxTime, double speedup) { }
