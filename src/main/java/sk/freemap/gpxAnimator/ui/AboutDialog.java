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
package sk.freemap.gpxAnimator.ui;

import sk.freemap.gpxAnimator.Constants;
import sk.freemap.gpxAnimator.Preferences;

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
import java.util.ResourceBundle;

public class AboutDialog extends JDialog {

    private static final long serialVersionUID = 8374270428933983176L;

    /**
     * Create the dialog.
     */
    public AboutDialog() {
        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        setTitle(resourceBundle.getString("ui.dialog.about.title"));
        setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.LINE_AXIS));

        @SuppressWarnings({"HardCodedStringLiteral", "StringConcatenation"}) // Just putting together some HTML code
        final String text = String.format(
                "<div align=\"center\"><h1>%s</h1>Version %s<br/>"
                + "Copyright &copy; %s <a href=\"http://www.freemap.sk/\">Freemap Slovakia</a></div>"
                + "<p>%s</p><p>%s</p>", Constants.APPNAME, Constants.VERSION, Constants.YEAR,
                resourceBundle.getString("ui.dialog.about.description"),
                String.format(resourceBundle.getString("ui.dialog.about.link"),
                        "<a href=\"https://gpx-animator.app\">https://gpx-animator.app</a>"));

        final JEditorPane dtrpngpxNavigator = new JEditorPane();
        dtrpngpxNavigator.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        dtrpngpxNavigator.setEditable(false);
        dtrpngpxNavigator.setContentType("text/html");
        dtrpngpxNavigator.setText(text);

        final JScrollPane scrollPane = new JScrollPane(dtrpngpxNavigator);
        contentPanel.add(scrollPane);

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(buttonPane, BorderLayout.PAGE_END);

        final JButton okButton = new JButton(resourceBundle.getString("ui.dialog.about.button.ok"));
        okButton.addActionListener(e -> AboutDialog.this.dispose());
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
    }
}
