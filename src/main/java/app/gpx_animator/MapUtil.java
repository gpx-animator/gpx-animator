/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
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
package app.gpx_animator;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.renderer.cache.TileCache;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

public final class MapUtil {

    private MapUtil() throws InstantiationException {
        throw new InstantiationException("MapUtils a utility class and can't be instantiated!");
    }

    @SuppressWarnings({"RegExpAnonymousGroup", "RegExpRedundantEscape"})
    // This regex is tested and I don't want to rewrite it which may potentionally break it.
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\{switch:([^}]*)\\}");


    @SuppressWarnings("checkstyle:ParameterNumber") // TODO This is too much and just a temporary solution not to break the build...
    public static void drawMap(final BufferedImage bi, final String tmsUrlTemplate, final float backgroundMapVisibility, final Integer zoom,
                               final double minX, final double maxX, final double minY, final double maxY, final RenderingContext rc)
            throws UserException {

        if (tmsUrlTemplate == null || backgroundMapVisibility <= 0.0) {
            // no map defined or map should not be visible
            return;
        }

        final var resourceBundle = Preferences.getResourceBundle();

        final var ga = (Graphics2D) bi.getGraphics();

        final var tileDblX = xToTileX(zoom, minX);
        final var tileX = (int) Math.floor(tileDblX);
        final var offsetX = (int) Math.floor(256.0 * (tileX - tileDblX));

        final var tileDblY = yToTileY(zoom, minY);
        final var tileY = (int) Math.floor(tileDblY);
        final var offsetY = (int) Math.floor(256.0 * (tileDblY - tileY));

        final var maxXtile = (int) Math.floor(xToTileX(zoom, maxX));
        final var maxYtile = (int) Math.floor(yToTileY(zoom, maxY));

        final var total = (maxXtile - tileX + 1) * (tileY - maxYtile + 1);
        var i = 0;

        final var m = SWITCH_PATTERN.matcher(tmsUrlTemplate); // note that only one switch in pattern is supported
        final var options = m.find() ? m.group(1).split(",") : null;

        final var tileCacheDir = Preferences.getTileCacheDir();
        final var tileCacheTimeLimit = Preferences.getTileCacheTimeLimit();

        for (var x = tileX; x <= maxXtile; x++) {
            for (var y = tileY; y >= maxYtile; y--) {
                if (rc.isCancelled1()) {
                    return;
                }

                i++;

                var url = tmsUrlTemplate
                        .replace("{zoom}", Integer.toString(zoom)) //NON-NLS
                        .replace("{x}", Integer.toString(x)) //NON-NLS
                        .replace("{y}", Integer.toString(y)); //NON-NLS

                if (options != null) {
                    final var sb = new StringBuffer();
                    final var matcher = SWITCH_PATTERN.matcher(url);
                    if (matcher.find()) {
                        matcher.appendReplacement(sb, options[i % options.length]);
                    }
                    matcher.appendTail(sb);
                    url = sb.toString();
                }

                rc.setProgress1((int) (100.0 * i / total), String.format(resourceBundle.getString("map.loadingtiles.progress"), i, total));

                final var tile = TileCache.getTile(url, tileCacheDir, tileCacheTimeLimit);

                // convert to RGB format
                final var tile1 = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
                tile1.getGraphics().drawImage(tile, 0, 0, null);

                ga.drawImage(tile1,
                        new RescaleOp(backgroundMapVisibility, (1f - backgroundMapVisibility) * 255f, null),
                        256 * (x - tileX) + offsetX,
                        bi.getHeight() - (256 * (tileY - y) + offsetY));
            }
        }
    }


    private static double yToTileY(final int zoom, final double minY) {
        return latToTileY(zoom, yToLat(minY));
    }


    private static double xToTileX(final int zoom, final double minX) {
        return lonToTileX(zoom, xToLon(minX));
    }


    private static double lonToTileX(final int zoom, final double lon) {
        return (lon + 180.0) / 360.0 * (1 << zoom);
    }


    private static double latToTileY(final int zoom, final double lat) {
        return (1 - Math.log(Math.tan(Math.toRadians(lat)) + 1 / Math.cos(Math.toRadians(lat))) / Math.PI) / 2 * (1 << zoom);
    }


    private static double xToLon(final double x) {
        return Math.toDegrees(x);
    }


    private static double yToLat(final double y) {
        return Math.toDegrees(2.0 * (Math.atan(Math.exp(y)) - Math.PI / 4.0));
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

}
