package app.gpx_animator.core.renderer;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Arrays;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

public final class TextRenderer extends RendererPlugin {

    private static final int IMAGE_TYPE = BufferedImage.TYPE_4BYTE_ABGR;
    private static final int ANTI_ALIAS_COMPENSATION = 10;
    private static final float STRIKE_WIDTH = 3f;
    private static final Stroke STROKE = new BasicStroke(STRIKE_WIDTH, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);

    private final transient Font font;
    private final transient FontMetrics fontMetrics;

    public TextRenderer(@NotNull final Configuration configuration, @NonNull final Metadata metadata, @NonNull final Font font) {
        super(configuration, metadata);
        this.font = font;
        this.fontMetrics = getFontMetrics();
    }

    private FontMetrics getFontMetrics() {
        final var image = new BufferedImage(100, 100, IMAGE_TYPE);
        final var graphics = (Graphics2D) image.getGraphics();
        graphics.setStroke(STROKE);
        return graphics.getFontMetrics(font);
    }

    private int calculateTextWidth(@NonNull final String text) {
        return Arrays.stream(text.split("\n"))
                .mapToInt(fontMetrics::stringWidth)
                .max()
                .orElseThrow();
    }

    private int calculateTextHeight(@NonNull final String text) {
        final var lines = text.split("\n").length;
        return lines * fontMetrics.getHeight();
    }

    public BufferedImage renderText(@NonNull final String text, @NonNull final TextAlignment alignment) {
        final var trimmedText = text.trim();
        final var width = calculateTextWidth(trimmedText) + ANTI_ALIAS_COMPENSATION;
        final var height = calculateTextHeight(trimmedText);

        final var image = new BufferedImage(width, height, IMAGE_TYPE);
        final var graphics = getGraphics(image);
        graphics.setStroke(STROKE);
        graphics.setFont(font);

        final var fontRenderContext = graphics.getFontRenderContext();
        final var lineHeight = fontMetrics.getHeight();

        var lineNum = 0;
        for (final var line : trimmedText.split("\n")) {
            lineNum++;
            var trimmedLine = line.trim();
            var xPosition = calculateHorizontalPosition(alignment, trimmedLine, image.getWidth());
            var yPosition = calculateVerticalPosition(lineNum, lineHeight);

            final var textLayout = new TextLayout(trimmedLine, font, fontRenderContext);
            final var shape = textLayout.getOutline(AffineTransform.getTranslateInstance(xPosition, yPosition));

            graphics.setColor(Color.white);
            graphics.fill(shape);
            graphics.draw(shape);

            graphics.setColor(Color.black);
            graphics.drawString(trimmedLine, xPosition, yPosition);
        }

        return image;
    }

    private int calculateHorizontalPosition(@NonNull final TextAlignment alignment, @NonNull final String line, final int width) {
        return switch (alignment) {
            case LEFT -> 0;
            case CENTER -> (width - ANTI_ALIAS_COMPENSATION - fontMetrics.stringWidth(line)) / 2;
            case RIGHT -> width - ANTI_ALIAS_COMPENSATION - fontMetrics.stringWidth(line);
        };
    }

    private int calculateVerticalPosition(final int lineNum, final int lineHeight) {
        return (-fontMetrics.getHeight() + fontMetrics.getAscent()) + (lineNum * lineHeight);
    }

    public enum TextAlignment {

        LEFT, CENTER, RIGHT;

        public static TextAlignment forPosition(@NonNull final Position position) {
            return switch (position) {
                case HIDDEN, TOP_LEFT, BOTTOM_LEFT -> LEFT;
                case TOP_CENTER, BOTTOM_CENTER -> CENTER;
                case TOP_RIGHT, BOTTOM_RIGHT -> RIGHT;
            };
        }
    }
}
