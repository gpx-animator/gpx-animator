package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RenderingContext;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class BackgroundMapPluginTest {

    @ParameterizedTest
    @CsvSource({
            "0, 0, 0, {-y}, 0",
            "1, 0, 0, {-y}, 1",
            "1, 0, 1, {-y}, 0",
            "3, 2, 1, {-y}, 6",
            "3, 2, 6, {-y}, 1",
            "18, 12345, 65536, {-y}, 196607",
            "0, 0, 0, {y}, 0",
            "3, 2, 1, {y}, 1",
            "3, 2, 6, {y}, 6"
    })
    void renderBackgroundResolvesTileRows(final int zoom, final int x, final int y, final String rowPlaceholder,
                                         final int expectedRow, @TempDir final Path directory) throws Exception {
        final var tile = new BufferedImage(256, 256, BufferedImage.TYPE_INT_RGB);
        final var graphics = tile.createGraphics();
        graphics.setColor(Color.MAGENTA);
        graphics.fillRect(0, 0, tile.getWidth(), tile.getHeight());
        graphics.dispose();

        final var tileDirectory = Files.createDirectories(directory.resolve("%d/%d".formatted(zoom, x)));
        final var tileFile = tileDirectory.resolve("%d-%d.png".formatted(y, expectedRow));
        assertTrue(ImageIO.write(tile, "png", tileFile.toFile()));

        // Include {y} alongside {-y} so the two row conventions must remain independent.
        final var configuration = new Configuration.Builder()
                .tmsUrlTemplate(directory.toUri() + "{zoom}/{x}/{y}-" + rowPlaceholder + ".png")
                .backgroundMapVisibility(1.0f)
                .build();
        final var plugin = new BackgroundMapPlugin(configuration);
        plugin.setMetadata(tileMetadata(zoom, x, y));
        plugin.setRenderingContext(new RenderingContext() {
            @Override
            public void setProgress1(final int pct, final String message) {
                // No progress UI is needed for an offline tile.
            }

            @Override
            public boolean isCancelled1() {
                return false;
            }
        });

        final var previousCacheDir = Preferences.getTileCacheDir();
        try {
            Preferences.setTileCacheDir("");
            final var image = new BufferedImage(128, 128, BufferedImage.TYPE_INT_RGB);
            plugin.renderBackground(image);
            assertEquals(Color.MAGENTA.getRGB(), image.getRGB(64, 64));
        } finally {
            Preferences.setTileCacheDir(previousCacheDir);
        }
    }

    private static Metadata tileMetadata(final int zoom, final int x, final int y) {
        // Mercator bounds inset from a single tile's edges avoid rounding onto adjacent tiles.
        final var tileCount = Math.pow(2, zoom);
        final var minX = (x + 0.25) / tileCount * 2 * Math.PI - Math.PI;
        final var maxX = (x + 0.75) / tileCount * 2 * Math.PI - Math.PI;
        final var minY = Math.PI * (1 - 2 * (y + 0.75) / tileCount);
        final var maxY = Math.PI * (1 - 2 * (y + 0.25) / tileCount);
        return new Metadata(zoom, minX, maxX, minY, maxY, 0, 0, 1, 1);
    }
}
