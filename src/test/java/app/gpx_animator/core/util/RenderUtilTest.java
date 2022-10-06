package app.gpx_animator.core.util;

import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static com.github.romankh3.image.comparison.model.ImageComparisonState.MATCH;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RenderUtilTest {

    @Disabled("images to be generated")
    @Test
    void rotateImage() throws IOException {
        // GIVEN
        BufferedImage input = ImageIO.read(new File(getClass().getResource("orientation_3.jpg").getFile()));
        BufferedImage expected = ImageIO.read(new File(getClass().getResource("expected.png").getFile()));
        // WHEN
        BufferedImage rotated = RenderUtil.rotateImage(input, 3);
        // THEN
        ImageComparisonResult result = new ImageComparison(expected, rotated).compareImages();
        assertEquals(MATCH, result.getImageComparisonState());
    }

}
