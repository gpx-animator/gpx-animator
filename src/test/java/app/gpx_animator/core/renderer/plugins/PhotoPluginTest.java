package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.data.Photo;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoPluginTest {

    @Disabled("input image to be generated")
    @Test
    void toPhotoWithUpsideDownImage() {
        // GIVEN
        File image = new File(getClass().getResource("upside_down.jpg").getFile());
        // WHEN
        Photo photo = PhotoPlugin.toPhoto(image);
        // THEN
        assertEquals(3, photo.orientation());
    }

}
