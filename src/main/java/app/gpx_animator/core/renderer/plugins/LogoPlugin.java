package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.renderer.ImageRenderer;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RendererPlugin;
import app.gpx_animator.core.renderer.RenderingContext;
import org.jetbrains.annotations.NotNull;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class LogoPlugin extends RendererPlugin {

    private final transient BufferedImage logo;
    private final transient Position position;
    private final transient int margin;
    private final transient ImageRenderer imageRenderer;

    public LogoPlugin(final @NotNull Configuration configuration, final @NotNull Metadata metadata) throws UserException {
        super(configuration, metadata);
        position = configuration.getLogoPosition();
        margin = configuration.getLogoMargin();

        final var file = configuration.getLogo();
        if (file != null && file.exists()) {
            try {
                logo = ImageIO.read(file);
            } catch (final IOException e) {
                throw new UserException("Can't read logo: ".concat(e.getMessage())); // TODO translate
            }
            imageRenderer = ImageRenderer.getInstance();
        } else {
            logo = null;
            imageRenderer = null;
        }
    }

    @Override
    public void renderFrame(final int frame, final @NotNull BufferedImage image, final @NotNull RenderingContext context) {
        if (logo == null || position.equals(Position.HIDDEN)) {
            // no logo defined or logo should not be visible
            return;
        }

        imageRenderer.renderImage(logo, position, margin, image);
    }
}
