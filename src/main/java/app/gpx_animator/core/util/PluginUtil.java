package app.gpx_animator.core.util;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.renderer.plugins.RendererPlugin;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;
import org.reflections.ReflectionUtils;
import org.reflections.Reflections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class PluginUtil {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(PluginUtil.class);

    private PluginUtil() throws InstantiationException {
        throw new InstantiationException("PluginUtil is a utility class and can't be instantiated!");
    }

    @SuppressWarnings("PMD.AvoidLiteralsInIfCondition") // checking parameter count in constructor examination is quite okay
    public static List<RendererPlugin> getAvailablePlugins(@NonNull final Configuration configuration, @NonNull final Metadata metadata,
                                                           @NonNull final RenderingContext renderingContext) {
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
                    // TODO I would like to have a more flexible approach with real dependency injection
                    final var parameterTypes = constructor.getParameterTypes();
                    if (constructor.getParameterCount() == 0) {
                        object = constructor.newInstance();
                    } else if (parameterTypes.length == 1 && parameterTypes[0] == Configuration.class) {
                        object = constructor.newInstance(configuration);
                    } else if (parameterTypes.length == 2 && parameterTypes[0] == Configuration.class && parameterTypes[1] == Metadata.class) {
                        object = constructor.newInstance(configuration, metadata);
                    } else if (parameterTypes.length == 3 && parameterTypes[0] == Configuration.class && parameterTypes[1] == Metadata.class
                            && parameterTypes[2] == RenderingContext.class) {
                        object = constructor.newInstance(configuration, metadata, renderingContext);
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

}
