package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.renderer.RendererPlugin;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.image.BufferedImage;

public class BackgroundColorPlugin extends RendererPlugin {

    private final Color backgroundColor;

    public BackgroundColorPlugin(@NotNull Configuration configuration) {
        super(configuration);
        this.backgroundColor = configuration.getBackgroundColor();
    }

    @Override
    public void renderBackground(@NotNull BufferedImage image) {
        final var graphics = getGraphics(image);
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

}
