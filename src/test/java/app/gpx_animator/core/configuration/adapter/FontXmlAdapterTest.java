package app.gpx_animator.core.configuration.adapter;

import org.junit.jupiter.api.Test;

import java.awt.Font;

import static java.awt.Font.BOLD;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FontXmlAdapterTest {

    @Test
    void testMarshal() {
        final var testee = new FontXmlAdapter();
        final var font = new Font("Arial", BOLD, 18);
        assertEquals("Arial-BOLD-18", testee.marshal(font));
    }

    @Test
    void testUnmarshal() {
        final var testee = new FontXmlAdapter();
        final var font = testee.unmarshal("Arial-BOLD-18");
        assertEquals("Arial", font.getName());
        assertTrue(font.isBold());
        assertFalse(font.isItalic());
        assertFalse(font.isPlain());
        assertEquals(18, font.getSize());
    }

}
