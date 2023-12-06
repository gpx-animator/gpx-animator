package app.gpx_animator.ui.swing;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.preferences.Preferences;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JOptionPane;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class SupportDataCreator {

    private static final Logger LOGGER = LoggerFactory.getLogger(SupportDataCreator.class);
    private static final String GB_MEASURE_UNIT = " GB";

    private SupportDataCreator() {
    }

    static void createSupportData() {
        List<Path> files = new ArrayList<>();

        files.add(collectRuntimeInformation());
        files.add(getLogbackFileName());
        files.addAll(getConfigurationFiles());
        files.addAll(getGpxFiles());
        files.forEach(System.out::println);

        var pathSavedZip = zipFiles(files);
        openSuccessDialog(pathSavedZip);
    }

    private static List<Path> getConfigurationFiles() {
        return Preferences.getRecentFiles().stream().map(File::toPath).toList();
    }

    private static List<Path> getGpxFiles() {
        // TODO how to get the recently used gpx files?
        return new ArrayList<>();
    }

    private static void openSuccessDialog(final String pathToZip) {
        var resourceBundle = Preferences.getResourceBundle();
        JOptionPane.showMessageDialog(MainFrame.getInstance(),
                String.format(resourceBundle.getString("ui.mainframe.dialog.message.supportdata.success"), pathToZip),
                resourceBundle.getString("ui.mainframe.dialog.title.success"),
                JOptionPane.INFORMATION_MESSAGE);
    }

    private static String zipFiles(final List<Path> files) {
        var homeDirectory = Paths.get(Preferences.getConfigurationDir() + System.getProperty("file.separator")
                + "support_data.zip");
        try (var fileOutputStream = new FileOutputStream(homeDirectory.toAbsolutePath().toString());
             var zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (Path file : files) {
                final var fileBytes = Files.readAllBytes(file);
                final var fileName = file.getFileName();
                if (fileName == null) {
                    continue;
                }
                zipOutputStream.putNextEntry(new ZipEntry(fileName.toString()));
                zipOutputStream.write(fileBytes);
                zipOutputStream.closeEntry();
                LOGGER.info("Added {} to zip file", fileName);
            }
            LOGGER.info("saved zip file at location {}", homeDirectory.toAbsolutePath());
        } catch (IOException e) {
            var errMsg = "Could not zip collected information files";
            LOGGER.error(errMsg);
            throw new RuntimeException(errMsg, e);
        }
        files.forEach(file -> {
            try {
                Files.delete(file);
            } catch (final IOException e) {
                var errMsg = "Could not delete file %s used to collect the data".formatted(file.getFileName());
                LOGGER.error(errMsg);
                throw new RuntimeException(errMsg, e);
            }
        });
        return homeDirectory.toAbsolutePath().toString();
    }

    private static Path collectRuntimeInformation() {
        Map<String, String> runtimeInfos = new LinkedHashMap<>();

        runtimeInfos.put("os name", Constants.OS_NAME);
        runtimeInfos.put("os version", Constants.OS_VERSION);
        runtimeInfos.put("os arch", Constants.OS_ARCH);
        runtimeInfos.put("gpx animator version", Constants.APPNAME_VERSION);
        runtimeInfos.put("java version", Constants.JAVA_VERSION);
        runtimeInfos.put("user agent", Constants.USER_AGENT);

        final var byteToGigaByte = Math.pow(1024, 3);
        try {
            final var fileStores = FileSystems.getDefault().getFileStores();
            for (final var fileStore : fileStores) {
                runtimeInfos.put("file store", fileStore.name());
                runtimeInfos.put("total space", round(fileStore.getTotalSpace() / byteToGigaByte) + GB_MEASURE_UNIT);
                runtimeInfos.put("used space",
                        round((fileStore.getTotalSpace() - fileStore.getUnallocatedSpace()) / byteToGigaByte)
                                + GB_MEASURE_UNIT);
                runtimeInfos.put("available space",
                        round(fileStore.getUsableSpace() / byteToGigaByte) + GB_MEASURE_UNIT);
            }
        } catch (final IOException e) {
            throw new RuntimeException("Could not get file stores", e);
        }

        final var runtime = Runtime.getRuntime();
        runtimeInfos.put("number of available processors", String.valueOf(runtime.availableProcessors()));
        runtimeInfos.put("total memory for jvm", round(runtime.totalMemory() / byteToGigaByte) + GB_MEASURE_UNIT);
        runtimeInfos.put("max memory for jvm", round(runtime.maxMemory() / byteToGigaByte) + GB_MEASURE_UNIT);
        runtimeInfos.put("memory usage of jvm",
                round((runtime.totalMemory() - runtime.freeMemory()) / byteToGigaByte) + GB_MEASURE_UNIT);

        final var lines = runtimeInfos.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue())
                .toList();

        final var path = Paths.get("runtime_information.txt");
        writeToFile(path, lines);
        return path;
    }

    private static Path getLogbackFileName() {
        final var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        final var fileAppender = (FileAppender<?>) logbackLogger.getAppender("FILE");
        final var file = fileAppender.getFile();

        return Paths.get(file);
    }

    private static void writeToFile(final Path path, final List<String> lines) {
        var fileName = path.getFileName();
        if (fileName == null) {
            return;
        }
        final var title = fileName.toString();
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (final IOException e) {
            throw new RuntimeException("Could not write " + title + " file", e);
        }
    }

    private static double round(final double value) {
        var bd = BigDecimal.valueOf(value);
        bd = bd.setScale(2, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}

