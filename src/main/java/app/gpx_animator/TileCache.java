/*
 *  Copyright 2019 Martin Ždila, Freemap Slovakia
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

/*
 *  Caching characteristics:
 *  1. User specifies location of map tile image file cache.
 *  2. If no cache specified, then no caching occurs.
 *  3. Cache design assumes relatively few files (within OS limits of
 *     number of files per directory).
 *  4. Cache file names are based on a hash of the URL. This should remove
 *     any naming issues because of special characters in the URL that might
 *     be incompatible with the OS file naming restrictions. It also allows
 *     for the cache to hold files for more than one TMS or for more than
 *     one zoom level, etc.
 *  5. Cache files age out based on a user specified age limit. By default
 *     this is quite large (12 hours) because with the possible exception of
 *     tiles from the default renderer at openstreetmap.org map tile generation
 *     is not done very often (might be a month or two between changes).
 */

package app.gpx_animator;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Date;

public final class TileCache {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(TileCache.class);

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
        final String tileCacheDir = Preferences.getTileCacheDir();
        final long tileCacheTimeLimit = Preferences.getTileCacheTimeLimit();
        if (cachingEnabled(tileCacheDir)) {
            // Remove any cached tiles that are too old
            final File cacheDir = new File(tileCacheDir);
            final File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File cacheEntry : files) {
                    final String cacheFilename = cacheEntry.getName();
                    if ((cacheFilename.length() == 74) && (cacheFilename.endsWith(CACHED_FILE_EXTENSION))) {
                        ageCacheFile(cacheEntry, tileCacheTimeLimit);
                    } else {
                        LOGGER.error("Error: Unknown file in tile cache: {}", cacheFilename);
                    }
                }
            }
        }
    }

    public static BufferedImage getTile(final String url, final String tileCacheDir, final Long tileCacheTimeLimit) throws UserException {

        BufferedImage image;

        if (cachingEnabled(tileCacheDir)) {
            try {
                image = cachedGetTile(url, tileCacheDir, tileCacheTimeLimit);
            } catch (final UserException e) {
                image = unCachedGetTile(url);
            }
        } else {
            image = unCachedGetTile(url);
        }
        return image;
    }

    private static BufferedImage unCachedGetTile(final String url) throws UserException {
        BufferedImage mapTile;

        final String userAgent = String.format("%s %s on %s %s (%s)", //NON-NLS
                Constants.APPNAME, Constants.VERSION, Constants.OS_NAME, Constants.OS_VERSION, Constants.OS_ARCH);
        System.setProperty("http.agent", userAgent);
        try {
            mapTile = ImageIO.read(new URL(url));
        } catch (final IOException e) {
            throw new UserException("error getting tile ".concat(url), e);
        }
        if (mapTile == null) {
            throw new UserException("could not get tile ".concat(url));
        }

        return mapTile;
    }

    private static BufferedImage cachedGetTile(final String url, final String tileCacheDir, final Long tileCacheTimeLimit) throws UserException {
        BufferedImage mapTile = null;
        final String filename = hashName(url).concat(CACHED_FILE_EXTENSION);
        final String path = tileCacheDir.concat(File.separator).concat(filename);
        final File cacheFile = new File(path);

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
                if (cacheFile.exists() && !cacheFile.delete()) {
                    //noinspection DuplicateStringLiteralInspection
                    LOGGER.error("Can't delete tile cache file: {}", cacheFile);
                }
            }
        }

        //
        // If we have been successful in reading our cached map tile then mapTile
        // will be non-null. If it is null, then we need to download the image
        // tile from the server and then write it into our cache.
        //
        if (mapTile == null) {          // Map tile doesn't exist or we could not read it
            mapTile = unCachedGetTile(url);
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
        boolean result = ((tileCachePath != null) && (tileCachePath.trim().length() > 0));

        if (result) {
            // Create the cache directory if it doesn't exist
            final File cacheDir = new File(tileCachePath);
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
        final Date fileDate = new Date(cacheFile.lastModified());
        long msBetweenDates = new Date().getTime() - fileDate.getTime();
        if ((msBetweenDates) > tileCacheTimeLimit) {
            if (cacheFile.exists() && !cacheFile.delete()) {
                //noinspection DuplicateStringLiteralInspection
                LOGGER.error("Can't delete tile cache file: {}", cacheFile);
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

    private static String bytesToHex(final byte[] hash) {
        final StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            final String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) { // NOPMD -- Ignore magic number literal
                //noinspection MagicCharacter
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
