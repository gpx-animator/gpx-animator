package app.gpx_animator.ui;

import app.gpx_animator.Preferences;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class MarkdownDialog extends JDialog {

    private static final long serialVersionUID = 1629914977489396863L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient String filename;
    private final transient Map<String, String> variables;

    public MarkdownDialog(final JFrame owner, final String title,
                          @NonNls final String filename,
                          final int width, final int height) {
        this(owner, title, filename, new HashMap<>(), width, height);
    }

    public MarkdownDialog(final JFrame owner, final String title,
                          @NonNls final String filename,
                          final Map<String, String> variables,
                          final int width, final int height) {
        super(owner, true);
        this.filename = filename;
        this.variables = variables;
        setTitle(title);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        setSize(width, height);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private JComponent buildContent() {
        final JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setText(parseVariables());
        textPane.setCaretPosition(0);

        final JScrollPane scrollPane = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JButton closeButton = new JButton(resourceBundle.getString("ui.dialog.markdown.button.close"));
        closeButton.addActionListener(e -> SwingUtilities.invokeLater(this::closeDialog));

        return FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("fill:200dlu:grow") //NON-NLS
                .rows("fill:100dlu:grow, 10dlu, p") //NON-NLS
                .add(scrollPane).xy(1, 1)
                .addBar(closeButton).xy(1, 3, CellConstraints.RIGHT, CellConstraints.FILL)
                .build();
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    @SuppressFBWarnings(value = { "NP_LOAD_OF_KNOWN_NULL_VALUE", "RCN_REDUNDANT_NULLCHECK_OF_NONNULL_VALUE", //NON-NLS
            "RCN_REDUNDANT_NULLCHECK_OF_NULL_VALUE" }, justification = "Check for null exactly as needed") //NON-NLS
    private String readFileAsMarkdown() throws IOException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream(filename)) {
            if (is == null) {
                return null;
            }
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private String parseVariables() {
        String html = readFileAsHTML();
        for (Map.Entry<String, String> variable : variables.entrySet()) {
            final String placeholder = "\\{\\{".concat(variable.getKey()).concat("\\s*\\}\\}"); //NON-NLS
            html = html.replaceAll(placeholder, variable.getValue());
        }
        return html;
    }

    private String readFileAsHTML() {
        try {
            final String md = readFileAsMarkdown();
            return convertMarkdownToHTML(md);
        } catch (final IOException | NullPointerException e) { // NOPMD -- NPE happens on missing file
            e.printStackTrace();
            JOptionPane.showMessageDialog(MarkdownDialog.this,
                    String.format(resourceBundle.getString("ui.dialog.markdown.errors.loading"), e.getMessage()),
                    resourceBundle.getString("ui.dialog.markdown.error"), JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(this::closeDialog);
            return "";
        }
    }

    private String convertMarkdownToHTML(final String md) {
        final MutableDataSet options = new MutableDataSet();
        final Parser parser = Parser.builder(options).build();
        final HtmlRenderer renderer = HtmlRenderer.builder(options).build();
        final Node document = parser.parse(md);
        return renderer.render(document);
    }

}
