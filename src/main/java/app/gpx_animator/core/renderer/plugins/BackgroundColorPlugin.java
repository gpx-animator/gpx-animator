package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class BackgroundColorPlugin implements RendererPlugin {

    private final transient Color backgroundColor;

    public BackgroundColorPlugin(@NonNull final Configuration configuration) {
        this.backgroundColor = configuration.getBackgroundColor();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void renderBackground(@NonNull final BufferedImage image) {
        final var graphics = getGraphics(image);
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

}
