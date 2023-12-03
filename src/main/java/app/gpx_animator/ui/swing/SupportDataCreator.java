package app.gpx_animator.ui.swing;

import app.gpx_animator.core.Constants;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class SupportDataCreator {

    private SupportDataCreator() {
    }

    static void createSupportData() {
        List<Path> files = new ArrayList<>();

        files.add(collectRuntimeInformation());
        files.add(getLogbackFileName());
        files.forEach(System.out::println);

        zipFiles(files);
    }

    private static void zipFiles(List<Path> files) {
        try (var fileOutputStream = new FileOutputStream("support_data.zip");
             var zipOutputStream = new ZipOutputStream(fileOutputStream)) {
            for (Path file : files) {
                var fileBytes = Files.readAllBytes(file);
                zipOutputStream.putNextEntry(new ZipEntry(file.getFileName().toString()));
                zipOutputStream.write(fileBytes);
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not zip collected information files", e);
        }
    }

    private static Path collectRuntimeInformation() {
        Map<String, String> runtimeInfos = new HashMap<>();

        runtimeInfos.put("os name", Constants.OS_NAME);
        runtimeInfos.put("os version", Constants.OS_VERSION);
        runtimeInfos.put("os arch", Constants.OS_ARCH);
        runtimeInfos.put("gpx animator version", Constants.APPNAME_VERSION);
        runtimeInfos.put("java version", Constants.JAVA_VERSION);
        runtimeInfos.put("user agent", Constants.USER_AGENT);

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
        final var title = path.getFileName().toString();
        try {
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new RuntimeException("Could not write " + title + " file", e);
        }
    }
}

