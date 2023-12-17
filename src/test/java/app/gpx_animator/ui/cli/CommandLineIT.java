package app.gpx_animator.ui.cli;

import app.gpx_animator.Main;
import app.gpx_animator.MemoryAppender;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.Renderer;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.Scanner;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class CommandLineIT {

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private MemoryAppender memoryAppender = null;

    private String checkFileSeparator(@SuppressWarnings("SameParameterValue") final String path) {
        return System.getProperty("os.name").toLowerCase(Locale.getDefault()).startsWith("windows")
                ? path.replaceAll("/", Matcher.quoteReplacement(File.separator))
                : path;
    }

    private String getTemporaryOutputFile() throws IOException {
        final var output = File.createTempFile("gpx-animator-test_", ".mp4");
        output.deleteOnExit();
        return output.getAbsolutePath();
    }

    private void assertDone() {
        final var done = memoryAppender.search("Done", Level.INFO).get(0).getMessage().contains("Movie written to");
        assertTrue(done, "Rendering not finished successfully! Check console and log output.");
    }

    @BeforeEach
    public void beforeEachTest() {
        final var rendererLogger = (Logger) LoggerFactory.getLogger(Renderer.class);
        final var frameLogger = (Logger) LoggerFactory.getLogger(Main.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        rendererLogger.setLevel(Level.DEBUG);
        rendererLogger.addAppender(memoryAppender);
        frameLogger.setLevel(Level.DEBUG);
        frameLogger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    void testBasicCommandLine() throws Exception {
        final var outputFile = getTemporaryOutputFile();
        final var args = new String[] {
                "--input", checkFileSeparator("./src/test/resources/gpx/bikeride.gpx"),
                "--output", outputFile
        };

        Main.start(args);
        assertDone();

        final var fileSize = new File(outputFile).length();
        assertTrue(fileSize > 150_000, "Output file size (%s bytes) too small, check content".formatted(fileSize));
        assertTrue(fileSize < 350_000, "Output file size (%s bytes) too big, check content".formatted(fileSize));
    }

    @Test
    void testPhotoPluginThroughCommandLine() throws Exception {
        // given
        final var outputFile = getTemporaryOutputFile();
        final var args = new String[] {
                "--input", checkFileSeparator("./src/test/resources/gpx/bikeride.gpx"),
                "--output", outputFile,
                "--photo-dir", checkFileSeparator("./src/test/resources/photo/directoryWithSinglePhoto"),
                "--photo-freeze-frame-time", "1000",
                "--photo-time", "3000",
                "--photo-animation-duration", "700"
        };

        // when
        Main.start(args);

        // then
        assertDone();

        final var photoName = "bikeride.jpg";
        final var localizedMessage = resourceBundle.getString("photos.progress.rendering");
        final var renderPhotoLoggingEvents = memoryAppender.searchFormattedMessages(localizedMessage.formatted(photoName), Level.INFO);
        var previousProgressPercentage = 0;
        assertEquals(193, renderPhotoLoggingEvents.size());
        for (final var loggingEvent : renderPhotoLoggingEvents) {
            var message = loggingEvent.getFormattedMessage();
            int progressPercentage = new Scanner(message).useDelimiter("\\D+").nextInt();
            assertTrue(progressPercentage >= previousProgressPercentage, "Progress percentage should not be shrinking");
            previousProgressPercentage = progressPercentage;
        }

        final var fileSize = new File(outputFile).length();
        assertTrue(fileSize > 350_000, "Output file size (%s bytes) too small, check content".formatted(fileSize));
        assertTrue(fileSize < 450_000, "Output file size (%s bytes) too big, check content".formatted(fileSize));
    }

    @AfterEach
    public void afterEachTest() {
        memoryAppender.stop();
        memoryAppender.reset();
    }

}
