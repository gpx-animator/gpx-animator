package app.gpx_animator;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UtilsTest {

    @Test
    @SuppressWarnings({"HardCodedStringLiteral", "DuplicateStringLiteralInspection"})
    void isEqual() {
        assertTrue(Utils.isEqual(null, null));
        assertTrue(Utils.isEqual("", ""));
        assertTrue(Utils.isEqual("null", "null"));

        assertTrue(Utils.isEqual("Horst", "Horst"));
        assertTrue(Utils.isEqual("Dörte", "Dörte"));
        assertTrue(Utils.isEqual("Александр", "Александр"));
        assertTrue(Utils.isEqual("凤", "凤"));

        assertFalse(Utils.isEqual(null, ""));
        assertFalse(Utils.isEqual(null, " "));
        assertFalse(Utils.isEqual(null, "null"));

        assertFalse(Utils.isEqual("Horst", "horst"));
        assertFalse(Utils.isEqual("Dörte", "Doerte"));
        assertFalse(Utils.isEqual("Александр", "Alexander"));
        assertFalse(Utils.isEqual("凤", "Feng"));
    }
}
