package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class BackgroundImagePlugin implements RendererPlugin {

    private final transient BufferedImage backgroundImage;

    public BackgroundImagePlugin(@NonNull final Configuration configuration) throws UserException {
        final var file = configuration.getBackgroundImage();
        if (file != null && file.exists()) {
            try {
                backgroundImage = ImageIO.read(file);
            } catch (final IOException e) {
                throw new UserException("Can't read background image: ".concat(e.getMessage()));
            }
        } else {
            backgroundImage = null;
        }
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public void renderBackground(@NonNull final BufferedImage image) {
        if (backgroundImage == null) {
            // no image defined
            return;
        }

        final var scaledImage = backgroundImage.getWidth() <= image.getWidth() && backgroundImage.getHeight() <= image.getHeight()
                ? backgroundImage
                : Scalr.resize(Scalr.resize(backgroundImage,
                Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, image.getWidth()),
                Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, image.getHeight());

        final var graphics = getGraphics(image);
        graphics.drawImage(scaledImage, 0, 0, scaledImage.getWidth(), scaledImage.getHeight(), null);
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage image) { }

}
