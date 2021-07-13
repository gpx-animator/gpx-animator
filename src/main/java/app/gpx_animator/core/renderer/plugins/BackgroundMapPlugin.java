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
package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.renderer.cache.TileCache;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.util.ResourceBundle;
import java.util.regex.Pattern;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class BackgroundMapPlugin implements RendererPlugin {

    @SuppressWarnings({"RegExpAnonymousGroup", "RegExpRedundantEscape"})
    // This regex is tested and I don't want to rewrite it which may potentionally break it.
    private static final Pattern SWITCH_PATTERN = Pattern.compile("\\{switch:([^}]*)\\}");

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient String tmsUrlTemplate;
    private final transient float backgroundMapVisibility;

    private transient Integer zoom;
    private transient double minX;
    private transient double maxX;
    private transient double minY;
    private transient double maxY;

    private transient RenderingContext context;

    public BackgroundMapPlugin(@NonNull final Configuration configuration) {
        tmsUrlTemplate = configuration.getTmsUrlTemplate();
        backgroundMapVisibility = configuration.getBackgroundMapVisibility();
    }

    @Override
    public void setMetadata(@NotNull final Metadata metadata) {
        zoom = metadata.zoom();
        minX = metadata.minX();
        maxX = metadata.maxX();
        minY = metadata.minY();
        maxY = metadata.maxY();
    }

    @Override
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "RenderingContext is used for callbacks")
    public void setRenderingContext(@NotNull final RenderingContext renderingContext) {
        this.context = renderingContext;
    }

    @Override
    public int getOrder() {
        return -1_000;
    }

    @Override
    public void renderBackground(@NonNull final BufferedImage image) throws UserException {
        if (tmsUrlTemplate == null || tmsUrlTemplate.isBlank() || backgroundMapVisibility <= 0.0 || zoom == null) {
            // no map defined or map should not be visible
            return;
        }

        final var ga = (Graphics2D) image.getGraphics();

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
                if (context.isCancelled1()) {
                    return;
                }

                i++;

                var url = tmsUrlTemplate
                        .replace("{zoom}", Integer.toString(zoom)) //NON-NLS
                        .replace("{x}", Integer.toString(x)) //NON-NLS
                        .replace("{y}", Integer.toString(y)); //NON-NLS

                if (options != null) {
                    final var sb = new StringBuilder();
                    final var matcher = SWITCH_PATTERN.matcher(url);
                    if (matcher.find()) {
                        matcher.appendReplacement(sb, options[i % options.length]);
                    }
                    matcher.appendTail(sb);
                    url = sb.toString();
                }

                context.setProgress1((int) (100.0 * i / total), String.format(resourceBundle.getString("map.loadingtiles.progress"), i, total));

                final var tile = TileCache.getTile(url, tileCacheDir, tileCacheTimeLimit);

                // convert to RGB format
                final var tile1 = new BufferedImage(tile.getWidth(), tile.getHeight(), BufferedImage.TYPE_INT_RGB);
                tile1.getGraphics().drawImage(tile, 0, 0, null);

                ga.drawImage(tile1,
                        new RescaleOp(backgroundMapVisibility, (1f - backgroundMapVisibility) * 255f, null),
                        256 * (x - tileX) + offsetX,
                        image.getHeight() - (256 * (tileY - y) + offsetY));
            }
        }

        context.setProgress1(100, String.format(resourceBundle.getString("map.loadingtiles.progress"), i, total));
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

}
