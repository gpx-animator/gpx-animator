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

import sk.freemap.gpxAnimator.Help;
import sk.freemap.gpxAnimator.Help.OptionHelpWriter;
import sk.freemap.gpxAnimator.Option;

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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

public class UsageDialog extends JDialog {

    private static final long serialVersionUID = -8639477664121609849L;

    /**
     * Create the dialog.
     */
    public UsageDialog() {
        setTitle("Usage");
        setBounds(100, 100, 657, 535);
        getContentPane().setLayout(new BorderLayout());
        final JPanel contentPanel = new JPanel();
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));

        final JEditorPane dtrpngpxNavigator = new JEditorPane();
        dtrpngpxNavigator.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
        dtrpngpxNavigator.setEditable(false);
        dtrpngpxNavigator.setContentType("text/html");

        final StringWriter sw = new StringWriter();
        final PrintWriter pw = new PrintWriter(sw);
        pw.println("<dl>");
        Help.printHelp(new OptionHelpWriter() {
            @Override
            public void writeOptionHelp(final Option option, final String argument, final boolean track, final Object defaultValue) {
                // TODO html escape
                pw.print("<dt><b>--");
                pw.print(option.getName());
                if (argument != null) {
                    pw.print(" &lt;");
                    pw.print(argument);
                    pw.print("&gt;");
                }
                pw.println("</b></dt>");
                pw.print("<dd>");
                pw.print(option.getHelp());
                if (track) {
                    pw.print("; can be specified multiple times if multiple tracks are provided");
                }
                if (defaultValue != null) {
                    pw.print("; default ");
                    pw.print(defaultValue);
                }
                pw.println("</dd>");
            }
        });
        pw.println("</dl>");
        pw.close();

        dtrpngpxNavigator.setText("Commandline parameters:" + sw.toString());

        dtrpngpxNavigator.setCaretPosition(0);

        final JScrollPane scrollPane = new JScrollPane(dtrpngpxNavigator);
        contentPanel.add(scrollPane);

        final JPanel buttonPane = new JPanel();
        buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
        getContentPane().add(buttonPane, BorderLayout.SOUTH);

        final JButton okButton = new JButton("OK");
        okButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                UsageDialog.this.dispose();
            }
        });
        okButton.setActionCommand("OK");
        buttonPane.add(okButton);
        getRootPane().setDefaultButton(okButton);
    }

}
