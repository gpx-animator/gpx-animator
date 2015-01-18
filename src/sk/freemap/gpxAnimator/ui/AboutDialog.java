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

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class AboutDialog extends JDialog {

	private static final long serialVersionUID = 8374270428933983176L;
	
	private final JPanel contentPanel = new JPanel();

	/**
	 * Create the dialog.
	 */
	public AboutDialog() {
		setTitle("About");
		setBounds(100, 100, 450, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.X_AXIS));
		{
			final JEditorPane dtrpngpxNavigator = new JEditorPane();
			dtrpngpxNavigator.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
			dtrpngpxNavigator.setEditable(false);
			dtrpngpxNavigator.setContentType("text/html");
			dtrpngpxNavigator.setText("<div align=\"center\">\n<h1>GPX Animator</h1>\nver. 1.2.2<br/>\n&copy; 2014 <a href=\"http://www.freemap.sk/\">Freemap Slovakia</a>\n</div>\n"
					+ "<p>GPX Animator generates video from GPX files.</p>"
					+ "<p>More information can be found at <a href=\"http://zdila.github.com/gpx-animator/\">http://zdila.github.com/gpx-animator/</a>.</p>\n");

			{
				final JScrollPane scrollPane = new JScrollPane(dtrpngpxNavigator);
				contentPanel.add(scrollPane);
			}
		}
		{
			final JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				final JButton okButton = new JButton("OK");
				okButton.addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						AboutDialog.this.dispose();
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
		}
	}

}
