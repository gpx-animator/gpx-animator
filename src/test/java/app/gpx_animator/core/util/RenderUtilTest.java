package app.gpx_animator.core.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.imageio.ImageIO;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.stream.Stream;

import static com.github.romankh3.image.comparison.model.ImageComparisonState.MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderUtilTest {

    @Test
    void testRotateImageWith180Degrees() throws IOException {
        // given an original image and the image rotated by 180 degrees
        BufferedImage input = ImageIO.read(new File(getClass().getResource("/photo/bikeride_rotation_180.jpg").getFile()));
        BufferedImage expected = ImageIO.read(new File(getClass().getResource("/photo/bikeride.jpg").getFile()));

        // when rotating the rotated image back by 180 degrees
        BufferedImage rotated = RenderUtil.rotateImage(input, 3);

        // then the rotated image and the original image are the same
        ImageComparisonResult result = new ImageComparison(expected, rotated).compareImages();
        assertEquals(MATCH, result.getImageComparisonState());
    }

    @ParameterizedTest(name = "orientation {0} should rotate by {1} degrees and flip {2}")
    @MethodSource
    void testRotateImage(final int orientation, final RotateByDegrees rotateByDegrees, final FlipDirection flipDirection) throws IOException {
        // given an input file which is rotated and flipped according to parameters
        final var input = ImageIO.read(new File(getClass().getResource("/photo/bikeride.jpg").getFile()));
        BufferedImage expected = input;
        if (rotateByDegrees != null) {
            expected = rotateImage(input, rotateByDegrees);
        }
        if (flipDirection != null) {
            expected = flipImage(expected, flipDirection);
        }

        // when rotate image is called
        final var rotated = RenderUtil.rotateImage(input, orientation);

        // then the rotated image is equal to the expected image
        final var result = new ImageComparison(expected, rotated).compareImages();
        assertEquals(MATCH, result.getImageComparisonState());
    }

    private static Stream<Arguments> testRotateImage() {
        return Stream.of(
                Arguments.of(2, null, FlipDirection.VERTICAL),
                Arguments.of(3, RotateByDegrees.DEGREES_180, null),
                Arguments.of(4, RotateByDegrees.DEGREES_180, FlipDirection.VERTICAL),
                Arguments.of(5, RotateByDegrees.DEGREES_90, FlipDirection.VERTICAL),
                Arguments.of(6, RotateByDegrees.DEGREES_90, null),
                Arguments.of(7, RotateByDegrees.DEGREES_MINUS_90, FlipDirection.VERTICAL),
                Arguments.of(8, RotateByDegrees.DEGREES_MINUS_90, null),
                Arguments.of(999, null, null)
        );
    }

    private BufferedImage rotateImage(final BufferedImage originalImage, final RotateByDegrees rotateByDegrees) {
        final var height = originalImage.getHeight();
        final var width = originalImage.getWidth();

        BufferedImage rotatedImage = null;
        if (rotateByDegrees == RotateByDegrees.DEGREES_90) {
            rotatedImage = new BufferedImage(height, width, originalImage.getType());
            final var g2d = rotatedImage.createGraphics();
            final double radians = Math.toRadians(rotateByDegrees.getDegrees());
            g2d.translate((height - width) / 2, (height - width) / 2);
            g2d.rotate(radians, height / 2.0, width / 2.0);
            g2d.drawRenderedImage(originalImage, null);
        } else if (rotateByDegrees == RotateByDegrees.DEGREES_180) {
            rotatedImage = new BufferedImage(width, height, originalImage.getType());
            final var g2d = rotatedImage.createGraphics();
            final double radians = Math.toRadians(rotateByDegrees.getDegrees());
            g2d.rotate(radians, width / 2.0, height / 2.0);
            g2d.drawRenderedImage(originalImage, null);
        } else if (rotateByDegrees == RotateByDegrees.DEGREES_MINUS_90) {
            rotatedImage = new BufferedImage(height, width, originalImage.getType());
            final var g2d = rotatedImage.createGraphics();
            final double radians = Math.toRadians(rotateByDegrees.getDegrees());
            g2d.translate((width - height) / 2, (width - height) / 2);
            g2d.rotate(radians, height / 2.0, width / 2.0);
            g2d.drawRenderedImage(originalImage, null);
        }
        return rotatedImage;
    }

    private BufferedImage flipImage(final BufferedImage originalImage, final FlipDirection flipDirection) {
        final var flippedImage = new BufferedImage(originalImage.getWidth(), originalImage.getHeight(),
                originalImage.getType());
        final var g2d = flippedImage.createGraphics();
        AffineTransform transform = null;
        if (flipDirection == FlipDirection.HORIZONTAL) {
            transform = AffineTransform.getScaleInstance(-1, 1);
            transform.translate(0, -originalImage.getWidth());
        } else if (flipDirection == FlipDirection.VERTICAL) {
            transform = AffineTransform.getScaleInstance(1, -1);
            transform.translate(0, -originalImage.getHeight());
        }
        g2d.setTransform(transform);
        g2d.drawImage(originalImage, 0, 0, null);
        g2d.dispose();
        return flippedImage;
    }

    private enum FlipDirection {
        HORIZONTAL,
        VERTICAL
    }

    private enum RotateByDegrees {
        DEGREES_90(90),
        DEGREES_180(180),
        DEGREES_MINUS_90(-90);

        private final double degrees;

        RotateByDegrees(final double degrees) {
            this.degrees = degrees;
        }

        public double getDegrees() {
            return degrees;
        }
    }
}
