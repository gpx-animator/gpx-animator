/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.swing;

import app.gpx_animator.core.Help;
import app.gpx_animator.core.preferences.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.io.PrintWriter;
import java.io.Serial;
import java.io.StringWriter;

public class UsageDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = -8639477664121609849L;

    /**
     * Create the dialog.
     */
    public UsageDialog() {
        final var resourceBundle = Preferences.getResourceBundle();

        setTitle(resourceBundle.getString("ui.dialog.usage.title"));
        setBounds(100, 100, 657, 535);
        getContentPane().setLayout(new BorderLayout());
        final var contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));

        final var dtrpngpxNavigator = new JEditorPane();
        dtrpngpxNavigator.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        dtrpngpxNavigator.setEditable(false);
        dtrpngpxNavigator.setContentType("text/html");

        final var sw = new StringWriter();
        final var pw = new PrintWriter(sw);
        pw.println("<dl>"); //NON-NLS
        Help.printHelp((option, argument, track, defaultValue) -> {
            // TODO html escape
            pw.print("<dt><b>--"); //NON-NLS
            pw.print(option.getName());
            if (argument != null) {
                pw.print(" &lt;"); //NON-NLS
                pw.print(argument);
                pw.print("&gt;"); //NON-NLS
            }
            pw.println("</b></dt>"); //NON-NLS
            pw.print("<dd>"); //NON-NLS
            pw.print(option.getHelp());
            if (track) {
                pw.print("; ".concat(resourceBundle.getString("ui.dialog.usage.multiple")));
            }
            if (defaultValue != null) {
                pw.print("; ".concat(resourceBundle.getString("ui.dialog.usage.default")).concat(" "));
                pw.print(defaultValue);
            }
            pw.println("</dd>"); //NON-NLS
        });
        pw.println("</dl>"); //NON-NLS
        pw.close();

        dtrpngpxNavigator.setText(resourceBundle.getString("ui.dialog.usage.cliparams").concat(sw.toString()));

        dtrpngpxNavigator.setCaretPosition(0);

        final var scrollPane = new JScrollPane(dtrpngpxNavigator);
        contentPanel.add(scrollPane);

        final var buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(buttonPane, BorderLayout.PAGE_END);

        final var okButton = new JButton(resourceBundle.getString("ui.dialog.usage.button.ok"));
        okButton.addActionListener(e -> UsageDialog.this.dispose());
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
    }

}
