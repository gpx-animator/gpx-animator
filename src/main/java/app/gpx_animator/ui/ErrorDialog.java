package app.gpx_animator.ui;

import app.gpx_animator.Preferences;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ResourceBundle;

public class ErrorDialog extends JDialog {

    private static final long serialVersionUID = 1628814974389392363L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient String message;
    private final transient Exception exception;

    public ErrorDialog(final JFrame owner, final String message, final Exception exception) {
        super(owner, true);
        this.message = message;
        this.exception = exception;
        setTitle(resourceBundle.getString("ui.dialog.error.title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setModal(true);
        setVisible(true);
    }

    private JComponent buildContent() {
        JTextArea messageText = new JTextArea(2, 20);
        messageText.setText(message);
        messageText.setWrapStyleWord(true);
        messageText.setLineWrap(true);
        messageText.setOpaque(false);
        messageText.setEditable(false);
        messageText.setFocusable(false);
        messageText.setBackground(UIManager.getColor("Label.background"));
        messageText.setFont(UIManager.getFont("Label.font"));
        messageText.setBorder(UIManager.getBorder("Label.border"));

        final JTextPane textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setText(getStrackTrace());
        textPane.setCaretPosition(0);

        final JScrollPane scrollPane = new JScrollPane(textPane,
                JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final JButton copyButton = new JButton(resourceBundle.getString("ui.dialog.error.button.copy"));
        copyButton.addActionListener(e -> SwingUtilities.invokeLater(this::copyMessage));
        final JButton closeButton = new JButton(resourceBundle.getString("ui.dialog.error.button.close"));
        closeButton.addActionListener(e -> SwingUtilities.invokeLater(this::closeDialog));

        return FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("fill:200dlu:grow") //NON-NLS
                .rows("p, 5dlu, fill:100dlu:grow, 10dlu, p") //NON-NLS
                .add(messageText).xy(1, 1)
                .add(scrollPane).xy(1, 3)
                .addBar(copyButton, closeButton).xy(1, 5, CellConstraints.RIGHT, CellConstraints.FILL)
                .build();
    }

    private String getStrackTrace() {
        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        if (exception != null) {
            pw.write(exception.getMessage() != null ? exception.getMessage() : exception.toString());
            pw.write("\n");
            exception.printStackTrace(pw);
        }
        return sw.toString();
    }

    private void copyMessage() {
        final StringSelection stringSelection = new StringSelection(getStrackTrace());
        final Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }

}
