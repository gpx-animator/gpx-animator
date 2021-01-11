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
        plugins.forEach(plugin -> LOGGER.info("Plugin '{}' with order {}", plugin.getClass(), plugin.getOrder()));

        LOGGER.info("Loaded {} plugins", plugins.size());

        return plugins;
    }

    @SuppressWarnings("unused") // forcing the subsclasses to use this constructor
    public RendererPlugin(@NonNull final Configuration configuration, @NonNull final Metadata metadata) {
        super();
    }

    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    public void renderBackground(@NonNull final BufferedImage image, @NonNull final RenderingContext context) throws UserException {}

    public void renderFrame(final int frame, @NonNull final BufferedImage image, @NonNull final RenderingContext context) throws UserException {}

}
