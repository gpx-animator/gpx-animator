package sk.freemap.gpxAnimator.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileSelector extends JPanel {
	private final JTextField fileTextField;

	/**
	 * Create the panel.
	 */
	public FileSelector() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		fileTextField = new JTextField();
		fileTextField.setMaximumSize(new Dimension(2147483647, 21));
		fileTextField.setPreferredSize(new Dimension(55, 21));
		add(fileTextField);
		fileTextField.setColumns(10);
		
		final Component rigidArea = Box.createRigidArea(new Dimension(5, 0));
		add(rigidArea);
		
		final JButton btnNewButton = new JButton("Browse");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JFileChooser gpxFileChooser = new JFileChooser();
				final FileFilter filter = new FileNameExtensionFilter("GPX Files", "gpx");
				gpxFileChooser.setAcceptAllFileFilterUsed(false);
				gpxFileChooser.addChoosableFileFilter(filter);
				if (gpxFileChooser.showOpenDialog(FileSelector.this) == JFileChooser.APPROVE_OPTION) {
					fileTextField.setText(gpxFileChooser.getSelectedFile().toString());
				}
				
//				final JFileChooser chooser = new JFileChooser();
//				chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
//				chooser.showOpenDialog(MainFrame.this);
			}
		});
		add(btnNewButton);

	}
}
