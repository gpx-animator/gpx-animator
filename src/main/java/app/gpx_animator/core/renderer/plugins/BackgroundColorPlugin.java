package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RendererPlugin;
import app.gpx_animator.core.renderer.RenderingContext;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public class BackgroundColorPlugin extends RendererPlugin {

    private final Color backgroundColor;

    public BackgroundColorPlugin(@NotNull final Configuration configuration, @NonNull final Metadata metadata) {
        super(configuration, metadata);
        this.backgroundColor = configuration.getBackgroundColor();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void renderBackground(@NotNull final BufferedImage image, @NotNull final RenderingContext context) {
        final var graphics = getGraphics(image);
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

}
