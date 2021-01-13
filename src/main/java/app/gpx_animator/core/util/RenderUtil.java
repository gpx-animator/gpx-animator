package app.gpx_animator.core.util;

import edu.umd.cs.findbugs.annotations.NonNull;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

public final class RenderUtil {

    private RenderUtil() throws InstantiationException {
        throw new InstantiationException("RenderUtil is a utility class and can't be instantiated!");
    }

    public static Graphics2D getGraphics(@NonNull final BufferedImage image) {
        final var graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return graphics;
    }

    public static long getTime(final int frame, final long minTime, final double fps, final double speedup) {
        return (long) Math.floor(minTime + frame / fps * 1000d * speedup);
    }

}
