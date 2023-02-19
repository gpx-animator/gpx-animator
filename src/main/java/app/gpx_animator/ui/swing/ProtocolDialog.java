package app.gpx_animator.ui.swing;

import app.gpx_animator.core.preferences.Preferences;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.JFrame;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ProtocolDialog extends MarkdownDialog {

    @Serial
    private static final long serialVersionUID = 913790912666544778L;

    private static final ResourceBundle RESOURCE_BUNDLE = Preferences.getResourceBundle();

    public ProtocolDialog(final JFrame owner, final int width, final int height) {
        super(owner, RESOURCE_BUNDLE.getString("ui.dialog.protocol.title"), getMarkdown(), width, height);
    }

    private static String getMarkdown() {
        final var content = readFile().collect(Collectors.joining(System.lineSeparator()));
        return "```%n%s%n```".formatted(content.isBlank() ? RESOURCE_BUNDLE.getString("ui.dialog.protocol.empty") : content);
    }

    private static Stream<String> readFile() {
        final var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        final var logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        final var fileAppender = (FileAppender<?>) logbackLogger.getAppender("FILE");
        final var file = fileAppender.getFile();

        try {
            return Files.lines(Path.of(file));
        } catch (final Exception e) {
            throw new RuntimeException("Unable to open log file '%s': %s".formatted(file, e.getMessage()), e);
        }
    }
}
