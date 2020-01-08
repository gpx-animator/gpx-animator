package sk.freemap.gpxAnimator.ui;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import sk.freemap.gpxAnimator.Constants;
import sk.freemap.gpxAnimator.Preferences;

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
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class ChangelogDialog extends JDialog {

    private static final long serialVersionUID = 1629914924489696463L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    public ChangelogDialog(final JFrame owner) {
        super(owner, true);

        @SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"})
        final String title = String.format(resourceBundle.getString("ui.dialog.changelog.title"),
                Constants.APPNAME + " v" + Constants.VERSION);
        setTitle(title);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        final JTextPane changelogView = new JTextPane();
        changelogView.setEditable(false);
        changelogView.setContentType("text/html");
        changelogView.setText(readChangelogAsHTML());
        changelogView.setCaretPosition(0);

        final JScrollPane scrollPane = new JScrollPane(changelogView,
                JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        final JButton closeButton = new JButton(resourceBundle.getString("ui.dialog.changelog.button.close"));
        closeButton.addActionListener(e -> SwingUtilities.invokeLater(this::closeDialog));

        return FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("fill:300dlu:grow") //NON-NLS
                .rows("fill:300dlu:grow, 10dlu, p") //NON-NLS
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
    private String readChangelogAsMarkdown() throws IOException {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        try (InputStream is = classLoader.getResourceAsStream("CHANGELOG.md")) {
            if (is == null) {
                return null;
            }
            try (InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                 BufferedReader reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        }
    }

    private String readChangelogAsHTML() {
        try {
            final String md = readChangelogAsMarkdown();
            return convertMarkdownToHTML(md);
        } catch (final IOException | NullPointerException e) { // NOPMD -- NPE happens on missing changelog file
            e.printStackTrace();
            JOptionPane.showMessageDialog(ChangelogDialog.this,
                    String.format(resourceBundle.getString("ui.dialog.changelog.errors.loading"), e.getMessage()),
                    resourceBundle.getString("ui.dialog.changelog.error"), JOptionPane.ERROR_MESSAGE);
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
