package sk.freemap.gpxAnimator.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class ColorSelector extends JPanel {
	private final JTextField colorTextField;

	/**
	 * Create the panel.
	 */
	public ColorSelector() {
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		
		colorTextField = new JTextField();
		colorTextField.setMaximumSize(new Dimension(2147483647, 21));
		colorTextField.setPreferredSize(new Dimension(55, 21));
		add(colorTextField);
		colorTextField.setColumns(10);
		
		final Component rigidArea = Box.createRigidArea(new Dimension(5, 0));
		add(rigidArea);
		
		final JButton selectButton = new JButton("Select");
		selectButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JColorChooser chooserPane = new JColorChooser();
				chooserPane.setColor(colorTextField.getBackground());
				final ActionListener okListener = new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						final Color color = chooserPane.getColor();
						colorTextField.setBackground(color);
						colorTextField.setText("#" + Integer.toHexString(color.getRGB()).toUpperCase());
					}
				};
				final JDialog colorChooser = JColorChooser.createDialog(ColorSelector.this, "Track Color", true, chooserPane, okListener, null);
				colorChooser.setVisible(true);
			}
		});		add(selectButton);

	}
}
