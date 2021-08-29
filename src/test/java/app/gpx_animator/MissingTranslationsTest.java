package app.gpx_animator;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public final class MissingTranslationsTest {

    private static final Properties EN = new Properties();
    private static final Properties DE = new Properties();
    private static Set<String> allKeys;

    @BeforeAll
    public static void beforeAllTests() throws IOException {
        EN.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("i18n/Messages.properties"));
        DE.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("i18n/Messages_de.properties"));
        allKeys = Stream.of(EN, DE)
                .map(Properties::keySet)
                .flatMap(Set::stream)
                .map(Object::toString)
                .collect(Collectors.toCollection(HashSet::new));
    }

    private void checkBundle(final Properties bundle, final String code) {
        assertEquals(List.of(), allKeys.stream()
                .filter(key -> bundle.get(key) == null)
                .collect(Collectors.toList()),
        "Missing translations for language code '%s'".formatted(code));
    }

    @Test
    void english() {
        checkBundle(EN, "en");
    }

    @Test
    void german() {
        checkBundle(DE, "de");
    }

}
