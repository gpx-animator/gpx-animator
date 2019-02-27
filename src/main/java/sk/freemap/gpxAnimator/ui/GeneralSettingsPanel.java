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
import sk.freemap.gpxAnimator.Option;

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
	private final JSpinner keepLastFrameSpinner;
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
	private JSpinner maxLatSpinner;
	private JSpinner minLonSpinner;
	private JSpinner maxLonSpinner;
	private JSpinner minLatSpinner;

	public GeneralSettingsPanel() {
		mapTamplateList = readMaps();

		setBorder(new EmptyBorder(5, 5, 5, 5));
		final GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{91, 100, 0, 0};
		gridBagLayout.rowHeights = new int[]{14, 20, 20, 20, 14, 20, 20, 20, 20, 20, 20, 20, 20, 50, 45, 20, 21, 23, 20, 20, 0};
		gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
		gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
		setLayout(gridBagLayout);

						final JLabel lblOutput = new JLabel("Output");
						final GridBagConstraints gbc_lblOutput = new GridBagConstraints();
						gbc_lblOutput.anchor = GridBagConstraints.EAST;
						gbc_lblOutput.insets = new Insets(0, 0, 5, 5);
						gbc_lblOutput.gridx = 0;
						gbc_lblOutput.gridy = 0;
						add(lblOutput, gbc_lblOutput);

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

				outputFileSelector.setToolTipText(Option.OUTPUT.getHelp());
				final GridBagConstraints gbc_outputFileSelector = new GridBagConstraints();
				gbc_outputFileSelector.fill = GridBagConstraints.BOTH;
				gbc_outputFileSelector.insets = new Insets(0, 0, 5, 0);
				gbc_outputFileSelector.gridx = 1;
				gbc_outputFileSelector.gridy = 0;
				add(outputFileSelector, gbc_outputFileSelector);

				outputFileSelector.addPropertyChangeListener("filename", propertyChangeListener);

		final JLabel lblWidth = new JLabel("Width");
		final GridBagConstraints gbc_lblWidth = new GridBagConstraints();
		gbc_lblWidth.anchor = GridBagConstraints.EAST;
		gbc_lblWidth.insets = new Insets(0, 0, 5, 5);
		gbc_lblWidth.gridx = 0;
		gbc_lblWidth.gridy = 1;
		add(lblWidth, gbc_lblWidth);

		widthSpinner = new JSpinner();
		widthSpinner.setToolTipText(Option.WIDTH.getHelp());
		widthSpinner.setModel(new EmptyNullSpinnerModel(Integer.valueOf(1), Integer.valueOf(0), null, Integer.valueOf(10)));
		widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
		final GridBagConstraints gbc_widthSpinner = new GridBagConstraints();
		gbc_widthSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_widthSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_widthSpinner.gridx = 1;
		gbc_widthSpinner.gridy = 1;
		add(widthSpinner, gbc_widthSpinner);
		widthSpinner.addChangeListener(changeListener);

		final JLabel lblHeight = new JLabel("Height");
		final GridBagConstraints gbc_lblHeight = new GridBagConstraints();
		gbc_lblHeight.anchor = GridBagConstraints.EAST;
		gbc_lblHeight.insets = new Insets(0, 0, 5, 5);
		gbc_lblHeight.gridx = 0;
		gbc_lblHeight.gridy = 2;
		add(lblHeight, gbc_lblHeight);

		heightSpinner = new JSpinner();
		heightSpinner.setToolTipText(Option.HEIGHT.getHelp());
		heightSpinner.setModel(new EmptyNullSpinnerModel(Integer.valueOf(1), Integer.valueOf(0), null, Integer.valueOf(10)));
		heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner, Integer.class));
		final GridBagConstraints gbc_heightSpinner = new GridBagConstraints();
		gbc_heightSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_heightSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_heightSpinner.gridx = 1;
		gbc_heightSpinner.gridy = 2;
		add(heightSpinner, gbc_heightSpinner);
		heightSpinner.addChangeListener(changeListener);

		final JLabel lblZoom = new JLabel("Zoom");
		final GridBagConstraints gbc_lblZoom = new GridBagConstraints();
		gbc_lblZoom.anchor = GridBagConstraints.EAST;
		gbc_lblZoom.insets = new Insets(0, 0, 5, 5);
		gbc_lblZoom.gridx = 0;
		gbc_lblZoom.gridy = 3;
		add(lblZoom, gbc_lblZoom);

		zoomSpinner = new JSpinner();
		zoomSpinner.setToolTipText(Option.ZOOM.getHelp());
		zoomSpinner.setModel(new EmptyNullSpinnerModel(Integer.valueOf(1), Integer.valueOf(0), Integer.valueOf(18), Integer.valueOf(1)));
		zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));
		final GridBagConstraints gbc_zoomSpinner = new GridBagConstraints();
		gbc_zoomSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_zoomSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_zoomSpinner.gridx = 1;
		gbc_zoomSpinner.gridy = 3;
		add(zoomSpinner, gbc_zoomSpinner);
		zoomSpinner.addChangeListener(changeListener);

		final JLabel lblBoundingBox = new JLabel("Bounding Box");
		final GridBagConstraints gbc_lblBoundingBox = new GridBagConstraints();
		gbc_lblBoundingBox.anchor = GridBagConstraints.EAST;
		gbc_lblBoundingBox.insets = new Insets(0, 0, 5, 5);
		gbc_lblBoundingBox.gridx = 0;
		gbc_lblBoundingBox.gridy = 4;
		add(lblBoundingBox, gbc_lblBoundingBox);

		final JPanel panel = new JPanel();
		final GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
		gbc_panel.insets = new Insets(0, 0, 5, 0);
		gbc_panel.gridx = 1;
		gbc_panel.gridy = 4;
		add(panel, gbc_panel);
		final GridBagLayout gbl_panel = new GridBagLayout();
		gbl_panel.columnWidths = new int[]{0, 40, 40, 40, 0};
		gbl_panel.rowHeights = new int[]{20, 0, 0, 0};
		gbl_panel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
		gbl_panel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
		panel.setLayout(gbl_panel);

		final JLabel lblMaxLat = new JLabel("Max Lat");
		final GridBagConstraints gbc_lblMaxLat = new GridBagConstraints();
		gbc_lblMaxLat.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxLat.anchor = GridBagConstraints.EAST;
		gbc_lblMaxLat.gridx = 1;
		gbc_lblMaxLat.gridy = 0;
		panel.add(lblMaxLat, gbc_lblMaxLat);

		final JLabel lblMinLon = new JLabel("Min Lon");
		final GridBagConstraints gbc_lblMinLon = new GridBagConstraints();
		gbc_lblMinLon.insets = new Insets(0, 0, 5, 5);
		gbc_lblMinLon.anchor = GridBagConstraints.EAST;
		gbc_lblMinLon.gridx = 0;
		gbc_lblMinLon.gridy = 1;
		panel.add(lblMinLon, gbc_lblMinLon);

		minLonSpinner = new JSpinner();
		minLonSpinner.setToolTipText(Option.MIN_LON.getHelp());
		minLonSpinner.setEditor(new EmptyZeroNumberEditor(minLonSpinner, Double.class));
		minLonSpinner.setModel(new EmptyNullSpinnerModel(null, Double.valueOf(0.0), Double.valueOf(180.0), Double.valueOf(0.1), false));
		final GridBagConstraints gbc_minLonSpinner = new GridBagConstraints();
		gbc_minLonSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_minLonSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLonSpinner.gridx = 1;
		gbc_minLonSpinner.gridy = 1;
		panel.add(minLonSpinner, gbc_minLonSpinner);
		minLonSpinner.addChangeListener(changeListener);

		maxLatSpinner = new JSpinner();
		maxLatSpinner.setToolTipText(Option.MAX_LAT.getHelp());
		maxLatSpinner.setEditor(new EmptyZeroNumberEditor(maxLatSpinner, Double.class));
		maxLatSpinner.setModel(new EmptyNullSpinnerModel(null, Double.valueOf(-90.0), Double.valueOf(90.0), Double.valueOf(0.1), false));
		final GridBagConstraints gbc_maxLatSpinner = new GridBagConstraints();
		gbc_maxLatSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLatSpinner.anchor = GridBagConstraints.NORTH;
		gbc_maxLatSpinner.insets = new Insets(0, 0, 5, 5);
		gbc_maxLatSpinner.gridx = 2;
		gbc_maxLatSpinner.gridy = 0;
		panel.add(maxLatSpinner, gbc_maxLatSpinner);
		maxLatSpinner.addChangeListener(changeListener);

		final JLabel lblMaxLon = new JLabel("Max Lon");
		final GridBagConstraints gbc_lblMaxLon = new GridBagConstraints();
		gbc_lblMaxLon.insets = new Insets(0, 0, 5, 5);
		gbc_lblMaxLon.anchor = GridBagConstraints.EAST;
		gbc_lblMaxLon.gridx = 2;
		gbc_lblMaxLon.gridy = 1;
		panel.add(lblMaxLon, gbc_lblMaxLon);

		final JLabel lblMinLat = new JLabel("Min Lat");
		final GridBagConstraints gbc_lblMinLat = new GridBagConstraints();
		gbc_lblMinLat.insets = new Insets(0, 0, 0, 5);
		gbc_lblMinLat.anchor = GridBagConstraints.EAST;
		gbc_lblMinLat.gridx = 1;
		gbc_lblMinLat.gridy = 2;
		panel.add(lblMinLat, gbc_lblMinLat);

		minLatSpinner = new JSpinner();
		minLatSpinner.setToolTipText(Option.MIN_LAT.getHelp());
		minLatSpinner.setEditor(new EmptyZeroNumberEditor(minLatSpinner, Double.class));
		minLatSpinner.setModel(new EmptyNullSpinnerModel(null, Double.valueOf(-90.0), Double.valueOf(90.0), Double.valueOf(0.1), false));
		final GridBagConstraints gbc_minLatSpinner = new GridBagConstraints();
		gbc_minLatSpinner.insets = new Insets(0, 0, 0, 5);
		gbc_minLatSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_minLatSpinner.gridx = 2;
		gbc_minLatSpinner.gridy = 2;
		panel.add(minLatSpinner, gbc_minLatSpinner);
		minLatSpinner.addChangeListener(changeListener);

		maxLonSpinner = new JSpinner();
		maxLonSpinner.setToolTipText(Option.MAX_LON.getHelp());
		maxLonSpinner.setEditor(new EmptyZeroNumberEditor(maxLonSpinner, Double.class));
		maxLonSpinner.setModel(new EmptyNullSpinnerModel(null, Double.valueOf(0.0), Double.valueOf(180.0), Double.valueOf(0.1), false));
		final GridBagConstraints gbc_maxLonSpinner = new GridBagConstraints();
		gbc_maxLonSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_maxLonSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_maxLonSpinner.gridx = 3;
		gbc_maxLonSpinner.gridy = 1;
		panel.add(maxLonSpinner, gbc_maxLonSpinner);
		maxLonSpinner.addChangeListener(changeListener);

		final JLabel lblMargin = new JLabel("Margin");
		final GridBagConstraints gbc_lblMargin = new GridBagConstraints();
		gbc_lblMargin.anchor = GridBagConstraints.EAST;
		gbc_lblMargin.insets = new Insets(0, 0, 5, 5);
		gbc_lblMargin.gridx = 0;
		gbc_lblMargin.gridy = 5;
		add(lblMargin, gbc_lblMargin);

		marginSpinner = new JSpinner();
		marginSpinner.setToolTipText(Option.MARGIN.getHelp());
		marginSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(0), Integer.valueOf(0), null, Integer.valueOf(1)));
		final GridBagConstraints gbc_marginSpinner = new GridBagConstraints();
		gbc_marginSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_marginSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_marginSpinner.gridx = 1;
		gbc_marginSpinner.gridy = 5;
		add(marginSpinner, gbc_marginSpinner);
		marginSpinner.addChangeListener(changeListener);

		final JLabel lblSpeedup = new JLabel("Speedup");
		final GridBagConstraints gbc_lblSpeedup = new GridBagConstraints();
		gbc_lblSpeedup.anchor = GridBagConstraints.EAST;
		gbc_lblSpeedup.insets = new Insets(0, 0, 5, 5);
		gbc_lblSpeedup.gridx = 0;
		gbc_lblSpeedup.gridy = 6;
		add(lblSpeedup, gbc_lblSpeedup);

		speedupSpinner = new JSpinner();
		speedupSpinner.setToolTipText(Option.SPEEDUP.getHelp());
		speedupSpinner.setModel(new EmptyNullSpinnerModel(Double.valueOf(0), Double.valueOf(0), null, Double.valueOf(1)));
		speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
		final GridBagConstraints gbc_speedupSpinner = new GridBagConstraints();
		gbc_speedupSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_speedupSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_speedupSpinner.gridx = 1;
		gbc_speedupSpinner.gridy = 6;
		add(speedupSpinner, gbc_speedupSpinner);

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
		speedupSpinner.addChangeListener(changeListener);

		final JLabel lblTotalTime = new JLabel("Total Time");
		final GridBagConstraints gbc_lblTotalTime = new GridBagConstraints();
		gbc_lblTotalTime.anchor = GridBagConstraints.EAST;
		gbc_lblTotalTime.insets = new Insets(0, 0, 5, 5);
		gbc_lblTotalTime.gridx = 0;
		gbc_lblTotalTime.gridy = 7;
		add(lblTotalTime, gbc_lblTotalTime);

		totalTimeSpinner = new JSpinner();
		totalTimeSpinner.setToolTipText(Option.TOTAL_TIME.getHelp());
		totalTimeSpinner.setModel(new DurationSpinnerModel());
		totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
		final GridBagConstraints gbc_totalTimeSpinner = new GridBagConstraints();
		gbc_totalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_totalTimeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_totalTimeSpinner.gridx = 1;
		gbc_totalTimeSpinner.gridy = 7;
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
		totalTimeSpinner.addChangeListener(changeListener);

		final JLabel lblMarkerSize = new JLabel("Marker Size");
		final GridBagConstraints gbc_lblMarkerSize = new GridBagConstraints();
		gbc_lblMarkerSize.anchor = GridBagConstraints.EAST;
		gbc_lblMarkerSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblMarkerSize.gridx = 0;
		gbc_lblMarkerSize.gridy = 8;
		add(lblMarkerSize, gbc_lblMarkerSize);

		markerSizeSpinner = new JSpinner();
		markerSizeSpinner.setToolTipText(Option.MARKER_SIZE.getHelp());
		markerSizeSpinner.setEditor(new EmptyZeroNumberEditor(markerSizeSpinner, Double.class));
		markerSizeSpinner.setModel(new EmptyNullSpinnerModel(Double.valueOf(6.0), Double.valueOf(0.0), null, Double.valueOf(1.0)));
		final GridBagConstraints gbc_markerSizeSpinner = new GridBagConstraints();
		gbc_markerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_markerSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_markerSizeSpinner.gridx = 1;
		gbc_markerSizeSpinner.gridy = 8;
		add(markerSizeSpinner, gbc_markerSizeSpinner);
		markerSizeSpinner.addChangeListener(changeListener);

		final JLabel lblWaypointSize = new JLabel("Waypoint Size");
		final GridBagConstraints gbc_lblWaypointSize = new GridBagConstraints();
		gbc_lblWaypointSize.anchor = GridBagConstraints.EAST;
		gbc_lblWaypointSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblWaypointSize.gridx = 0;
		gbc_lblWaypointSize.gridy = 9;
		add(lblWaypointSize, gbc_lblWaypointSize);

		waypointSizeSpinner = new JSpinner();
		waypointSizeSpinner.setToolTipText(Option.WAYPOINT_SIZE.getHelp());
		waypointSizeSpinner.setEditor(new EmptyZeroNumberEditor(waypointSizeSpinner, Double.class));
		waypointSizeSpinner.setModel(new EmptyNullSpinnerModel(Double.valueOf(1.0), Double.valueOf(0.0), null, Double.valueOf(1.0)));
		final GridBagConstraints gbc_waypointSizeSpinner = new GridBagConstraints();
		gbc_waypointSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_waypointSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_waypointSizeSpinner.gridx = 1;
		gbc_waypointSizeSpinner.gridy = 9;
		add(waypointSizeSpinner, gbc_waypointSizeSpinner);
		waypointSizeSpinner.addChangeListener(changeListener);

		final JLabel lblTailDuration = new JLabel("Tail Duration");
		final GridBagConstraints gbc_lblTailDuration = new GridBagConstraints();
		gbc_lblTailDuration.anchor = GridBagConstraints.EAST;
		gbc_lblTailDuration.insets = new Insets(0, 0, 5, 5);
		gbc_lblTailDuration.gridx = 0;
		gbc_lblTailDuration.gridy = 10;
		add(lblTailDuration, gbc_lblTailDuration);

		tailDurationSpinner = new JSpinner();
		tailDurationSpinner.setToolTipText(Option.TAIL_DURATION.getHelp());
		tailDurationSpinner.setModel(new DurationSpinnerModel());
		tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
		final GridBagConstraints gbc_tailDurationSpinner = new GridBagConstraints();
		gbc_tailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_tailDurationSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_tailDurationSpinner.gridx = 1;
		gbc_tailDurationSpinner.gridy = 10;
		add(tailDurationSpinner, gbc_tailDurationSpinner);
		tailDurationSpinner.addChangeListener(changeListener);

		final JLabel lblFps = new JLabel("FPS");
		final GridBagConstraints gbc_lblFps = new GridBagConstraints();
		gbc_lblFps.anchor = GridBagConstraints.EAST;
		gbc_lblFps.insets = new Insets(0, 0, 5, 5);
		gbc_lblFps.gridx = 0;
		gbc_lblFps.gridy = 11;
		add(lblFps, gbc_lblFps);

		fpsSpinner = new JSpinner();
		fpsSpinner.setToolTipText(Option.FPS.getHelp());
		fpsSpinner.setModel(new SpinnerNumberModel(Double.valueOf(0.1), Double.valueOf(0.1), null, Double.valueOf(1)));
		final GridBagConstraints gbc_fpsSpinner = new GridBagConstraints();
		gbc_fpsSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fpsSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fpsSpinner.gridx = 1;
		gbc_fpsSpinner.gridy = 11;
		add(fpsSpinner, gbc_fpsSpinner);
		fpsSpinner.addChangeListener(changeListener);

		final JLabel lblTmsUrlTemplate = new JLabel("Background Map");
		final GridBagConstraints gbc_lblTmsUrlTemplate = new GridBagConstraints();
		gbc_lblTmsUrlTemplate.anchor = GridBagConstraints.EAST;
		gbc_lblTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
		gbc_lblTmsUrlTemplate.gridx = 0;
		gbc_lblTmsUrlTemplate.gridy = 12;
		add(lblTmsUrlTemplate, gbc_lblTmsUrlTemplate);

		tmsUrlTemplateComboBox = new JComboBox<MapTemplate>();
		tmsUrlTemplateComboBox.setToolTipText(Option.TMS_URL_TEMPLATE.getHelp());
		tmsUrlTemplateComboBox.setEditable(true);
		tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<MapTemplate>(mapTamplateList.toArray(new MapTemplate[mapTamplateList.size()])));
		final GridBagConstraints gbc_tmsUrlTemplateComboBox = new GridBagConstraints();
		gbc_tmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
		gbc_tmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
		gbc_tmsUrlTemplateComboBox.gridx = 1;
		gbc_tmsUrlTemplateComboBox.gridy = 12;
		add(tmsUrlTemplateComboBox, gbc_tmsUrlTemplateComboBox);
		tmsUrlTemplateComboBox.setPreferredSize(new Dimension(10, tmsUrlTemplateComboBox.getPreferredSize().height));

		final JLabel lblAttribution = new JLabel("Attribution");
		final GridBagConstraints gbc_lblAttribution = new GridBagConstraints();
		gbc_lblAttribution.anchor = GridBagConstraints.EAST;
		gbc_lblAttribution.insets = new Insets(0, 0, 5, 5);
		gbc_lblAttribution.gridx = 0;
		gbc_lblAttribution.gridy = 13;
		add(lblAttribution, gbc_lblAttribution);

		final JScrollPane scrollPane = new JScrollPane();
		scrollPane.setPreferredSize(new Dimension(3, 50));
		final GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.insets = new Insets(0, 0, 5, 0);
		gbc_scrollPane.gridx = 1;
		gbc_scrollPane.gridy = 13;
		add(scrollPane, gbc_scrollPane);

		attributionTextArea = new JTextArea();
		attributionTextArea.setToolTipText(Option.ATTRIBUTION.getHelp());
		scrollPane.setViewportView(attributionTextArea);

		final JLabel lblVisibility = new JLabel("Map Visibility");
		final GridBagConstraints gbc_lblVisibility = new GridBagConstraints();
		gbc_lblVisibility.anchor = GridBagConstraints.EAST;
		gbc_lblVisibility.insets = new Insets(0, 0, 5, 5);
		gbc_lblVisibility.gridx = 0;
		gbc_lblVisibility.gridy = 14;
		add(lblVisibility, gbc_lblVisibility);

		backgroundMapVisibilitySlider = new JSlider();
		backgroundMapVisibilitySlider.setMinorTickSpacing(5);
		backgroundMapVisibilitySlider.setPaintTicks(true);
		backgroundMapVisibilitySlider.setMajorTickSpacing(10);
		backgroundMapVisibilitySlider.setPaintLabels(true);
		backgroundMapVisibilitySlider.setToolTipText(Option.BACKGROUND_MAP_VISIBILITY.getHelp());
		final GridBagConstraints gbc_backgroundMapVisibilitySlider = new GridBagConstraints();
		gbc_backgroundMapVisibilitySlider.fill = GridBagConstraints.HORIZONTAL;
		gbc_backgroundMapVisibilitySlider.insets = new Insets(0, 0, 5, 0);
		gbc_backgroundMapVisibilitySlider.gridx = 1;
		gbc_backgroundMapVisibilitySlider.gridy = 14;
		add(backgroundMapVisibilitySlider, gbc_backgroundMapVisibilitySlider);
		//		tmsUrlTemplateComboBox.addChangeListener(listener);
				backgroundMapVisibilitySlider.addChangeListener(changeListener);

		final JLabel lblFontSize = new JLabel("Font Size");
		final GridBagConstraints gbc_lblFontSize = new GridBagConstraints();
		gbc_lblFontSize.anchor = GridBagConstraints.EAST;
		gbc_lblFontSize.insets = new Insets(0, 0, 5, 5);
		gbc_lblFontSize.gridx = 0;
		gbc_lblFontSize.gridy = 15;
		add(lblFontSize, gbc_lblFontSize);

		fontSizeSpinner = new JSpinner();
		fontSizeSpinner.setToolTipText(Option.FONT_SIZE.getHelp());
		fontSizeSpinner.setModel(new SpinnerNumberModel(Integer.valueOf(1), Integer.valueOf(1), null, Integer.valueOf(1)));
		final GridBagConstraints gbc_fontSizeSpinner = new GridBagConstraints();
		gbc_fontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_fontSizeSpinner.insets = new Insets(0, 0, 5, 0);
		gbc_fontSizeSpinner.gridx = 1;
		gbc_fontSizeSpinner.gridy = 15;
		add(fontSizeSpinner, gbc_fontSizeSpinner);
		fontSizeSpinner.addChangeListener(changeListener);

		final JLabel lblSkipIdle = new JLabel("Skip Idle");
		final GridBagConstraints gbc_lblSkipIdle = new GridBagConstraints();
		gbc_lblSkipIdle.anchor = GridBagConstraints.EAST;
		gbc_lblSkipIdle.insets = new Insets(0, 0, 5, 5);
		gbc_lblSkipIdle.gridx = 0;
		gbc_lblSkipIdle.gridy = 16;
		add(lblSkipIdle, gbc_lblSkipIdle);

		skipIdleCheckBox = new JCheckBox("");
		skipIdleCheckBox.setToolTipText(Option.SKIP_IDLE.getHelp());
		final GridBagConstraints gbc_skipIdleCheckBox = new GridBagConstraints();
		gbc_skipIdleCheckBox.anchor = GridBagConstraints.WEST;
		gbc_skipIdleCheckBox.insets = new Insets(0, 0, 5, 0);
		gbc_skipIdleCheckBox.gridx = 1;
		gbc_skipIdleCheckBox.gridy = 16;
		add(skipIdleCheckBox, gbc_skipIdleCheckBox);
		skipIdleCheckBox.addItemListener(new ItemListener() {
			@Override
			public void itemStateChanged(final ItemEvent e) {
				configurationChanged();
			}
		});

		final JLabel lblFlashbackColor = new JLabel("Flashback Color");
		final GridBagConstraints gbc_lblFlashbackColor = new GridBagConstraints();
		gbc_lblFlashbackColor.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackColor.insets = new Insets(0, 0, 5, 5);
		gbc_lblFlashbackColor.gridx = 0;
		gbc_lblFlashbackColor.gridy = 17;
		add(lblFlashbackColor, gbc_lblFlashbackColor);

		flashbackColorSelector = new ColorSelector();
		flashbackColorSelector.setToolTipText(Option.FLASHBACK_COLOR.getHelp());
		final GridBagConstraints gbc_flashbackColorSelector = new GridBagConstraints();
		gbc_flashbackColorSelector.fill = GridBagConstraints.BOTH;
		gbc_flashbackColorSelector.insets = new Insets(0, 0, 5, 0);
		gbc_flashbackColorSelector.gridx = 1;
		gbc_flashbackColorSelector.gridy = 17;
		add(flashbackColorSelector, gbc_flashbackColorSelector);
		flashbackColorSelector.addPropertyChangeListener("color", propertyChangeListener);

		final JLabel lblFlashbackDuration = new JLabel("Flashback Duration");
		final GridBagConstraints gbc_lblFlashbackDuration = new GridBagConstraints();
		gbc_lblFlashbackDuration.anchor = GridBagConstraints.EAST;
		gbc_lblFlashbackDuration.insets = new Insets(0, 0, 0, 5);
		gbc_lblFlashbackDuration.gridx = 0;
		gbc_lblFlashbackDuration.gridy = 18;
		add(lblFlashbackDuration, gbc_lblFlashbackDuration);

		flashbackDurationSpinner = new JSpinner();
		flashbackDurationSpinner.setToolTipText(Option.FLASHBACK_DURATION.getHelp());
		flashbackDurationSpinner.setModel(new DurationSpinnerModel());
		flashbackDurationSpinner.setEditor(new DurationEditor(flashbackDurationSpinner));
		final GridBagConstraints gbc_flashbackDurationSpinner = new GridBagConstraints();
		gbc_flashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_flashbackDurationSpinner.gridx = 1;
		gbc_flashbackDurationSpinner.gridy = 18;
		add(flashbackDurationSpinner, gbc_flashbackDurationSpinner);
		flashbackDurationSpinner.addChangeListener(changeListener);

		final JLabel lblKeepLastFrame = new JLabel("Keep Last Frame");
		final GridBagConstraints gbc_lblKeepLastFrame = new GridBagConstraints();
		gbc_lblKeepLastFrame.anchor = GridBagConstraints.EAST;
		gbc_lblKeepLastFrame.insets = new Insets(0, 0, 0, 5);
		gbc_lblKeepLastFrame.gridx = 0;
		gbc_lblKeepLastFrame.gridy = 19;
		add(lblKeepLastFrame, gbc_lblKeepLastFrame);

		keepLastFrameSpinner = new JSpinner();
		keepLastFrameSpinner.setToolTipText(Option.KEEP_LAST_FRAME.getHelp());
		keepLastFrameSpinner.setModel(new DurationSpinnerModel());
		keepLastFrameSpinner.setEditor(new DurationEditor(keepLastFrameSpinner));
		final GridBagConstraints gbc_keepLastFrameSpinner = new GridBagConstraints();
		gbc_keepLastFrameSpinner.fill = GridBagConstraints.HORIZONTAL;
		gbc_keepLastFrameSpinner.gridx = 1;
		gbc_keepLastFrameSpinner.gridy = 19;
		add(keepLastFrameSpinner, gbc_keepLastFrameSpinner);
		keepLastFrameSpinner.addChangeListener(changeListener);
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

		final InputStream is = getClass().getResourceAsStream("/maps.xml");

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
		minLatSpinner.setValue(c.getMinLat());
		maxLatSpinner.setValue(c.getMaxLat());
		minLonSpinner.setValue(c.getMinLon());
		maxLonSpinner.setValue(c.getMaxLon());
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
		b.minLat((Double) minLatSpinner.getValue());
		b.maxLat((Double) maxLatSpinner.getValue());
		b.minLon((Double) minLonSpinner.getValue());
		b.maxLon((Double) maxLonSpinner.getValue());
		b.speedup((Double) speedupSpinner.getValue());
		final Long td = (Long) tailDurationSpinner.getValue();
		b.tailDuration(td == null ? 0l : td.longValue());
		b.fps((Double) fpsSpinner.getValue());
		b.totalTime((Long) totalTimeSpinner.getValue());
		b.keepLastFrame((Long) keepLastFrameSpinner.getValue());
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
				tmsItem instanceof MapTemplate && ((MapTemplate) tmsItem).getAttributionText() != null
				? ((MapTemplate) tmsItem).getAttributionText() : "").trim());
	}

	protected abstract void configurationChanged();

}
