package app.gpx_animator.ui.swing;

import app.gpx_animator.core.preferences.Preferences;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.FileAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.Serial;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

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

        var textArea = new JTextArea();
        textArea.setEditable(false);
        var font = new Font(textArea.getFont().getName(), textArea.getFont().getStyle(), TEXT_SIZE);
        textArea.setFont(font);

        var lines = readLogFile();
        var log = lines.isEmpty() ? "" : String.join("\n", lines);
        textArea.setText(log);

        final var scrollPlane = new JScrollPane(textArea);
        contentPanel.add(scrollPlane);

        final var buttonPanel = new JPanel();
        final var copyAllLogButton = new JButton(resourceBundle.getString("ui.dialog.logviewer.copyallbutton"));
        copyAllLogButton.addActionListener(e -> copyLog(lines));
        buttonPanel.add(copyAllLogButton);
        final var copy50LogButton = new JButton(resourceBundle.getString("ui.dialog.logviewer.copy50lines"));
        copy50LogButton.addActionListener(e -> {
            var copyLog = lines.stream().limit(50).toList();
            copyLog(copyLog);
        });
        buttonPanel.add(copy50LogButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
    }

    private static void copyLog(List<String> log) {
        final var copyLog = String.join("\n", log);
        final var selection = new StringSelection(copyLog);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(selection, null);
    }

    private List<String> readLogFile() {
        var loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
        var logbackLogger = loggerContext.getLogger(Logger.ROOT_LOGGER_NAME);
        var fileAppender = (FileAppender<?>) logbackLogger.getAppender("FILE");
        var file = fileAppender.getFile();
        try (var lines = Files.lines(Path.of(file))){
            return lines.toList();
        } catch (IOException e) {
            throw new RuntimeException("Unable to open log file %s".formatted(file));
        }
    }
}
