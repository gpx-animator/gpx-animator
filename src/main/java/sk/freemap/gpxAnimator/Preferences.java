package sk.freemap.gpxAnimator;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class Preferences {

    private static final java.util.prefs.Preferences prefs =
            java.util.prefs.Preferences.userRoot().node("app/gpx-animator");

    private static final String LAST_WORKING_DIR = "last_working_dir";
    private static final String RECENT_FILES = "recent_files";
    private static final String TILE_CACHE_DIR = "tile_cache_dir";
    private static final String TILE_CACHE_TIME_LIMIT = "tile_cache_time_limit";

    public static String getLastWorkingDir() {
       String lastWorkingDir = prefs.get(LAST_WORKING_DIR, null);
        if (lastWorkingDir == null) {
            lastWorkingDir = System.getProperty("user.home");
            final String videosDir = lastWorkingDir + System.getProperty("file.separator") + "Videos";
            if (new File(videosDir).exists()) {
                lastWorkingDir = videosDir;
            }
        }
       return lastWorkingDir;
    }

    public static void setLastWorkingDir(final String lastWorkingDir) {
        prefs.put(LAST_WORKING_DIR, lastWorkingDir);
    }

    public static List<File> getRecentFiles() {
        return Arrays.stream(prefs.get(RECENT_FILES, "").split(","))
                .map(File::new)
                .filter(File::exists)
                .collect(Collectors.toList());
    }

    public static void addRecentFile(final File file) {
        final String result = Stream.concat(Stream.of(file), getRecentFiles().stream())
                .distinct()
                .limit(5)
                .map(File::getAbsolutePath)
                .collect(Collectors.joining(","));
        prefs.put(RECENT_FILES, result);
    }

    public static String getTileCacheDir() {
        return prefs.get(TILE_CACHE_DIR,
                System.getProperty("user.home")
                        + System.getProperty("file.separator") + ".gpx-animator"
                        + System.getProperty("file.separator") + "caches"
                        + System.getProperty("file.separator") + "tiles");
    }

    public static void setTileCacheDir(final String tileCacheDir) {
        prefs.put(TILE_CACHE_DIR, tileCacheDir);
    }

    public static long getTileCacheTimeLimit() {
        return prefs.getLong(TILE_CACHE_TIME_LIMIT,
                24 * 60 * 60 * 1_000); // 24 hours
    }

    public static void setTileCacheTimeLimit(final long tileCacheTimeLimit) {
        prefs.putLong(TILE_CACHE_TIME_LIMIT, tileCacheTimeLimit);
    }

    private Preferences() {
        throw new UnsupportedOperationException("This class provides static methods only!");
    }
}
