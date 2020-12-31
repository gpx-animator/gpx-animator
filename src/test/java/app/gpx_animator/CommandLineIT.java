package app.gpx_animator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.regex.Matcher;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Tag("it")
public final class CommandLineIT {

    private transient MemoryAppender memoryAppender = null;

    private String checkFileSeparator(@SuppressWarnings("SameParameterValue") final String path) {
        return System.getProperty("os.name").toLowerCase(Locale.getDefault()).startsWith("windows")
                ? path.replaceAll("/", Matcher.quoteReplacement(File.separator))
                : path;
    }

    private String getTemporaryOutputFile() throws IOException {
        final File output = File.createTempFile("gpx-animator-test_", ".mp4");
        output.deleteOnExit();
        return output.getAbsolutePath();
    }

    private void assertDone() {
        final boolean done = memoryAppender.search("Done", Level.INFO).get(0).getMessage().contains("Movie written to");
        assertTrue(done, "Rendering not finished successfully! Check console and log output.");
    }

    @BeforeEach
    public void beforeEachTest() {
        final Logger logger = (Logger) LoggerFactory.getLogger(Renderer.class);
        memoryAppender = new MemoryAppender();
        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        logger.setLevel(Level.DEBUG);
        logger.addAppender(memoryAppender);
        memoryAppender.start();
    }

    @Test
    public void testBasicCommandLine() throws Exception {
        final var outputFile = getTemporaryOutputFile();
        final String[] args = new String[] {
                "--input", checkFileSeparator("./src/test/resources/gpx/bikeride.gpx"),
                "--output", outputFile
        };

        Main.start(args);
        assertDone();

        final var fileSize = new File(outputFile).length();
        assertTrue(fileSize > 550_000, "Output file size (%s bytes) too small, check content".formatted(fileSize));
        assertTrue(fileSize < 600_000, "Output file size (%s bytes) too big, check content".formatted(fileSize));
    }

    @AfterEach
    public void afterEachTest() {
        memoryAppender.stop();
        memoryAppender.reset();
    }

}
