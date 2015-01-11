package sk.freemap.gpxAnimator.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Help;

abstract class GeneralSettingsPanel extends JPanel {

	private static final long serialVersionUID = -2024548578211891192L;
	
	private final JSpinner heightSpinner;
	private final FileSelector outputFileSelector;
	private final JSpinner widthSpinner;
	private final JSpinner zoomSpinner;
	private final JSpinner marginSpinner;
	private final JSpinner speedupSpinner;
	private final JSpinner markerSizeSpinner;
	private final JSpinner waypointSizeSpinner;
	private final JSpinner tailDurationSpinner;
	private final JSpinner fpsSpinner;
	private final JComboBox<MapTemplate> tmsUrlTemplateComboBox;
	private final JSlider backgroundMapVisibilitySlider;
	private final JSpinner fontSizeSpinner;
	private final JCheckBox skipIdleCheckBox;
	private final ColorSelector flashbackColorSelector;
	private final JSpinner flashbackDurationSpinner;
	private final JSpinner totalTimeSpinner;
	private JTextArea attributionTextArea;

	private List<MapTemplate> mapTamplateList;

	private final ChangeListener changeListener = new ChangeListener() {
		@Override
		public void stateChanged(final ChangeEvent e) {
			configurationChanged();
		}
	};
	
	private final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
		@Override
		public void propertyChange(final PropertyChangeEvent evt) {
			configurationChanged();
		}
	};
	
	public GeneralSettingsPanel() {
		mapTamplateList = readMaps();

		outputFileSelector = new FileSelector() {
			private static final long serialVersionUID = 7372002778976603239L;

			@Override
			protected Type configure(final JFileChooser outputFileChooser) {
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("JPEG Image Frames", "jpg"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("PNG Image Frames", "png"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("H.264 Encoded Video Files (*.mp4, *.mov, *.mkv)", "mp4", "mov", "mkv"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MPEG-1 Encoded Video Files (*.mpg)", "mpg"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MPEG-4 Encoded Video Files (*.avi)", "avi"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("MS MPEG-4 Encoded Video Files (*.wmv, *.asf)", "wmv", "asf"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("Theora Encoded Video Files (*.ogv)", "ogv"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("FLV Encoded Video Files (*.flv)", "flv"));
				outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("RV10 Encoded Video Files (*.rm)", "rm"));
				return Type.SAVE;
			}
			
			@Override
			protected String transformFilename(final String filename) {
				if ((filename.toLowerCase().endsWith(".png") || filename.toLowerCase().endsWith(".jpg"))
						&& String.format(filename, 100).equals(String.format(filename, 200))) {
					final int n = filename.lastIndexOf('.');
					return filename.substring(0, n) + "%08d" + filename.substring(n);
				} else {
					return filename;
				}
			}
		};
		
		outputFileSelector.setToolTipText(Help.HELP_OUTPUT);
		
		setBorder(new EmptyBorder(5, 5, 5, 5));
		
//		tabbedPane.addTab("General", null, panel, null);
		final GridBagLayout gbl_tabContentPanel = new GridBagLayout();
		gbl_tabContentPanel.columnWidths = new int[]{0, 0, 0};
		gbl_tabContentPanel.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
		gbl_tabContentPanel.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
		gbl_tabContentPanel.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gbl_tabContentPanel);

		final JLabel lblOutput = new JLabel("Output");
		final GridBagConstraints gbc_lblOutput = new GridBagConstraints();
		gbc_lblOutput.anchor = GridBagConstraints.EAST;
		gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
		gbc_lblOutput.gridx = 0;
		gbc_lblOutput.gridy = 0;
		add(lblOutput, gbc_lblOutput);

		final GridBagConstraints gbc_frameFileNamePatternFileSelector = new GridBagConstraints();
		gbc_frameFileNamePatternFileSelector.insets = new Insets(0, 0, 5, 0);
		gbc_frameFileNamePatternFileSelector.fill = GridBagConstraints.BOTH;
		gbc_frameFileNamePatternFileSelector.gridx = 1;
		gbc_frameFileNamePatternFileSelector.gridy = 0;
		add(outputFileSelector, gbc_frameFileNamePatternFileSelector);
		
		final JLabel lblWidth = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		add(lblWidth, gbc_lblWidth);
		
		widthSpinner = new JSpinner();
		widthSpinner.setToolTipText(Help.HELP_WIDTH);
		widthSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), null, new Integer(10)));
		widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
		final GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
		gbc_widthSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_widthSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_widthSpinner.gridx = 1;
		gbc_widthSpinner.gridy = 1;
		add(widthSpinner, gbc_widthSpinner);
		
		final JLabel lblHeight = new JLabel("Height");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 2;
		add(lblHeight, gbc_lblHeight);
		
		heightSpinner = new JSpinner();
		heightSpinner.setToolTipText(Help.HELP_HEIGHT);
		heightSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), null, new Integer(10)));
		heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner, Integer.class));
		final GridBagConstraints gbc_heightSpinner = new GridBagConstraints();
		gbc_heightSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_heightSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_heightSpinner.gridx = 1;
		gbc_heightSpinner.gridy = 2;
		add(heightSpinner, gbc_heightSpinner);
		
		final JLabel lblZoom = new JLabel("Zoom");
		final GridBagConstraints gbc_lblZoom = new GridBagConstraints();
		gbc_lblZoom.anchor = GridBagConstraints.EAST;
		gbc_lblZoom.insets = new Insets(0, 0, 5, 5);
		gbc_lblZoom.gridx = 0;
		gbc_lblZoom.gridy = 3;
		add(lblZoom, gbc_lblZoom);
		
		zoomSpinner = new JSpinner();
		zoomSpinner.setToolTipText(Help.HELP_ZOOM);
		zoomSpinner.setModel(new EmptyNullSpinnerModel(new Integer(1), new Integer(0), new Integer(18), new Integer(1)));
		zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));

		final GridBagConstraints gbc_zoomSpinner = new GridBagConstraints();
		gbc_zoomSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_zoomSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_zoomSpinner.gridx = 1;
		gbc_zoomSpinner.gridy = 3;
		add(zoomSpinner, gbc_zoomSpinner);
		
		final JLabel lblMargin = new JLabel("Margin");
		final GridBagConstraints gbc_lblMargin = new GridBagConstraints();
		gbc_lblMargin.anchor = GridBagConstraints.EAST;
		gbc_lblMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMargin.gridx = 0;
		gbc_lblMargin.gridy = 4;
		add(lblMargin, gbc_lblMargin);
		
		marginSpinner = new JSpinner();
		marginSpinner.setToolTipText(Help.HELP_MARGIN);
		marginSpinner.setModel(new SpinnerNumberModel(new Integer(0), new Integer(0), null, new Integer(1)));
		final GridBagConstraints gbc_marginSpinner = new GridBagConstraints();
		gbc_marginSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_marginSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_marginSpinner.gridx = 1;
		gbc_marginSpinner.gridy = 4;
		add(marginSpinner, gbc_marginSpinner);
		
		final JLabel lblSpeedup = new JLabel("Speedup");
		final GridBagConstraints gbc_lblSpeedup = new GridBagConstraints();
		gbc_lblSpeedup.anchor = GridBagConstraints.EAST;
		gbc_lblSpeedup.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeedup.gridx = 0;
		gbc_lblSpeedup.gridy = 5;
		add(lblSpeedup, gbc_lblSpeedup);
		
		speedupSpinner = new JSpinner();
		speedupSpinner.setToolTipText(Help.HELP_SPEEDUP);
		speedupSpinner.setModel(new EmptyNullSpinnerModel(new Double(0), new Double(0), null, new Double(1)));
		speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
		final GridBagConstraints gbc_sppedupSpinner = new GridBagConstraints();
		gbc_sppedupSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_sppedupSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_sppedupSpinner.gridx = 1;
		gbc_sppedupSpinner.gridy = 5;
		add(speedupSpinner, gbc_sppedupSpinner);
		
		speedupSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean enabled = speedupSpinner.getValue() == null;
				if (!enabled) {
					totalTimeSpinner.setValue(null);
				}
				totalTimeSpinner.setEnabled(enabled);
			}
		});
		
		final JLabel lblTotalTime = new JLabel("Total Time");
		final GridBagConstraints gbc_lblTotalTime = new GridBagConstraints();
		gbc_lblTotalTime.anchor = GridBagConstraints.EAST;
		gbc_lblTotalTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTime.gridx = 0;
		gbc_lblTotalTime.gridy = 6;
		add(lblTotalTime, gbc_lblTotalTime);
		
		totalTimeSpinner = new JSpinner();
		totalTimeSpinner.setToolTipText(Help.HELP_TOTAL_LENGTH);
		totalTimeSpinner.setModel(new DurationSpinnerModel());
		totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
		final GridBagConstraints gbc_totalTimeSpinner = new GridBagConstraints();
		gbc_totalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTimeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_totalTimeSpinner.gridx = 1;
		gbc_totalTimeSpinner.gridy = 6;
		add(totalTimeSpinner, gbc_totalTimeSpinner);
		
		totalTimeSpinner.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(final ChangeEvent e) {
				final boolean enabled = totalTimeSpinner.getValue() == null;
				if (!enabled) {
					speedupSpinner.setValue(null);
				}
				speedupSpinner.setEnabled(enabled);
			}
		});
		
		final JLabel lblMarkerSize = new JLabel("Marker Size");
		final GridBagConstraints gbc_lblMarkerSize = new GridBagConstraints();
		gbc_lblMarkerSize.anchor = GridBagConstraints.EAST;
		gbc_lblMarkerSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarkerSize.gridx = 0;
		gbc_lblMarkerSize.gridy = 7;
		add(lblMarkerSize, gbc_lblMarkerSize);
		
		markerSizeSpinner = new JSpinner();
		markerSizeSpinner.setToolTipText(Help.HELP_MARKER_SIZE);
		markerSizeSpinner.setEditor(new EmptyZeroNumberEditor(markerSizeSpinner, Double.class));
		markerSizeSpinner.setModel(new EmptyNullSpinnerModel(Double.valueOf(6.0), Double.valueOf(0.0), null, Double.valueOf(1.0)));
		final GridBagConstraints gbc_markerSizeSpinner = new GridBagConstraints();
		gbc_markerSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_markerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_markerSizeSpinner.gridx = 1;
		gbc_markerSizeSpinner.gridy = 7;
		add(markerSizeSpinner, gbc_markerSizeSpinner);
		
		final JLabel lblWaypointSize = new JLabel("Waypoint Size");
		final GridBagConstraints gbc_lblWaypointSize = new GridBagConstraints();
		gbc_lblWaypointSize.anchor = GridBagConstraints.EAST;
		gbc_lblWaypointSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaypointSize.gridx = 0;
		gbc_lblWaypointSize.gridy = 8;
		add(lblWaypointSize, gbc_lblWaypointSize);
		
		waypointSizeSpinner = new JSpinner();
		waypointSizeSpinner.setToolTipText(Help.HELP_WAYPOINT_SIZE);
		waypointSizeSpinner.setEditor(new EmptyZeroNumberEditor(waypointSizeSpinner, Double.class));
		waypointSizeSpinner.setModel(new EmptyNullSpinnerModel(Double.valueOf(1.0), Double.valueOf(0.0), null, Double.valueOf(1.0)));
		final GridBagConstraints gbc_waypintSizeSpinner = new GridBagConstraints();
		gbc_waypintSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_waypintSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_waypintSizeSpinner.gridx = 1;
		gbc_waypintSizeSpinner.gridy = 8;
		add(waypointSizeSpinner, gbc_waypintSizeSpinner);
		
		final JLabel lblTailDuration = new JLabel("Tail Duration");
		final GridBagConstraints gbc_lblTailDuration = new GridBagConstraints();
		gbc_lblTailDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblTailDuration.anchor = GridBagConstraints.EAST;
		gbc_lblTailDuration.gridx = 0;
		gbc_lblTailDuration.gridy = 9;
		add(lblTailDuration, gbc_lblTailDuration);
		
		tailDurationSpinner = new JSpinner();
		tailDurationSpinner.setToolTipText(Help.HELP_TAIL_DURATION);
		tailDurationSpinner.setModel(new DurationSpinnerModel());
		tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
		final GridBagConstraints gbc_tailDurationSpinner = new GridBagConstraints();
		gbc_tailDurationSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_tailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_tailDurationSpinner.gridx = 1;
		gbc_tailDurationSpinner.gridy = 9;
		add(tailDurationSpinner, gbc_tailDurationSpinner);
		
		final JLabel lblFps = new JLabel("FPS");
		final GridBagConstraints gbc_lblFps = new GridBagConstraints();
		gbc_lblFps.anchor = GridBagConstraints.EAST;
		gbc_lblFps.insets = new Insets(0, 0, 5, 5);
		gbc_lblFps.gridx = 0;
		gbc_lblFps.gridy = 10;
		add(lblFps, gbc_lblFps);
		
		fpsSpinner = new JSpinner();
		fpsSpinner.setToolTipText(Help.HELP_FPS);
		fpsSpinner.setModel(new SpinnerNumberModel(new Double(0.1), new Double(0.1), null, new Double(1)));
		final GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
		gbc_fpsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fpsSpinner.gridx = 1;
		gbc_fpsSpinner.gridy = 10;
		add(fpsSpinner, gbc_fpsSpinner);
		
		final JLabel lblTmsUrlTemplate = new JLabel("Background Map");
		final GridBagConstraints gbc_lblTmsUrlTemplate = new GridBagConstraints();
		gbc_lblTmsUrlTemplate.anchor = GridBagConstraints.EAST;
		gbc_lblTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTmsUrlTemplate.gridx = 0;
		gbc_lblTmsUrlTemplate.gridy = 11;
		add(lblTmsUrlTemplate, gbc_lblTmsUrlTemplate);
		
		tmsUrlTemplateComboBox = new JComboBox<MapTemplate>();
		tmsUrlTemplateComboBox.setToolTipText(Help.HELP_TMS_URL_TEMPLATE);
		tmsUrlTemplateComboBox.setEditable(true);
		tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<MapTemplate>(mapTamplateList.toArray(new MapTemplate[mapTamplateList.size()])));
		final GridBagConstraints gbc_tmsUrlTemplateComboBox = new GridBagConstraints();
		gbc_tmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tmsUrlTemplateComboBox.gridx = 1;
		gbc_tmsUrlTemplateComboBox.gridy = 11;
		add(tmsUrlTemplateComboBox, gbc_tmsUrlTemplateComboBox);
		
		final JLabel lblAttribution = new JLabel("Attribution");
		final GridBagConstraints gbc_lblAttribution = new GridBagConstraints();
		gbc_lblAttribution.anchor = GridBagConstraints.EAST;
		gbc_lblAttribution.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttribution.gridx = 0;
		gbc_lblAttribution.gridy = 12;
		add(lblAttribution, gbc_lblAttribution);
		
		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(3, 50));
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 12;
		add(scrollPane, gbc_scrollPane);
		
		attributionTextArea = new JTextArea();
		attributionTextArea.setToolTipText(Help.HELP_ATTRIBUTION);
		scrollPane.setViewportView(attributionTextArea);
		
		final JLabel lblVisibility = new JLabel("Map Visibility");
		final GridBagConstraints gbc_lblVisibility = new GridBagConstraints();
		gbc_lblVisibility.anchor = GridBagConstraints.EAST;
		gbc_lblVisibility.insets = new Insets(0, 0, 5, 5);
		gbc_lblVisibility.gridx = 0;
		gbc_lblVisibility.gridy = 13;
		add(lblVisibility, gbc_lblVisibility);
		
		backgroundMapVisibilitySlider = new JSlider();
		backgroundMapVisibilitySlider.setMinorTickSpacing(5);
		backgroundMapVisibilitySlider.setPaintTicks(true);
		backgroundMapVisibilitySlider.setMajorTickSpacing(10);
		backgroundMapVisibilitySlider.setPaintLabels(true);
		backgroundMapVisibilitySlider.setToolTipText(Help.HELP_BG_MAP_VISIBILITY);
		final GridBagConstraints gbc_backgroundMapVisibilitySlider = new GridBagConstraints();
		gbc_backgroundMapVisibilitySlider.insets = new Insets(0, 0, 5, 0);
		gbc_backgroundMapVisibilitySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_backgroundMapVisibilitySlider.gridx = 1;
		gbc_backgroundMapVisibilitySlider.gridy = 13;
		add(backgroundMapVisibilitySlider, gbc_backgroundMapVisibilitySlider);
		
		final JLabel lblFontSize = new JLabel("Font Size");
		final GridBagConstraints gbc_lblFontSize = new GridBagConstraints();
		gbc_lblFontSize.anchor = GridBagConstraints.EAST;
		gbc_lblFontSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontSize.gridx = 0;
		gbc_lblFontSize.gridy = 14;
		add(lblFontSize, gbc_lblFontSize);
		
		fontSizeSpinner = new JSpinner();
		fontSizeSpinner.setToolTipText(Help.HELP_FONT_SIZE);
		fontSizeSpinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
		final GridBagConstraints gbc_fontSizeSpinner = new GridBagConstraints();
		gbc_fontSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fontSizeSpinner.gridx = 1;
		gbc_fontSizeSpinner.gridy = 14;
		add(fontSizeSpinner, gbc_fontSizeSpinner);
		
		final JLabel lblSkipIdle = new JLabel("Skip Idle");
		final GridBagConstraints gbc_lblSkipIdle = new GridBagConstraints();
		gbc_lblSkipIdle.anchor = GridBagConstraints.EAST;
		gbc_lblSkipIdle.insets = new Insets(0, 0, 5, 5);
		gbc_lblSkipIdle.gridx = 0;
		gbc_lblSkipIdle.gridy = 15;
		add(lblSkipIdle, gbc_lblSkipIdle);
		
		skipIdleCheckBox = new JCheckBox("");
		skipIdleCheckBox.setToolTipText(Help.HELP_SKIP_IDLE);
		final GridBagConstraints gbc_keepIdleCheckBox = new GridBagConstraints();
		gbc_keepIdleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_keepIdleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_keepIdleCheckBox.gridx = 1;
		gbc_keepIdleCheckBox.gridy = 15;
		add(skipIdleCheckBox, gbc_keepIdleCheckBox);
		
		final JLabel lblFlashbackColor = new JLabel("Flashback Color");
		final GridBagConstraints gbc_lblFlashbackColor = new GridBagConstraints();
		gbc_lblFlashbackColor.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlashbackColor.gridx = 0;
		gbc_lblFlashbackColor.gridy = 16;
		add(lblFlashbackColor, gbc_lblFlashbackColor);
		
		flashbackColorSelector = new ColorSelector();
		flashbackColorSelector.setToolTipText(Help.HELP_FLASHBACK_COLOR);
		final GridBagConstraints gbc_flashbackColorSelector = new GridBagConstraints();
		gbc_flashbackColorSelector.insets = new Insets(0, 0, 5, 0);
		gbc_flashbackColorSelector.fill = GridBagConstraints.BOTH;
		gbc_flashbackColorSelector.gridx = 1;
		gbc_flashbackColorSelector.gridy = 16;
		add(flashbackColorSelector, gbc_flashbackColorSelector);
		
		final JLabel lblFlashbackDuration = new JLabel("Flashback Duration");
		final GridBagConstraints gbc_lblFlashbackDuration = new GridBagConstraints();
		gbc_lblFlashbackDuration.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackDuration.insets = new Insets(0, 0, 0, 5);
		gbc_lblFlashbackDuration.gridx = 0;
		gbc_lblFlashbackDuration.gridy = 17;
		add(lblFlashbackDuration, gbc_lblFlashbackDuration);
		
		flashbackDurationSpinner = new JSpinner();
		flashbackDurationSpinner.setToolTipText(Help.HELP_FLASHBACK_DURATION);
		flashbackDurationSpinner.setModel(new DurationSpinnerModel());
		flashbackDurationSpinner.setEditor(new DurationEditor(flashbackDurationSpinner));
		final GridBagConstraints gbc_flashbackDurationSpinner = new GridBagConstraints();
		gbc_flashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_flashbackDurationSpinner.gridx = 1;
		gbc_flashbackDurationSpinner.gridy = 17;
		add(flashbackDurationSpinner, gbc_flashbackDurationSpinner);
		
		outputFileSelector.addPropertyChangeListener("filename", propertyChangeListener);
		widthSpinner.addChangeListener(changeListener);
		heightSpinner.addChangeListener(changeListener);
		zoomSpinner.addChangeListener(changeListener);
		marginSpinner.addChangeListener(changeListener);
		speedupSpinner.addChangeListener(changeListener);
		totalTimeSpinner.addChangeListener(changeListener);
		markerSizeSpinner.addChangeListener(changeListener);
		waypointSizeSpinner.addChangeListener(changeListener);
		tailDurationSpinner.addChangeListener(changeListener);
		fpsSpinner.addChangeListener(changeListener);
//		tmsUrlTemplateComboBox.addChangeListener(listener);
		backgroundMapVisibilitySlider.addChangeListener(changeListener);
		fontSizeSpinner.addChangeListener(changeListener);
		skipIdleCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				configurationChanged();
			}
		});
		flashbackColorSelector.addPropertyChangeListener("color", propertyChangeListener);
		flashbackDurationSpinner.addChangeListener(changeListener);
	}
	
	private List<MapTemplate> readMaps() {
		final SAXParserFactory factory = SAXParserFactory.newInstance();
		final SAXParser saxParser;
		try {
			saxParser = factory.newSAXParser();
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException(e);
		} catch (final SAXException e) {
			throw new RuntimeException(e);
		}
		
		final List<MapTemplate> labeledItems = new ArrayList<MapTemplate>();

		final InputStream is = MainFrame.class.getResourceAsStream("maps.xml");
		
		try {
			try {
				saxParser.parse(is, new DefaultHandler() {
					StringBuilder sb = new StringBuilder();
					String name;
					String url;
					String attributionText;

					@Override
					public void endElement(final String uri, final String localName, final String qName) throws SAXException {
						if ("name".equals(qName)) {
							name = sb.toString().trim();
						} else if ("url".equals(qName)) {
							url = sb.toString().trim();
						} else if ("attribution-text".equals(qName)) {
							attributionText = sb.toString().trim();
						} else if ("entry".equals(qName)) {
							labeledItems.add(new MapTemplate(name, url, attributionText));
						}
						sb.setLength(0);
					}
	
					@Override
					public void characters(final char[] ch, final int start, final int length) throws SAXException {
						sb.append(ch, start, length);
					}
				});
			} catch (final SAXException e) {
				throw new RuntimeException(e);
			} finally {
				is.close();
			}
		} catch (final IOException e) {
			throw new RuntimeException(e);
		}
		
		Collections.sort(labeledItems, new Comparator<MapTemplate>() {
			@Override
			public int compare(final MapTemplate o1, final MapTemplate o2) {
				return o1.toString().compareTo(o2.toString());
			}
		});
		
		return labeledItems;
	}
	
	
	public void setConfiguration(final Configuration c) {
		heightSpinner.setValue(c.getHeight());
		widthSpinner.setValue(c.getWidth());
		marginSpinner.setValue(c.getMargin());
		zoomSpinner.setValue(c.getZoom());
		speedupSpinner.setValue(c.getSpeedup());
		tailDurationSpinner.setValue(c.getTailDuration());
		fpsSpinner.setValue(c.getFps());
		totalTimeSpinner.setValue(c.getTotalTime());
		backgroundMapVisibilitySlider.setValue((int) (c.getBackgroundMapVisibility() * 100));

		final String tmsUrlTemplate = c.getTmsUrlTemplate();
		found: {
			for (final MapTemplate mapTemplate : mapTamplateList) {
				if (mapTemplate.getUrl().equals(tmsUrlTemplate)) {
					tmsUrlTemplateComboBox.setSelectedItem(mapTemplate);
					break found;
				}
			}
			tmsUrlTemplateComboBox.setSelectedItem(tmsUrlTemplate);
		}
		
		attributionTextArea.setText(c.getAttribution());
		skipIdleCheckBox.setSelected(c.isSkipIdle());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		outputFileSelector.setFilename(c.getOutput().toString());
		fontSizeSpinner.setValue(c.getFontSize());
		markerSizeSpinner.setValue(c.getMarkerSize());
		waypointSizeSpinner.setValue(c.getWaypointSize());
		flashbackColorSelector.setColor(c.getFlashbackColor());
		flashbackDurationSpinner.setValue(c.getFlashbackDuration());
	}
	

	public void buildConfiguration(final Configuration.Builder b) {
		b.height((Integer) heightSpinner.getValue());
		b.width((Integer) widthSpinner.getValue());
		b.margin((Integer) marginSpinner.getValue());
		b.zoom((Integer) zoomSpinner.getValue());
		b.speedup((Double) speedupSpinner.getValue());
		final Long td = (Long) tailDurationSpinner.getValue();
		b.tailDuration(td == null ? 0l : td.longValue());
		b.fps((Double) fpsSpinner.getValue());
		b.totalTime((Long) totalTimeSpinner.getValue());
		b.backgroundMapVisibility(backgroundMapVisibilitySlider.getValue() / 100f);
		final Object tmsItem = tmsUrlTemplateComboBox.getSelectedItem();
		final String tmsUrlTemplate = tmsItem instanceof MapTemplate ? ((MapTemplate) tmsItem).getUrl() : (String) tmsItem;
		b.tmsUrlTemplate(tmsUrlTemplate == null || tmsUrlTemplate.isEmpty() ? null : tmsUrlTemplate);
		b.skipIdle(skipIdleCheckBox.isSelected());
		b.flashbackColor(flashbackColorSelector.getColor());
		b.flashbackDuration((Long) flashbackDurationSpinner.getValue());
		b.output(new File(outputFileSelector.getFilename()));
		b.fontSize((Integer) fontSizeSpinner.getValue());
		b.markerSize((Double) markerSizeSpinner.getValue());
		b.waypointSize((Double) waypointSizeSpinner.getValue());
		
		b.attribution(attributionTextArea.getText().replace("%MAP_ATTRIBUTION%",
				tmsItem instanceof MapTemplate ? ((MapTemplate) tmsItem).getAttributionText() : "").trim());
	}

	protected abstract void configurationChanged();

}
