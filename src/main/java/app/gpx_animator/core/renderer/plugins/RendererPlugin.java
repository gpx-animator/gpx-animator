package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

public interface RendererPlugin {

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
    void renderBackground(@NonNull BufferedImage image) throws UserException;

    /**
     * This render method is called to render one frame of the video. The size
     * of this frame is equal to the resolution of the resulting video.
     *
     * @param frame   the frame number
     * @param image   the image of the frame to be modified
     * @param marker  the track point marking the actual position // TODO get the marker based on the frame number
     * @throws UserException error to be shown to the user
     */
    void renderFrame(int frame, @Nullable Point2D marker, @NonNull BufferedImage image) throws UserException;

}
