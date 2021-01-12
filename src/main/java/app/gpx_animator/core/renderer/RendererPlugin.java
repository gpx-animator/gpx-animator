package app.gpx_animator.core.renderer;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public interface RendererPlugin {

    @NonNls
    Logger LOGGER = LoggerFactory.getLogger(RendererPlugin.class);

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition") // checking parameter count in constructor examination is quite okay
    static List<RendererPlugin> getAvailablePlugins(@NonNull final Configuration configuration, @NonNull final Metadata metadata) {
        final var plugins = new ArrayList<RendererPlugin>();

        final var reflections = new Reflections("app.gpx_animator.core.renderer.plugins");
        final var classes = reflections.getSubTypesOf(RendererPlugin.class);
        for (final var aClass : classes) {
            @SuppressWarnings("unchecked") final var constructors = ReflectionUtils.getAllConstructors(aClass);
            final var iterator = constructors.iterator();
            Object object = null;
            while (object == null && iterator.hasNext()) {
                final var constructor = iterator.next();
                try {
                    final var parameterTypes = constructor.getParameterTypes();
                    if (constructor.getParameterCount() == 0) {
                        object = constructor.newInstance();
                    } else if (parameterTypes.length == 1) {
                        if (parameterTypes[0] == Configuration.class) {
                            object = constructor.newInstance(configuration);
                        } else if (parameterTypes[0] == Metadata.class) {
                            object = constructor.newInstance(metadata);
                        }
                    } else if (parameterTypes.length == 2) {
                        if (parameterTypes[0] == Configuration.class && parameterTypes[1] == Metadata.class) {
                            object = constructor.newInstance(configuration, metadata);
                        } else if (parameterTypes[0] == Metadata.class && parameterTypes[1] == Configuration.class) {
                            object = constructor.newInstance(metadata, configuration);
                        }
                    }
                    if (object != null) {
                        final var instance = aClass.cast(object);
                        plugins.add(instance);
                    }
                } catch (final Exception e) {
                    LOGGER.error("Unable to initialize renderer plugin '{}'", aClass.getName(), e);
                }
            }
            if (object == null) {
                LOGGER.error("No suitable constructor found for renderer plugin '{}'", aClass.getName());
            }
        }

        plugins.sort(Comparator.comparingInt(RendererPlugin::getOrder));
        plugins.forEach(plugin -> LOGGER.info("Plugin '{}' with order {}", plugin.getClass(), plugin.getOrder()));

        LOGGER.info("Loaded {} plugins", plugins.size());

        return plugins;
    }

    /**
     * This method returns an integer value which is used to call the plugins
     * in a specified order to enable "layering" of the output.
     *
     * Plugins with a lower value are executed first, followed by plugins with
     * a higher value.
     *
     * @return the order number
     */
    int getOrder();

    void renderBackground(@NonNull BufferedImage image, @NonNull RenderingContext context) throws UserException;

    void renderFrame(int frame, @NonNull BufferedImage image, @NonNull RenderingContext context) throws UserException;

}
