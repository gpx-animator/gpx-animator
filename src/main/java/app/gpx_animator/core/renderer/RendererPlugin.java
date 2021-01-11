package app.gpx_animator.core.renderer;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public abstract class RendererPlugin {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(RendererPlugin.class);

    public static List<RendererPlugin> getAvailablePlugins(@NotNull Configuration configuration, @NonNull final Metadata metadata) {
        final var plugins = new ArrayList<RendererPlugin>();

        final var reflections = new Reflections("app.gpx_animator.core.renderer.plugins");
        final var classes = reflections.getSubTypesOf(RendererPlugin.class);
        for (final var aClass : classes) {
            @SuppressWarnings("unchecked") final var constructors =
                    ReflectionUtils.getConstructors(aClass, ReflectionUtils.withParameters(Configuration.class, Metadata.class));
            final var iterator = constructors.iterator();
            if (iterator.hasNext()) {
                final var constructor = iterator.next();
                try {
                    final var object = constructor.newInstance(configuration, metadata);
                    final var instance = aClass.cast(object);
                    plugins.add(instance);
                } catch (final Exception e) {
                    LOGGER.error("Unable to initialize renderer plugin '{}'", aClass.getName(), e);
                }
            } else {
                LOGGER.error("Renderer plugin '{}' is missing the required constructor", aClass.getName());
            }
        }

        plugins.sort(Comparator.comparingInt(RendererPlugin::getOrder));

        LOGGER.info("Loaded {} plugins", plugins.size());

        return plugins;
    }

    public RendererPlugin(@NonNull final Configuration configuration, @NonNull final Metadata metadata) {
        super();
    }

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public void renderBackground(@NonNull final BufferedImage image, @NonNull final RenderingContext rc) throws UserException {}

    public void renderFrame(final int frame, @NonNull final BufferedImage image, @NonNull final Configuration configuration) throws UserException {}

    protected Graphics2D getGraphics(@NonNull final BufferedImage image) {
        final var graphics = (Graphics2D) image.getGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_ON);
        graphics.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_QUALITY);
        graphics.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        return graphics;
    }

}
