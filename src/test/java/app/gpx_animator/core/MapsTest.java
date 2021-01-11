package app.gpx_animator.core;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.imageio.ImageIO;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("nightly")
public class MapsTest {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(MapsTest.class);

    @BeforeAll
    public static void setup() {
        final var userAgent = String.format("%s %s on %s %s (%s)", //NON-NLS
                Constants.APPNAME, Constants.VERSION, Constants.OS_NAME, Constants.OS_VERSION, Constants.OS_ARCH);
        System.setProperty("http.agent", userAgent);
    }

    @Test
    public void validateMaps() {
        final var mapValidations = readValidations();
        final var mapTemplates = MapUtil.readMaps();

        final List<MapTemplate> testSkipped = new ArrayList<>();
        final List<MapTemplate> testFailed = new ArrayList<>();
        final List<MapTemplate> testPassed = new ArrayList<>();

        mapTemplates.forEach(mapTemplate -> {
            final var id = mapTemplate.getId();
            final var mapValidation = mapValidations.get(id);

            if (mapValidation == null || mapValidation.getValidationUrls().isEmpty()) {
                testSkipped.add(mapTemplate);
                LOGGER.warn("No test for map '{} (ID: {})'", mapTemplate.toString(), mapTemplate.getId());
            } else {
                if (validateMap(mapTemplate, mapValidation)) {
                    testPassed.add(mapTemplate);
                } else {
                    testFailed.add(mapTemplate);
                }
            }
        });

        LOGGER.info("{} maps passed, {} maps failed, {} maps skipped", testPassed.size(), testFailed.size(), testSkipped.size());
        assertTrue(testFailed.isEmpty());
    }

    private boolean validateMap(final MapTemplate mapTemplate, final MapValidation mapValidation) {
        var okay = true;

        for (final var url : mapValidation.getValidationUrls()) {
            try {
                final var mapTile = ImageIO.read(new URL(url));
                if (mapTile == null) {
                    okay = false;
                    LOGGER.error("Failed test for map '{} (ID: {})' with URL '{}': {}",
                            mapTemplate.toString(), mapTemplate.getId(), url, "no image");
                }
            } catch (final Exception e) {
                okay = false;
                LOGGER.error("Failed test for map '{} (ID: {})' with URL '{}': {}",
                        mapTemplate.toString(), mapTemplate.getId(), url, e.getMessage());
            }
        }

        return okay;
    }

    public static Map<String, MapValidation> readValidations() {
        final var factory = SAXParserFactory.newInstance();
        final SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        final Map<String, MapValidation> mapValidations = new HashMap<>();

        try {
            try (var is = MapUtil.class.getResourceAsStream("/maps-tests.xml")) { //NON-NLS
                saxParser.parse(is, new DefaultHandler() {
                    private final StringBuilder sb = new StringBuilder();
                    private String id;
                    private  List<String> validationUrls = new ArrayList<>();

                    @Override
                    @SuppressWarnings({
                            "checkstyle:MissingSwitchDefault", // Every other case can be ignored!
                            "checkstyle:InnerAssignment"       // Checkstyle 8.37 can't handle the enhanced switch properly
                    })
                    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "Every other case can be ignored!") //NON-NLS NON-NLS
                    public void endElement(final String uri, final String localName, @NonNls final String qName) {
                        switch (qName) {
                            case "id" -> id = sb.toString().trim();
                            case "url" -> validationUrls.add(sb.toString().trim());
                            case "map" -> {
                                mapValidations.put(id, new MapValidation(validationUrls));
                                validationUrls = new ArrayList<>();
                            }
                        }
                        sb.setLength(0);
                    }

                    @Override
                    public void characters(final char[] ch, final int start, final int length) {
                        sb.append(ch, start, length);
                    }
                });
            } catch (final SAXException e) {
                throw new RuntimeException(e);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        return mapValidations;
    }

    private static final class MapValidation {
        private final List<String> validationUrls;

        private MapValidation(final List<String> validationUrls) {
            this.validationUrls = validationUrls;
        }

        public List<String> getValidationUrls() {
            return validationUrls;
        }
    }

}
