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

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.gpx.GpxPoint;
import app.gpx_animator.core.renderer.TextRenderer;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class CommentPlugin extends TextRenderer implements RendererPlugin {

    private final transient Position position;
    private final transient int margin;

    private transient String lastComment;

    public CommentPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());
        position = configuration.getCommentPosition();
        margin = configuration.getCommentMargin();
    }

    @Override
    public int getOrder() {
        return 1_000;
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage image) {
        if (Position.HIDDEN.equals(position)) {
            // comment should not be visible
            return;
        }

        final var comment = getCommentString(marker);
        if (comment != null && !comment.isBlank()) {
            renderText(comment, position, margin, image);
        }
    }

    /**
     * This method has a special behaviour:
     * - If the track point has a comment, it returns the comment.
     * - If the track point has no comment, it returns the last comment.
     * - If the track point has an empty comment, it resets the comment.
     */
    private String getCommentString(@Nullable final Point2D marker) {
        if (marker instanceof GpxPoint gpxPoint) {
            final var latLon = gpxPoint.getLatLon();
            final var comment = latLon.getCmt();

            // null = use the last comment
            if (comment != null) {

                // blank = reset last comment
                if (comment.isBlank()) {
                    lastComment = "";

                    // new comment
                } else if (!comment.isBlank()) {
                    lastComment = comment;
                }
            }
        }
        return lastComment;
    }
}
