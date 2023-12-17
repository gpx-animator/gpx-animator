package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.data.Photo;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PhotoPluginTest {

    @Test
    void toPhotoWithUpsideDownImage() {
        // given an image rotated by 180 degrees with exif orientation 3
        File image = new File(getClass().getResource("/photo/bikeride_rotation_180.jpg").getFile());

        // when parsing the metadata of the image
        Photo photo = PhotoPlugin.toPhoto(image);

        // then the parsed metadata have orientation 3
        assertEquals(3, photo.orientation());
    }
}
