package app.gpx_animator.ui.swing;

import app.gpx_animator.core.preferences.Preferences;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

public class LogViewerDialog extends EscapeDialog {

    @Serial
    private static final long serialVersionUID = 5563604480066875446L;

    private static final int TEXT_SIZE = 14;

    public LogViewerDialog(final JFrame owner) {
        super(owner);

        final var resourceBundle = Preferences.getResourceBundle();

        setTitle(resourceBundle.getString("ui.dialog.logviewer.title"));
        setBounds(100, 100, 657, 535);
        getContentPane().setLayout(new BorderLayout());

        final var contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));
        var lines = readLogFile();
        var textArea = new JTextArea();
        var font = new Font(textArea.getFont().getName(), textArea.getFont().getStyle(), TEXT_SIZE);
        textArea.setFont(font);

        var log = lines.isBlank() ? "" : lines;
        textArea.setText(log);

        final var scrollPlane = new JScrollPane(textArea);
        contentPanel.add(scrollPlane);
    }

    private String readLogFile() {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        var fileAppender = (FileAppender<?>) logbackLogger.getAppender("FILE");
        var file = fileAppender.getFile();
        try (var lines = Files.lines(Path.of(file))){
            return lines.collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
