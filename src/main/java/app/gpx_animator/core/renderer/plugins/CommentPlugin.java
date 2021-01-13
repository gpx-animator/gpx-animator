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

    private String lastComment;

    public CommentPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());
        position = configuration.getCommentPosition();
        margin = configuration.getCommentMargin();
    }

    @Override
    public int getOrder() {
        return 0;
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
