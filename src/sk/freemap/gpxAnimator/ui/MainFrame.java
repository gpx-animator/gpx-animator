package sk.freemap.gpxAnimator.ui;

import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

public class MainFrame extends JFrame {

	private final JPanel contentPane;
	private final JSpinner heightSpinner;

	/**
	 * Launch the application.
	 */
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					final MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (final Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 681, 606);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		final GridBagLayout gbl_contentPane = new GridBagLayout();
		gbl_contentPane.columnWidths = new int[]{438, 0, 0};
		gbl_contentPane.rowHeights = new int[]{264, 0, 0};
		gbl_contentPane.columnWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		gbl_contentPane.rowWeights = new double[]{1.0, 0.0, Double.MIN_VALUE};
		contentPane.setLayout(gbl_contentPane);
		
		final JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		final GridBagConstraints gbc_tabbedPane = new GridBagConstraints();
		gbc_tabbedPane.gridwidth = 2;
		gbc_tabbedPane.insets = new Insets(0, 0, 5, 0);
		gbc_tabbedPane.fill = GridBagConstraints.BOTH;
		gbc_tabbedPane.gridx = 0;
		gbc_tabbedPane.gridy = 0;
		contentPane.add(tabbedPane, gbc_tabbedPane);
		
		final JScrollPane generalScrollPane = new JScrollPane();
		tabbedPane.addTab("General", null, generalScrollPane, null);
		
		final JPanel tabContentPanel = new JPanel();
		tabContentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		
		generalScrollPane.setViewportView(tabContentPanel);
//		tabbedPane.addTab("General", null, panel, null);
		final GridBagLayout gbl_tabContentPanel = new GridBagLayout();
		gbl_tabContentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_tabContentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_tabContentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_tabContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		tabContentPanel.setLayout(gbl_tabContentPanel);
		
		final JLabel lblOutput = new JLabel("Output");
		final GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.EAST;
		gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutput.gridx = 0;
		gbc_lblOutput.gridy = 0;
		tabContentPanel.add(lblOutput, gbc_lblOutput);
		
		final FileSelector fileSelector = new FileSelector();
		final GridBagConstraints gbc_fileSelector = new GridBagConstraints();
		gbc_fileSelector.insets = new Insets(0, 0, 5, 0);
		gbc_fileSelector.fill = GridBagConstraints.BOTH;
		gbc_fileSelector.gridx = 1;
		gbc_fileSelector.gridy = 0;
		tabContentPanel.add(fileSelector, gbc_fileSelector);
		
		final JLabel lblWidth = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		tabContentPanel.add(lblWidth, gbc_lblWidth);
		
		final JSpinner widthSpinner = new JSpinner();
		widthSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
		gbc_widthSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_widthSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_widthSpinner.gridx = 1;
		gbc_widthSpinner.gridy = 1;
		tabContentPanel.add(widthSpinner, gbc_widthSpinner);
		
		final JLabel lblHeight = new JLabel("Height");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 2;
		tabContentPanel.add(lblHeight, gbc_lblHeight);
		
		heightSpinner = new JSpinner();
		heightSpinner.setModel(new EmptyNullSpinnerModel());
		heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner));
		final GridBagConstraints gbc_heightSpinner = new GridBagConstraints();
		gbc_heightSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_heightSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_heightSpinner.gridx = 1;
		gbc_heightSpinner.gridy = 2;
		tabContentPanel.add(heightSpinner, gbc_heightSpinner);
		
		final JLabel lblZoom = new JLabel("Zoom");
		final GridBagConstraints gbc_lblZoom = new GridBagConstraints();
		gbc_lblZoom.anchor = GridBagConstraints.EAST;
		gbc_lblZoom.insets = new Insets(0, 0, 5, 5);
		gbc_lblZoom.gridx = 0;
		gbc_lblZoom.gridy = 3;
		tabContentPanel.add(lblZoom, gbc_lblZoom);
		
		final JSpinner zoomSpinner = new JSpinner();
		zoomSpinner.setModel(new SpinnerNumberModel(0, 0, 18, 1));
		final GridBagConstraints gbc_zoomSpinner = new GridBagConstraints();
		gbc_zoomSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_zoomSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_zoomSpinner.gridx = 1;
		gbc_zoomSpinner.gridy = 3;
		tabContentPanel.add(zoomSpinner, gbc_zoomSpinner);
		
		final JLabel lblMargin = new JLabel("Margin");
		final GridBagConstraints gbc_lblMargin = new GridBagConstraints();
		gbc_lblMargin.anchor = GridBagConstraints.EAST;
		gbc_lblMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMargin.gridx = 0;
		gbc_lblMargin.gridy = 4;
		tabContentPanel.add(lblMargin, gbc_lblMargin);
		
		final JSpinner marginSpinner = new JSpinner();
		final GridBagConstraints gbc_marginSpinner = new GridBagConstraints();
		gbc_marginSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_marginSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_marginSpinner.gridx = 1;
		gbc_marginSpinner.gridy = 4;
		tabContentPanel.add(marginSpinner, gbc_marginSpinner);
		
		final JLabel lblSpeedup = new JLabel("Speedup");
		final GridBagConstraints gbc_lblSpeedup = new GridBagConstraints();
		gbc_lblSpeedup.anchor = GridBagConstraints.EAST;
		gbc_lblSpeedup.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeedup.gridx = 0;
		gbc_lblSpeedup.gridy = 5;
		tabContentPanel.add(lblSpeedup, gbc_lblSpeedup);
		
		final JSpinner sppedupSpinner = new JSpinner();
		final GridBagConstraints gbc_sppedupSpinner = new GridBagConstraints();
		gbc_sppedupSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_sppedupSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_sppedupSpinner.gridx = 1;
		gbc_sppedupSpinner.gridy = 5;
		tabContentPanel.add(sppedupSpinner, gbc_sppedupSpinner);
		
		final JLabel lblMarkerSize = new JLabel("Marker Size");
		final GridBagConstraints gbc_lblMarkerSize = new GridBagConstraints();
		gbc_lblMarkerSize.anchor = GridBagConstraints.EAST;
		gbc_lblMarkerSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarkerSize.gridx = 0;
		gbc_lblMarkerSize.gridy = 6;
		tabContentPanel.add(lblMarkerSize, gbc_lblMarkerSize);
		
		final JSpinner markerSizeSpinner = new JSpinner();
		final GridBagConstraints gbc_markerSizeSpinner = new GridBagConstraints();
		gbc_markerSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_markerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_markerSizeSpinner.gridx = 1;
		gbc_markerSizeSpinner.gridy = 6;
		tabContentPanel.add(markerSizeSpinner, gbc_markerSizeSpinner);
		
		final JLabel lblWaypointSize = new JLabel("Waypoint Size");
		final GridBagConstraints gbc_lblWaypointSize = new GridBagConstraints();
		gbc_lblWaypointSize.anchor = GridBagConstraints.EAST;
		gbc_lblWaypointSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaypointSize.gridx = 0;
		gbc_lblWaypointSize.gridy = 7;
		tabContentPanel.add(lblWaypointSize, gbc_lblWaypointSize);
		
		final JSpinner waypintSizeSpinner = new JSpinner();
		waypintSizeSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_waypintSizeSpinner = new GridBagConstraints();
		gbc_waypintSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_waypintSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_waypintSizeSpinner.gridx = 1;
		gbc_waypintSizeSpinner.gridy = 7;
		tabContentPanel.add(waypintSizeSpinner, gbc_waypintSizeSpinner);
		
		final JLabel lblTailDuration = new JLabel("Tail Duration");
		final GridBagConstraints gbc_lblTailDuration = new GridBagConstraints();
		gbc_lblTailDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblTailDuration.anchor = GridBagConstraints.EAST;
		gbc_lblTailDuration.gridx = 0;
		gbc_lblTailDuration.gridy = 8;
		tabContentPanel.add(lblTailDuration, gbc_lblTailDuration);
		
		final JSpinner tailDurationSpinner = new JSpinner();
		tailDurationSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_tailDurationSpinner = new GridBagConstraints();
		gbc_tailDurationSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_tailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_tailDurationSpinner.gridx = 1;
		gbc_tailDurationSpinner.gridy = 8;
		tabContentPanel.add(tailDurationSpinner, gbc_tailDurationSpinner);
		
		final JLabel lblFps = new JLabel("FPS");
		final GridBagConstraints gbc_lblFps = new GridBagConstraints();
		gbc_lblFps.anchor = GridBagConstraints.EAST;
		gbc_lblFps.insets = new Insets(0, 0, 5, 5);
		gbc_lblFps.gridx = 0;
		gbc_lblFps.gridy = 9;
		tabContentPanel.add(lblFps, gbc_lblFps);
		
		final JSpinner fpsSpinner = new JSpinner();
		fpsSpinner.setModel(new SpinnerNumberModel(new Double(0), new Double(0), null, new Double(1)));
		final GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
		gbc_fpsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fpsSpinner.gridx = 1;
		gbc_fpsSpinner.gridy = 9;
		tabContentPanel.add(fpsSpinner, gbc_fpsSpinner);
		
		final JLabel lblTmsUrlTemplate = new JLabel("TMS URL Template");
		final GridBagConstraints gbc_lblTmsUrlTemplate = new GridBagConstraints();
		gbc_lblTmsUrlTemplate.anchor = GridBagConstraints.EAST;
		gbc_lblTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTmsUrlTemplate.gridx = 0;
		gbc_lblTmsUrlTemplate.gridy = 10;
		tabContentPanel.add(lblTmsUrlTemplate, gbc_lblTmsUrlTemplate);
		
		final JComboBox tmsUrlTemplateComboBox = new JComboBox();
		tmsUrlTemplateComboBox.setEditable(true);
		tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel(new String[] {"", "http://tile.openstreetmap.org/{zoom}/{x}/{y}.png", "http://t{switch:1,2,3,4}.freemap.sk/T/{zoom}/{x}/{y}.png"}));
		final GridBagConstraints gbc_tmsUrlTemplateComboBox = new GridBagConstraints();
		gbc_tmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tmsUrlTemplateComboBox.gridx = 1;
		gbc_tmsUrlTemplateComboBox.gridy = 10;
		tabContentPanel.add(tmsUrlTemplateComboBox, gbc_tmsUrlTemplateComboBox);
		
		final JLabel lblVisibility = new JLabel("Visibility");
		final GridBagConstraints gbc_lblVisibility = new GridBagConstraints();
		gbc_lblVisibility.anchor = GridBagConstraints.EAST;
		gbc_lblVisibility.insets = new Insets(0, 0, 5, 5);
		gbc_lblVisibility.gridx = 0;
		gbc_lblVisibility.gridy = 11;
		tabContentPanel.add(lblVisibility, gbc_lblVisibility);
		
		final JSlider visibilitySlider = new JSlider();
		visibilitySlider.setMinorTickSpacing(5);
		visibilitySlider.setPaintTicks(true);
		visibilitySlider.setMajorTickSpacing(10);
		visibilitySlider.setPaintLabels(true);
		final GridBagConstraints gbc_visibilitySlider = new GridBagConstraints();
		gbc_visibilitySlider.insets = new Insets(0, 0, 5, 0);
		gbc_visibilitySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_visibilitySlider.gridx = 1;
		gbc_visibilitySlider.gridy = 11;
		tabContentPanel.add(visibilitySlider, gbc_visibilitySlider);
		
		final JLabel lblFontSize = new JLabel("Font Size");
		final GridBagConstraints gbc_lblFontSize = new GridBagConstraints();
		gbc_lblFontSize.anchor = GridBagConstraints.EAST;
		gbc_lblFontSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontSize.gridx = 0;
		gbc_lblFontSize.gridy = 12;
		tabContentPanel.add(lblFontSize, gbc_lblFontSize);
		
		final JSpinner fontSizeSpinner = new JSpinner();
		fontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_fontSizeSpinner = new GridBagConstraints();
		gbc_fontSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fontSizeSpinner.gridx = 1;
		gbc_fontSizeSpinner.gridy = 12;
		tabContentPanel.add(fontSizeSpinner, gbc_fontSizeSpinner);
		
		final JLabel lblKeepIdle = new JLabel("Keep Idle");
		final GridBagConstraints gbc_lblKeepIdle = new GridBagConstraints();
		gbc_lblKeepIdle.anchor = GridBagConstraints.EAST;
		gbc_lblKeepIdle.insets = new Insets(0, 0, 5, 5);
		gbc_lblKeepIdle.gridx = 0;
		gbc_lblKeepIdle.gridy = 13;
		tabContentPanel.add(lblKeepIdle, gbc_lblKeepIdle);
		
		final JCheckBox keepIdleCheckBox = new JCheckBox("");
		final GridBagConstraints gbc_keepIdleCheckBox = new GridBagConstraints();
		gbc_keepIdleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_keepIdleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_keepIdleCheckBox.gridx = 1;
		gbc_keepIdleCheckBox.gridy = 13;
		tabContentPanel.add(keepIdleCheckBox, gbc_keepIdleCheckBox);
		
		final JLabel lblFlashbackColor = new JLabel("Flashback Color");
		final GridBagConstraints gbc_lblFlashbackColor = new GridBagConstraints();
		gbc_lblFlashbackColor.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlashbackColor.gridx = 0;
		gbc_lblFlashbackColor.gridy = 14;
		tabContentPanel.add(lblFlashbackColor, gbc_lblFlashbackColor);
		
		final ColorSelector flashbackColorSelector = new ColorSelector();
		final GridBagConstraints gbc_flashbackColorSelector = new GridBagConstraints();
		gbc_flashbackColorSelector.insets = new Insets(0, 0, 5, 0);
		gbc_flashbackColorSelector.fill = GridBagConstraints.BOTH;
		gbc_flashbackColorSelector.gridx = 1;
		gbc_flashbackColorSelector.gridy = 14;
		tabContentPanel.add(flashbackColorSelector, gbc_flashbackColorSelector);
		
		final JLabel lblFlashbackDuration = new JLabel("Flashback Duration");
		final GridBagConstraints gbc_lblFlashbackDuration = new GridBagConstraints();
		gbc_lblFlashbackDuration.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackDuration.insets = new Insets(0, 0, 0, 5);
		gbc_lblFlashbackDuration.gridx = 0;
		gbc_lblFlashbackDuration.gridy = 15;
		tabContentPanel.add(lblFlashbackDuration, gbc_lblFlashbackDuration);
		
		final JSpinner flashbackDurationSpinner = new JSpinner();
		flashbackDurationSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_flashbackDurationSpinner = new GridBagConstraints();
		gbc_flashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_flashbackDurationSpinner.gridx = 1;
		gbc_flashbackDurationSpinner.gridy = 15;
		tabContentPanel.add(flashbackDurationSpinner, gbc_flashbackDurationSpinner);
		
		final TrackSettingsPanel trackSettingsPanel = new TrackSettingsPanel(null);
		
		final JScrollPane trackScrollPane = new JScrollPane();
		tabbedPane.addTab("Track", null, trackScrollPane, null);
		trackScrollPane.setViewportView(trackSettingsPanel);
		
		final JButton button = new JButton("Remove track");
		button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
			}
		});
		final GridBagConstraints gbc_button = new GridBagConstraints();
		gbc_button.anchor = GridBagConstraints.EAST;
		gbc_button.gridwidth = 3;
		gbc_button.gridx = 0;
		gbc_button.gridy = 7;
		
		final JPanel buttonPanel = new JPanel();
		final GridBagConstraints gbc_buttonPanel = new GridBagConstraints();
		gbc_buttonPanel.anchor = GridBagConstraints.EAST;
		gbc_buttonPanel.gridwidth = 2;
		gbc_buttonPanel.fill = GridBagConstraints.VERTICAL;
		gbc_buttonPanel.gridx = 0;
		gbc_buttonPanel.gridy = 1;
		contentPane.add(buttonPanel, gbc_buttonPanel);
		
		final JButton addTrackButton = new JButton("Add Track");
		addTrackButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(final ActionEvent e) {
				final JScrollPane scrollPane = new JScrollPane();
				final TrackSettingsPanel trackSettingsPanel = new TrackSettingsPanel(new ActionListener() {
					@Override
					public void actionPerformed(final ActionEvent e) {
						tabbedPane.remove(scrollPane);
					}
				});
				scrollPane.setViewportView(trackSettingsPanel);
				tabbedPane.addTab("Track", null, scrollPane, null);
				tabbedPane.setSelectedComponent(scrollPane);
			}
		});
		buttonPanel.add(addTrackButton);
		
		final JButton startButton = new JButton("Start");
		buttonPanel.add(startButton);
	}
}
