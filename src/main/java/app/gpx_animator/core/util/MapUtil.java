/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.util;

import app.gpx_animator.core.data.MapTemplate;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class MapUtil {

    private MapUtil() throws InstantiationException {
        throw new InstantiationException("MapUtil is a utility class and can't be instantiated!");
    }

    public static List<MapTemplate> readMaps() {
        final var factory = SAXParserFactory.newInstance();
        final SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        final List<MapTemplate> labeledItems = new ArrayList<>();

        try {
            try (var is = MapUtil.class.getResourceAsStream("/maps.xml")) { //NON-NLS
                saxParser.parse(is, new DefaultHandler() {
                    private final StringBuilder sb = new StringBuilder();
                    private String id;
                    private String name;
                    private String url;
                    private String attributionText;

                    @Override
                    @SuppressWarnings({
                            "checkstyle:MissingSwitchDefault", // Every other case can be ignored!
                            "checkstyle:InnerAssignment"       // Checkstyle 8.37 can't handle the enhanced switch properly
                    })
                    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "Every other case can be ignored!") //NON-NLS NON-NLS
                    public void endElement(final String uri, final String localName, @NonNls final String qName) {
                        switch (qName) {
                            case "id" -> id = sb.toString().trim();
                            case "name" -> name = sb.toString().trim();
                            case "url" -> url = sb.toString().trim();
                            case "attribution-text" -> attributionText = sb.toString().trim();
                            case "entry" -> labeledItems.add(new MapTemplate(id, name, url, attributionText));
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

        labeledItems.sort(Comparator.comparing(MapTemplate::toString));

        return labeledItems;
    }

    public static MapTemplate getMapTemplate(final String tmsUrlTemplate) {
        if (tmsUrlTemplate == null || tmsUrlTemplate.isBlank()) {
            return null;
        }

        return readMaps().stream()
                .filter(m -> tmsUrlTemplate.equals(m.getUrl()))
                .findFirst()
                .orElse(null);
    }
}
