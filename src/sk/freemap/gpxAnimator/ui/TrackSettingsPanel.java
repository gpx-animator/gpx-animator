package sk.freemap.gpxAnimator.ui;

import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class TrackSettingsPanel extends JPanel {

	private static final long serialVersionUID = 2492074184123083022L;
	private final JLabel lblLabel;
	private final JTextField textField_1;
	private final JLabel lblLineWidth;
	private final JLabel lblTimeOffset;
	private final JLabel lblForcedPointTime;
	private final JSpinner spinner_1;
	private final JSpinner spinner_2;
	private final JSpinner spinner_3;
	private final JLabel lblColor_1;
	private final ColorSelector colorSelector;
	private final FileSelector fileSelector;


	public TrackSettingsPanel(final ActionListener removeActionListener) {
		setBounds(100, 100, 595, 419);
		setBorder(new EmptyBorder(5, 5, 5, 5));
		final GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{0, 0, 0};
		gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
		gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
		setLayout(gbl_contentPane);
		
		final JLabel lblGpx = new JLabel("Input GPX File");
		final GridBagConstraints gbc_lblGpx = new GridBagConstraints();
		gbc_lblGpx.insets = new Insets(0, 0, 5, 5);
		gbc_lblGpx.anchor = GridBagConstraints.EAST;
		gbc_lblGpx.gridx = 0;
		gbc_lblGpx.gridy = 0;
		add(lblGpx, gbc_lblGpx);
		
		fileSelector = new FileSelector();
		final GridBagConstraints gbc_fileSelector = new GridBagConstraints();
		gbc_fileSelector.insets = new Insets(0, 0, 5, 0);
		gbc_fileSelector.fill = GridBagConstraints.BOTH;
		gbc_fileSelector.gridx = 1;
		gbc_fileSelector.gridy = 0;
		add(fileSelector, gbc_fileSelector);
		
		lblLabel = new JLabel("Label");
		final GridBagConstraints gbc_lblLabel = new GridBagConstraints();
		gbc_lblLabel.anchor = GridBagConstraints.EAST;
		gbc_lblLabel.insets = new Insets(0, 0, 5, 5);
		gbc_lblLabel.gridx = 0;
		gbc_lblLabel.gridy = 1;
		add(lblLabel, gbc_lblLabel);
		
		textField_1 = new JTextField();
		final GridBagConstraints gbc_textField_1 = new GridBagConstraints();
		gbc_textField_1.insets = new Insets(0, 0, 5, 0);
		gbc_textField_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_textField_1.gridx = 1;
		gbc_textField_1.gridy = 1;
		add(textField_1, gbc_textField_1);
		textField_1.setColumns(10);
		
		lblColor_1 = new JLabel("Color");
		final GridBagConstraints gbc_lblColor_1 = new GridBagConstraints();
		gbc_lblColor_1.anchor = GridBagConstraints.EAST;
		gbc_lblColor_1.insets = new Insets(0, 0, 5, 5);
		gbc_lblColor_1.gridx = 0;
		gbc_lblColor_1.gridy = 2;
		add(lblColor_1, gbc_lblColor_1);
		
		colorSelector = new ColorSelector();
		final GridBagConstraints gbc_colorSelector = new GridBagConstraints();
		gbc_colorSelector.insets = new Insets(0, 0, 5, 0);
		gbc_colorSelector.fill = GridBagConstraints.BOTH;
		gbc_colorSelector.gridx = 1;
		gbc_colorSelector.gridy = 2;
		add(colorSelector, gbc_colorSelector);
		
		lblLineWidth = new JLabel("Line Width");
		final GridBagConstraints gbc_lblLineWidth = new GridBagConstraints();
		gbc_lblLineWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblLineWidth.anchor = GridBagConstraints.EAST;
		gbc_lblLineWidth.gridx = 0;
		gbc_lblLineWidth.gridy = 3;
		add(lblLineWidth, gbc_lblLineWidth);
		
		spinner_3 = new JSpinner();
		spinner_3.setFont(new Font("Dialog", Font.PLAIN, 12));
		spinner_3.setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(1)));
		final GridBagConstraints gbc_spinner_3 = new GridBagConstraints();
		gbc_spinner_3.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_3.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_3.gridx = 1;
		gbc_spinner_3.gridy = 3;
		add(spinner_3, gbc_spinner_3);
		
		lblTimeOffset = new JLabel("Time Offset");
		final GridBagConstraints gbc_lblTimeOffset = new GridBagConstraints();
		gbc_lblTimeOffset.anchor = GridBagConstraints.EAST;
		gbc_lblTimeOffset.insets = new Insets(0, 0, 5, 5);
		gbc_lblTimeOffset.gridx = 0;
		gbc_lblTimeOffset.gridy = 4;
		add(lblTimeOffset, gbc_lblTimeOffset);
		
		spinner_2 = new JSpinner();
		final GridBagConstraints gbc_spinner_2 = new GridBagConstraints();
		gbc_spinner_2.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_2.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_2.gridx = 1;
		gbc_spinner_2.gridy = 4;
		add(spinner_2, gbc_spinner_2);
		
		lblForcedPointTime = new JLabel("Forced Point Time Interval");
		final GridBagConstraints gbc_lblForcedPointTime = new GridBagConstraints();
		gbc_lblForcedPointTime.anchor = GridBagConstraints.EAST;
		gbc_lblForcedPointTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblForcedPointTime.gridx = 0;
		gbc_lblForcedPointTime.gridy = 5;
		add(lblForcedPointTime, gbc_lblForcedPointTime);
		
		spinner_1 = new JSpinner();
		final GridBagConstraints gbc_spinner_1 = new GridBagConstraints();
		gbc_spinner_1.insets = new Insets(0, 0, 5, 0);
		gbc_spinner_1.fill = GridBagConstraints.HORIZONTAL;
		gbc_spinner_1.gridx = 1;
		gbc_spinner_1.gridy = 5;
		add(spinner_1, gbc_spinner_1);
		
		if (removeActionListener != null) {
			final JButton btnNewButton = new JButton("Remove Track");
			btnNewButton.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(final ActionEvent e) {
					removeActionListener.actionPerformed(new ActionEvent(TrackSettingsPanel.this, ActionEvent.ACTION_PERFORMED, ""));
				}
			});
			final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
			gbc_btnNewButton.anchor = GridBagConstraints.EAST;
			gbc_btnNewButton.gridwidth = 3;
			gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
			gbc_btnNewButton.gridx = 0;
			gbc_btnNewButton.gridy = 7;
			add(btnNewButton, gbc_btnNewButton);
		}
	}

}
