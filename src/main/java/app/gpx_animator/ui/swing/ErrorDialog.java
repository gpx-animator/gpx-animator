/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.swing;

import app.gpx_animator.core.preferences.Preferences;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.ScrollPaneConstants;

import javax.swing.WindowConstants;

import org.jetbrains.annotations.Nullable;

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
import java.awt.datatransfer.StringSelection;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.StringWriter;
import java.util.ResourceBundle;

public class ErrorDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 1628814974389392363L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final String message;
    private final String stackTrace;

    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    public ErrorDialog(final JFrame owner, final String message, final Exception exception) {
        super(owner, true);
        this.message = message;
        this.stackTrace = getStackTrace(exception);
        setTitle(resourceBundle.getString("ui.dialog.error.title"));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
        setModal(true);
        setVisible(true);
    }

    private JComponent buildContent() {
        var messageText = new JTextArea(2, 20);
        messageText.setText(message);
        messageText.setWrapStyleWord(true);
        messageText.setLineWrap(true);
        messageText.setOpaque(false);
        messageText.setEditable(false);
        messageText.setFocusable(false);
        messageText.setBackground(UIManager.getColor("Label.background"));
        messageText.setFont(UIManager.getFont("Label.font"));
        messageText.setBorder(UIManager.getBorder("Label.border"));

        final var textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setText(stackTrace);
        textPane.setCaretPosition(0);

        final var scrollPane = new JScrollPane(textPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final var copyButton = new JButton(resourceBundle.getString("ui.dialog.error.button.copy"));
        copyButton.addActionListener(e -> SwingUtilities.invokeLater(this::copyMessage));
        final var closeButton = new JButton(resourceBundle.getString("ui.dialog.error.button.close"));
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

    private String getStackTrace(@Nullable final Exception exception) {
        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);
        if (exception != null) {
            pw.write(exception.getMessage() != null ? exception.getMessage() : exception.toString());
            pw.write("\n");
            exception.printStackTrace(pw);
        }
        return sw.toString();
    }

    private void copyMessage() {
        final var stringSelection = new StringSelection(stackTrace);
        final var clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }

}
