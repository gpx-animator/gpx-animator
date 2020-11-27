package app.gpx_animator;

import org.junit.jupiter.api.Test;

import java.awt.Font;

import static java.awt.Font.BOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FontXmlAdapterTest {

    @Test
    public void testMarshal() {
        final FontXmlAdapter testee = new FontXmlAdapter();
        final Font font = new Font("Arial", BOLD, 18);
        assertEquals("Arial-BOLD-18", testee.marshal(font));
    }

    @Test
    public void testUnmarshal() {
        final FontXmlAdapter testee = new FontXmlAdapter();
        final Font font = testee.unmarshal("Arial-BOLD-18");
        assertEquals("Arial", font.getName());
        assertTrue(font.isBold());
        assertFalse(font.isItalic());
        assertFalse(font.isPlain());
        assertEquals(18, font.getSize());
    }

}
