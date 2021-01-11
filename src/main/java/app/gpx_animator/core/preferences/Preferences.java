package app.gpx_animator.core.preferences;

import app.gpx_animator.ColorXmlAdapter;

import java.awt.Color;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Preferences {
    private static final ResourceBundle RESOURCE_BUNDLE = Preferences.getResourceBundle();

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    private static final java.util.prefs.Preferences PREFS =
            java.util.prefs.Preferences.userRoot().node("app/gpx-animator"); //NON-NLS

    private static final String CHANGELOG_VERSION = "changelog_version"; //NON-NLS
    private static final String LAST_WORKING_DIR = "last_working_dir"; //NON-NLS
    private static final String RECENT_FILES = "recent_files"; //NON-NLS
    private static final String TILE_CACHE_DIR = "tile_cache_dir"; //NON-NLS
    private static final String TILE_CACHE_TIME_LIMIT = "tile_cache_time_limit"; //NON-NLS
    private static final String TRACK_COLOR_RANDOM = "track_color_random"; //NON-NLS
    private static final String TRACK_COLOR_DEFAULT = "track_color_default"; //NON-NLS

    public static ResourceBundle getResourceBundle() {
        return ResourceBundle.getBundle("i18n/Messages"); //NON-NLS
    }

    public static String getConfigurationDir() {
        return System.getProperty("user.home").concat(FILE_SEPARATOR).concat(".gpx-animator"); //NON-NLS
    }

    public static String getChangelogVersion() {
        return PREFS.get(CHANGELOG_VERSION, "");
    }

    public static void setChangelogVersion(final String changelogVersion) {
        PREFS.put(CHANGELOG_VERSION, changelogVersion);
    }

    public static String getLastWorkingDir() {
        var lastWorkingDir = PREFS.get(LAST_WORKING_DIR, null);
        if (lastWorkingDir == null) {
            lastWorkingDir = System.getProperty("user.home");
            final var videosDir = lastWorkingDir.concat(FILE_SEPARATOR).concat(RESOURCE_BUNDLE.getString("preferences.videodirectory"));
            if (new File(videosDir).exists()) {
                lastWorkingDir = videosDir;
            }
        }
       return lastWorkingDir;
    }

    public static void setLastWorkingDir(final String lastWorkingDir) {
        PREFS.put(LAST_WORKING_DIR, lastWorkingDir);
    }

    public static List<File> getRecentFiles() {
        return Arrays.stream(PREFS.get(RECENT_FILES, "").split(","))
                .map(File::new)
                .filter(File::exists)
                .collect(Collectors.toList());
    }

    public static void addRecentFile(final File file) {
        final var result = Stream.concat(Stream.of(file), getRecentFiles().stream())
                .distinct()
                .limit(5)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(","));
        PREFS.put(RECENT_FILES, result);
    }

    public static String getTileCacheDir() {
        return PREFS.get(TILE_CACHE_DIR,
                getConfigurationDir()
                        .concat(FILE_SEPARATOR).concat("caches") //NON-NLS
                        .concat(FILE_SEPARATOR).concat("tiles")); //NON-NLS
    }

    public static void setTileCacheDir(final String tileCacheDir) {
        PREFS.put(TILE_CACHE_DIR, tileCacheDir);
    }

    public static long getTileCacheTimeLimit() {
        return PREFS.getLong(TILE_CACHE_TIME_LIMIT,
                24 * 60 * 60 * 1_000); // 24 hours
    }

    public static void setTileCacheTimeLimit(final long tileCacheTimeLimit) {
        PREFS.putLong(TILE_CACHE_TIME_LIMIT, tileCacheTimeLimit);
    }

    public static boolean getTrackColorRandom() {
        return PREFS.getBoolean(TRACK_COLOR_RANDOM, true);
    }

    public static void setTrackColorRandom(final boolean trackColorRandom) {
        PREFS.putBoolean(TRACK_COLOR_RANDOM, trackColorRandom);
    }

    public static Color getTrackColorDefault() {
        final var colorCode = PREFS.get(TRACK_COLOR_DEFAULT, "#FF0000"); //NON-NLS
        final var xmlAdapter = new ColorXmlAdapter();
        return xmlAdapter.unmarshal(colorCode);
    }

    public static void setTrackColorDefault(final Color trackColorDefault) {
        final var xmlAdapter = new ColorXmlAdapter();
        final var colorCode = xmlAdapter.marshal(trackColorDefault);
        PREFS.put(TRACK_COLOR_DEFAULT, colorCode);
    }

    private Preferences() {
        throw new UnsupportedOperationException("This class provides static methods only!");
    }
}
