/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
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
 * <p>To extend the rendering process, just implement this interface. At the
 * initialization of the rendering process, all classes implementing this
 * interface will be found and a constrcutor with the
 * <code>Configuration</code> as parameter will be called to create an
 * instance object.</p>
 *
 * <p>Therefore, all implementations <em>must</em> provide a constructor with
 * the <code>Configuration</code> as the one and only parameter:</p>
 *
 * <pre>
 * {@code
 * public MyPlugin(@NonNull final Configuration configuration) {
 * }
 * </pre>
 */
public interface RendererPlugin {

    /**
     * <p>This method returns an integer value which is used to call the plugins
     * in a specified order to enable "layering" of the output.</p>
     *
     * <p>Plugins with a lower value are executed first, followed by plugins with
     * a higher value. The default value is <code>0</code></p>
     *
     * @return the order number
     */
    default int getOrder() {
        return 0;
    }

    /**
     * <p>If your rendering plugin is adding more frames to the video, you have
     * to return the number of added frames for the whole video before the
     * rendering starts. Do the calculation here and return the number of
     * frames.</p>
     *
     * <p>Default is to not add frames to the video.</p>
     *
     * @return number of additional frames that will be added to the video
     */
    default int getAdditionalFrameCount() {
        return 0;
    }

    /**
     * <p>Overwrite this method, if you want access to the metadata of the video.</p>
     *
     * @param metadata the metadata of the video
     */
    default void setMetadata(final @NonNull Metadata metadata) { }

    /**
     * <p>Overwrite this method, if you want access to the frame writer for the
     * video.</p>
     *
     * @param frameWriter the frame writer for the video
     */
    default void setFrameWriter(final @NonNull FrameWriter frameWriter) { }

    /**
     * <p>Overwrite this method, if you want access to the rendering context.</p>
     *
     * @param renderingContext the rendering context
     */
    default void setRenderingContext(final @NonNull RenderingContext renderingContext) { }

    /**
     * <p>This render method is called to render the background image of the
     * video. If the video does not use the moving map feature, the background
     * has the same size as the video. If the video makes use of the moving
     * map, the background has the size of the map behind the video, which
     * usually is bigger than the viewport.</p>
     *
     * @param image   the background image to be modified
     * @throws UserException error to be shown to the user
     */
    default void renderBackground(final @NonNull BufferedImage image) throws UserException { }

    /**
     * <p>This render method is called to render one frame of the video. The size
     * of this frame is equal to the resolution of the resulting video.</p>
     *
     * @param frame   the frame number
     * @param image   the image of the frame to be modified
     * @param marker  the track point marking the actual position // TODO get the marker based on the frame number
     * @throws UserException error to be shown to the user
     */
    @SuppressWarnings("RedundantThrows") // implementations throw this exception
    default void renderFrame(final int frame, final @Nullable Point2D marker, final @NonNull BufferedImage image) throws UserException { }

    /**
     * <p>This method is called when the rendering has finished successfully.</p>
     *
     * @throws UserException error to be shown to the user
     */
    @SuppressWarnings("RedundantThrows") // implementations throw this exception
    default void renderingFinished() throws UserException { }

    /**
     * <p>This method is called when the rendering was canceled.</p>
     *
     * @throws UserException error to be shown to the user
     */
    @SuppressWarnings("RedundantThrows") // implementations throw this exception
    default void renderingCanceled() throws UserException { }
}
