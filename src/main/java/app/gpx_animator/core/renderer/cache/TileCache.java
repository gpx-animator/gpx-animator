/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.core.renderer.cache;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.UserException;
import app.gpx_animator.core.preferences.Preferences;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;

public final class TileCache {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(TileCache.class);

    private static final String DELETE_ERROR = "Can't delete tile cache file: {}";

    private TileCache() throws InstantiationException {
        throw new InstantiationException("TileCache is a utility class which can't be instantiated!");
    }

    private static final String CACHED_FILE_TYPE = "png"; //NON-NLS
    private static final String CACHED_FILE_EXTENSION = ".gpxac.".concat(CACHED_FILE_TYPE); //NON-NLS
    private static MessageDigest messageDigest = null;

    //
    // Remove all old cached map tiles
    //
    // It is possible that the user has pointed our cache to a directory
    // that holds other files. We will make a sanity check on files before
    // we delete them.
    //
    // The sanity checks are very basic:
    //  1. Is the length of the name what we expect?
    //  2. Is the file extension (string suffix) correct?
    //
    // If either check fails, log a warning rather than delete the file.
    //
    public static void ageCache() {
        ageCache(Preferences.getTileCacheTimeLimit());
    }

    private static void ageCache(final long tileCacheTimeLimit) {
        final var tileCacheDir = Preferences.getTileCacheDir();
        if (cachingEnabled(tileCacheDir)) {
            // Remove any cached tiles that are too old
            final var cacheDir = new File(tileCacheDir);
            final var files = cacheDir.listFiles();
            if (files != null) {
                for (var cacheEntry : files) {
                    final var cacheFilename = cacheEntry.getName();
                    if ((cacheFilename.length() == 74) && (cacheFilename.endsWith(CACHED_FILE_EXTENSION))) {
                        ageCacheFile(cacheEntry, tileCacheTimeLimit);
                    } else {
                        LOGGER.error("Error: Unknown file in tile cache: {}", cacheFilename);
                    }
                }
            }
        }
    }

    public static void clear() {
        ageCache(-1);
    }

    public static long getSize() {
        final var tileCacheDir = Preferences.getTileCacheDir();
        final var cacheDir = new File(tileCacheDir);
        final var files = cacheDir.listFiles();
        var size = 0L;
        if (files != null) {
            for (var cacheEntry : files) {
                final var cacheFilename = cacheEntry.getName();
                if ((cacheFilename.length() == 74) && (cacheFilename.endsWith(CACHED_FILE_EXTENSION))) {
                    size += cacheEntry.length();
                }
            }
        }
        return size;
    }

    public static BufferedImage getTile(final String url, final String userAgent, final String tileCacheDir, final Long tileCacheTimeLimit)
        throws UserException {

        BufferedImage image;

        if (cachingEnabled(tileCacheDir)) {
            try {
                image = cachedGetTile(url, userAgent, tileCacheDir, tileCacheTimeLimit);
            } catch (final UserException e) {
                image = unCachedGetTile(url, userAgent);
            }
        } else {
            image = unCachedGetTile(url, userAgent);
        }
        return image;
    }

    private static BufferedImage unCachedGetTile(final String url, final String userAgent) throws UserException {
        BufferedImage mapTile;

        if (!userAgent.isBlank()) {
            System.setProperty("http.agent", userAgent);
        } else {
            System.setProperty("http.agent", Constants.USER_AGENT);
        }
        try {
            mapTile = ImageIO.read(URI.create(url).toURL());
        } catch (final IOException e) {
            throw new UserException(String.format("error getting tile %s: %s", url, e.getCause()), e);
        }
        if (mapTile == null) {
            throw new UserException("could not get tile ".concat(url));
        }

        return mapTile;
    }

    private static BufferedImage cachedGetTile(final String url, final String userAgent, final String tileCacheDir,
        final Long tileCacheTimeLimit) throws UserException {
        BufferedImage mapTile = null;
        final var filename = hashName(url).concat(CACHED_FILE_EXTENSION);
        final var path = tileCacheDir.concat(File.separator).concat(filename);
        final var cacheFile = new File(path);

        // Age out old tile file in cache directory.
        ageCacheFile(cacheFile, tileCacheTimeLimit);

        // If map tile is in cache, then return it.
        if (cacheFile.isFile()) {
            try {
                mapTile = ImageIO.read(cacheFile);
            } catch (final IOException e) {
                // Treat as non-fatal, we will notify the user then attempt to
                // remove the file we could not read.

                LOGGER.error("Error: Failed to read cached tile {} ({})", url, path, e);
                try {
                    Files.deleteIfExists(cacheFile.toPath());
                } catch (final IOException ex) {
                    LOGGER.error(DELETE_ERROR, cacheFile, ex);
                }
            }
        }

        //
        // If we have been successful in reading our cached map tile then mapTile
        // will be non-null. If it is null, then we need to download the image
        // tile from the server and then write it into our cache.
        //
        if (mapTile == null) {          // Map tile doesn't exist or we could not read it
            mapTile = unCachedGetTile(url, userAgent);
            try {
                ImageIO.write(mapTile, CACHED_FILE_TYPE, cacheFile);
            } catch (final IOException e) {
                // Treat as non-fatal. This should revert the behavior to the same
                // as running without a cache.
                LOGGER.error("Error writing cached tile {} ({})", url, path, e);
            }
        }

        return mapTile;
    }

    //
    // Check for tile cache enabled.
    //
    // We consider caching enabled if:
    //  1. The path is given (not null and not empty)
    //  2. The cache path points to a directory
    //
    // If the cache path does not exist, then we will create it.
    //
    private static boolean cachingEnabled(final String tileCachePath) {
        var result = (tileCachePath != null && !tileCachePath.isBlank());

        if (result) {
            // Create the cache directory if it doesn't exist
            final var cacheDir = new File(tileCachePath);
            if (cacheDir.exists()) {
                result = cacheDir.isDirectory();
            } else {
                if (!cacheDir.mkdirs()) {
                    LOGGER.error("Can't create tile cache directory '{}'. Fallback to not caching the tiles!", cacheDir);
                    result = false;
                }
            }
        }
        return result;
    }

    //
    // Check age on a file and remove it if it is too old.
    //
    private static void ageCacheFile(final File cacheFile, final Long tileCacheTimeLimit) {
        final Instant lastModified = Instant.ofEpochMilli(cacheFile.lastModified());
        final Instant now = Instant.now();

        if (Duration.between(lastModified, now).toMillis() > tileCacheTimeLimit) {
            try {
                Files.deleteIfExists(cacheFile.toPath());
            } catch (final IOException e) {
                LOGGER.error(DELETE_ERROR, cacheFile, e);
            }
        }
    }

    private static String hashName(final String url) throws UserException {
        try {
            if (messageDigest == null) {
                messageDigest = MessageDigest.getInstance("SHA-256");
            }
            return bytesToHex(messageDigest.digest(url.getBytes(StandardCharsets.UTF_8)));
        } catch (final NoSuchAlgorithmException e) {
            throw new UserException("error creating hash name ".concat(url), e);
        }
    }

    private static String bytesToHex(final byte... hash) {
        final var hexString = new StringBuilder();
        for (var b : hash) {
            final var hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) { // NOPMD -- Ignore magic number literal
                //noinspection MagicCharacter
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
