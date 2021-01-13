package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.renderer.framewriter.FrameWriter;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * To extend the rendering process, just implement this interface. At the
 * initialization of the rendering process, all classes implementing this
 * interface will be found and a constrcutor with the
 * <code>Configuration</code> as parameter will be called to create an
 * instance object.
 *
 * Therefore, all implementations <em>must</em> provide a constructor with
 * the <code>Configuration</code> as the one and only parameter:
 *
 * <pre>
 * {@code
 * public MyPlugin(@NonNull final Configuration configuration) {
 * }
 * </pre>
 */
public interface RendererPlugin {

    /**
     * This method returns an integer value which is used to call the plugins
     * in a specified order to enable "layering" of the output.
     *
     * Plugins with a lower value are executed first, followed by plugins with
     * a higher value. The default value is <code>0</code>
     *
     * @return the order number
     */
    default int getOrder() {
        return 0;
    }

    /**
     * If your rendering plugin is adding more frames to the video, you have
     * to return the number of added frames for the whole video before the
     * rendering starts. Do the calculation here and return the number of
     * frames.
     *
     * Default is to not add frames to the video.
     *
     * @return number of additional frames that will be added to the video
     */
    default int getAdditionalFrameCount() {
        return 0;
    }

    /**
     * Overwrite this method, if you want access to the metadata of the video.
     *
     * @param metadata the metadata of the video
     */
    default void setMetadata(@NonNull Metadata metadata) { }

    /**
     * Overwrite this method, if you want access to the frame writer for the
     * video.
     *
     * @param frameWriter the frame writer for the video
     */
    default void setFrameWriter(@NonNull FrameWriter frameWriter) { }

    /**
     * Overwrite this method, if you want access to the rendering context.
     *
     * @param renderingContext the rendering context
     */
    default void setRenderingContext(@NonNull RenderingContext renderingContext) { }

    /**
     * This render method is called to render the background image of the
     * video. If the video does not use the moving map feature, the background
     * has the same size as the video. If the video makes use of the moving
     * map, the background has the size of the map behind the video, which
     * usually is bigger than the viewport.
     *
     * @param image   the background image to be modified
     * @throws UserException error to be shown to the user
     */
    default void renderBackground(@NonNull BufferedImage image) throws UserException { }

    /**
     * This render method is called to render one frame of the video. The size
     * of this frame is equal to the resolution of the resulting video.
     *
     * @param frame   the frame number
     * @param image   the image of the frame to be modified
     * @param marker  the track point marking the actual position // TODO get the marker based on the frame number
     * @throws UserException error to be shown to the user
     */
    default void renderFrame(int frame, @Nullable Point2D marker, @NonNull BufferedImage image) throws UserException { }

}
