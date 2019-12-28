package sk.freemap.gpxAnimator.ui;

import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Option;

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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JFileChooser.FILES_ONLY;

abstract class GeneralSettingsPanel extends JPanel {

    private static final long serialVersionUID = -2024548578211891192L;

    private final transient JSpinner heightSpinner;
    private final transient FileSelector outputFileSelector;
    private final transient JSpinner widthSpinner;
    private final transient JSpinner zoomSpinner;
    private final transient JSpinner marginSpinner;
    private final transient JSpinner speedupSpinner;
    private final transient JSpinner markerSizeSpinner;
    private final transient JSpinner waypointSizeSpinner;
    private final transient ColorSelector tailColorSelector;
    private final transient JSpinner tailDurationSpinner;
    private final transient JSpinner fpsSpinner;
    private final transient JComboBox<MapTemplate> tmsUrlTemplateComboBox;
    private final transient JSlider backgroundMapVisibilitySlider;
    private final transient JSpinner fontSizeSpinner;
    private final transient JCheckBox skipIdleCheckBox;
    private final transient ColorSelector flashbackColorSelector;
    private final transient JSpinner flashbackDurationSpinner;
    private final transient JSpinner keepLastFrameSpinner;
    private final transient JSpinner totalTimeSpinner;
    private final transient FileSelector photosDirectorySelector;
    private final transient JSpinner photoTimeSpinner;
    private final transient JTextArea attributionTextArea;
    private final transient List<MapTemplate> mapTemplateList;
    private final transient JSpinner maxLatSpinner;
    private final transient JSpinner minLonSpinner;
    private final transient JSpinner maxLonSpinner;
    private final transient JSpinner minLatSpinner;

    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    GeneralSettingsPanel() {
        mapTemplateList = readMaps();

        setBorder(new EmptyBorder(5, 5, 5, 5));
        final GridBagLayout gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths = new int[]{91, 100, 0, 0};
        gridBagLayout.rowHeights = new int[]{14, 20, 20, 20, 14, 20, 20, 20, 20, 20, 20, 20, 20, 50, 45, 20, 21, 23, 20, 0};
        gridBagLayout.columnWeights = new double[]{0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final JLabel lblOutput = new JLabel("Output");
        final GridBagConstraints gbcLabelOutput = new GridBagConstraints();
        gbcLabelOutput.anchor = GridBagConstraints.EAST;
        gbcLabelOutput.insets = new Insets(0, 0, 5, 5);
        gbcLabelOutput.gridx = 0;
        gbcLabelOutput.gridy = 0;
        add(lblOutput, gbcLabelOutput);

        outputFileSelector = new FileSelector(FILES_ONLY) {
            private static final long serialVersionUID = 7372002778976603239L;

            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("JPEG Image Frames", "jpg"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("PNG Image Frames", "png"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("H.264 Encoded Video Files (*.mp4, *.mov, *.mkv)", "mp4", "mov", "mkv"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("MPEG-1 Encoded Video Files (*.mpg)", "mpg"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("MPEG-4 Encoded Video Files (*.avi)", "avi"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("MS MPEG-4 Encoded Video Files (*.wmv, *.asf)", "wmv", "asf"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("Theora Encoded Video Files (*.ogv)", "ogv"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("FLV Encoded Video Files (*.flv)", "flv"));
                outputFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter("RV10 Encoded Video Files (*.rm)", "rm"));
                return Type.SAVE;
            }

            @Override
            protected String transformFilename(final String filename) {
                if ((filename.toLowerCase(Locale.getDefault()).endsWith(".png") || filename.toLowerCase(Locale.getDefault()).endsWith(".jpg"))
                        && String.format(filename, 100).equals(String.format(filename, 200))) {
                    final int n = filename.lastIndexOf('.');
                    return filename.substring(0, n) + "%08d" + filename.substring(n);
                } else {
                    return filename;
                }
            }
        };

        outputFileSelector.setToolTipText(Option.OUTPUT.getHelp());
        final GridBagConstraints gbcOutputFileSelector = new GridBagConstraints();
        gbcOutputFileSelector.fill = GridBagConstraints.BOTH;
        gbcOutputFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcOutputFileSelector.gridx = 1;
        gbcOutputFileSelector.gridy = 0;
        add(outputFileSelector, gbcOutputFileSelector);

        final PropertyChangeListener propertyChangeListener = evt -> configurationChanged();
        outputFileSelector.addPropertyChangeListener("filename", propertyChangeListener);

        final JLabel lblWidth = new JLabel("Width");
        final GridBagConstraints gbcLabelWidth = new GridBagConstraints();
        gbcLabelWidth.anchor = GridBagConstraints.EAST;
        gbcLabelWidth.insets = new Insets(0, 0, 5, 5);
        gbcLabelWidth.gridx = 0;
        gbcLabelWidth.gridy = 1;
        add(lblWidth, gbcLabelWidth);

        widthSpinner = new JSpinner();
        widthSpinner.setToolTipText(Option.WIDTH.getHelp());
        widthSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
        final GridBagConstraints gbcWidthSpinner = new GridBagConstraints();
        gbcWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthSpinner.insets = new Insets(0, 0, 5, 0);
        gbcWidthSpinner.gridx = 1;
        gbcWidthSpinner.gridy = 1;
        add(widthSpinner, gbcWidthSpinner);
        final ChangeListener changeListener = e -> configurationChanged();
        widthSpinner.addChangeListener(changeListener);

        final JLabel lblHeight = new JLabel("Height");
        final GridBagConstraints gbcLabelHeight = new GridBagConstraints();
        gbcLabelHeight.anchor = GridBagConstraints.EAST;
        gbcLabelHeight.insets = new Insets(0, 0, 5, 5);
        gbcLabelHeight.gridx = 0;
        gbcLabelHeight.gridy = 2;
        add(lblHeight, gbcLabelHeight);

        heightSpinner = new JSpinner();
        heightSpinner.setToolTipText(Option.HEIGHT.getHelp());
        heightSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner, Integer.class));
        final GridBagConstraints gbcHeightSpinner = new GridBagConstraints();
        gbcHeightSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcHeightSpinner.insets = new Insets(0, 0, 5, 0);
        gbcHeightSpinner.gridx = 1;
        gbcHeightSpinner.gridy = 2;
        add(heightSpinner, gbcHeightSpinner);
        heightSpinner.addChangeListener(changeListener);

        final JLabel lblZoom = new JLabel("Zoom");
        final GridBagConstraints gbcLabelZoom = new GridBagConstraints();
        gbcLabelZoom.anchor = GridBagConstraints.EAST;
        gbcLabelZoom.insets = new Insets(0, 0, 5, 5);
        gbcLabelZoom.gridx = 0;
        gbcLabelZoom.gridy = 3;
        add(lblZoom, gbcLabelZoom);

        zoomSpinner = new JSpinner();
        zoomSpinner.setToolTipText(Option.ZOOM.getHelp());
        zoomSpinner.setModel(new EmptyNullSpinnerModel(1, 0, 18, 1));
        zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));
        final GridBagConstraints gbcZoomSpinner = new GridBagConstraints();
        gbcZoomSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcZoomSpinner.insets = new Insets(0, 0, 5, 0);
        gbcZoomSpinner.gridx = 1;
        gbcZoomSpinner.gridy = 3;
        add(zoomSpinner, gbcZoomSpinner);
        zoomSpinner.addChangeListener(changeListener);

        final JLabel lblBoundingBox = new JLabel("Bounding Box");
        final GridBagConstraints gbcLabelBoundingBox = new GridBagConstraints();
        gbcLabelBoundingBox.anchor = GridBagConstraints.EAST;
        gbcLabelBoundingBox.insets = new Insets(0, 0, 5, 5);
        gbcLabelBoundingBox.gridx = 0;
        gbcLabelBoundingBox.gridy = 4;
        add(lblBoundingBox, gbcLabelBoundingBox);

        final JPanel panel = new JPanel();
        final GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcPanel.insets = new Insets(0, 0, 5, 0);
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 4;
        add(panel, gbcPanel);
        final GridBagLayout gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[]{0, 40, 40, 40, 0};
        gblPanel.rowHeights = new int[]{20, 0, 0, 0};
        gblPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gblPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gblPanel);

        final JLabel lblMaxLat = new JLabel("Max Lat");
        final GridBagConstraints gbcLabelMaxLat = new GridBagConstraints();
        gbcLabelMaxLat.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLat.anchor = GridBagConstraints.EAST;
        gbcLabelMaxLat.gridx = 1;
        gbcLabelMaxLat.gridy = 0;
        panel.add(lblMaxLat, gbcLabelMaxLat);

        final JLabel lblMinLon = new JLabel("Min Lon");
        final GridBagConstraints gbcLabelMinLon = new GridBagConstraints();
        gbcLabelMinLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMinLon.anchor = GridBagConstraints.EAST;
        gbcLabelMinLon.gridx = 0;
        gbcLabelMinLon.gridy = 1;
        panel.add(lblMinLon, gbcLabelMinLon);

        minLonSpinner = new JSpinner();
        minLonSpinner.setToolTipText(Option.MIN_LON.getHelp());
        minLonSpinner.setEditor(new EmptyZeroNumberEditor(minLonSpinner, Double.class));
        minLonSpinner.setModel(new EmptyNullSpinnerModel(null, -180.0, 180.0, 0.1, false));
        final GridBagConstraints gbcMinLonSpinner = new GridBagConstraints();
        gbcMinLonSpinner.insets = new Insets(0, 0, 5, 5);
        gbcMinLonSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMinLonSpinner.gridx = 1;
        gbcMinLonSpinner.gridy = 1;
        panel.add(minLonSpinner, gbcMinLonSpinner);
        minLonSpinner.addChangeListener(changeListener);

        maxLatSpinner = new JSpinner();
        maxLatSpinner.setToolTipText(Option.MAX_LAT.getHelp());
        maxLatSpinner.setEditor(new EmptyZeroNumberEditor(maxLatSpinner, Double.class));
        maxLatSpinner.setModel(new EmptyNullSpinnerModel(null, -90.0, 90.0, 0.1, false));
        final GridBagConstraints gbcMaxLatSpinner = new GridBagConstraints();
        gbcMaxLatSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMaxLatSpinner.anchor = GridBagConstraints.NORTH;
        gbcMaxLatSpinner.insets = new Insets(0, 0, 5, 5);
        gbcMaxLatSpinner.gridx = 2;
        gbcMaxLatSpinner.gridy = 0;
        panel.add(maxLatSpinner, gbcMaxLatSpinner);
        maxLatSpinner.addChangeListener(changeListener);

        final JLabel lblMaxLon = new JLabel("Max Lon");
        final GridBagConstraints gbcLabelMaxLon = new GridBagConstraints();
        gbcLabelMaxLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLon.anchor = GridBagConstraints.EAST;
        gbcLabelMaxLon.gridx = 2;
        gbcLabelMaxLon.gridy = 1;
        panel.add(lblMaxLon, gbcLabelMaxLon);

        final JLabel lblMinLat = new JLabel("Min Lat");
        final GridBagConstraints gbcLabelMinLat = new GridBagConstraints();
        gbcLabelMinLat.insets = new Insets(0, 0, 0, 5);
        gbcLabelMinLat.anchor = GridBagConstraints.EAST;
        gbcLabelMinLat.gridx = 1;
        gbcLabelMinLat.gridy = 2;
        panel.add(lblMinLat, gbcLabelMinLat);

        minLatSpinner = new JSpinner();
        minLatSpinner.setToolTipText(Option.MIN_LAT.getHelp());
        minLatSpinner.setEditor(new EmptyZeroNumberEditor(minLatSpinner, Double.class));
        minLatSpinner.setModel(new EmptyNullSpinnerModel(null, -90.0, 90.0, 0.1, false));
        final GridBagConstraints gbcMinLatSpinner = new GridBagConstraints();
        gbcMinLatSpinner.insets = new Insets(0, 0, 0, 5);
        gbcMinLatSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMinLatSpinner.gridx = 2;
        gbcMinLatSpinner.gridy = 2;
        panel.add(minLatSpinner, gbcMinLatSpinner);
        minLatSpinner.addChangeListener(changeListener);

        maxLonSpinner = new JSpinner();
        maxLonSpinner.setToolTipText(Option.MAX_LON.getHelp());
        maxLonSpinner.setEditor(new EmptyZeroNumberEditor(maxLonSpinner, Double.class));
        maxLonSpinner.setModel(new EmptyNullSpinnerModel(null, -180.0, 180.0, 0.1, false));
        final GridBagConstraints gbcMaxLonSpinner = new GridBagConstraints();
        gbcMaxLonSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMaxLonSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMaxLonSpinner.gridx = 3;
        gbcMaxLonSpinner.gridy = 1;
        panel.add(maxLonSpinner, gbcMaxLonSpinner);
        maxLonSpinner.addChangeListener(changeListener);

        final JLabel lblMargin = new JLabel("Margin");
        final GridBagConstraints gbcLabelMargin = new GridBagConstraints();
        gbcLabelMargin.anchor = GridBagConstraints.EAST;
        gbcLabelMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelMargin.gridx = 0;
        gbcLabelMargin.gridy = 5;
        add(lblMargin, gbcLabelMargin);

        marginSpinner = new JSpinner();
        marginSpinner.setToolTipText(Option.MARGIN.getHelp());
        marginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final GridBagConstraints gbcMarginSpinner = new GridBagConstraints();
        gbcMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarginSpinner.gridx = 1;
        gbcMarginSpinner.gridy = 5;
        add(marginSpinner, gbcMarginSpinner);
        marginSpinner.addChangeListener(changeListener);

        final JLabel lblSpeedup = new JLabel("Speedup");
        final GridBagConstraints gbcLabelSpeedup = new GridBagConstraints();
        gbcLabelSpeedup.anchor = GridBagConstraints.EAST;
        gbcLabelSpeedup.insets = new Insets(0, 0, 5, 5);
        gbcLabelSpeedup.gridx = 0;
        gbcLabelSpeedup.gridy = 6;
        add(lblSpeedup, gbcLabelSpeedup);

        speedupSpinner = new JSpinner();
        speedupSpinner.setToolTipText(Option.SPEEDUP.getHelp());
        speedupSpinner.setModel(new EmptyNullSpinnerModel((double) 0, (double) 0, null, 1d));
        speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
        final GridBagConstraints gbcSpeedupSpinner = new GridBagConstraints();
        gbcSpeedupSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcSpeedupSpinner.insets = new Insets(0, 0, 5, 0);
        gbcSpeedupSpinner.gridx = 1;
        gbcSpeedupSpinner.gridy = 6;
        add(speedupSpinner, gbcSpeedupSpinner);

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
        final GridBagConstraints gbcLabelTotalTime = new GridBagConstraints();
        gbcLabelTotalTime.anchor = GridBagConstraints.EAST;
        gbcLabelTotalTime.insets = new Insets(0, 0, 5, 5);
        gbcLabelTotalTime.gridx = 0;
        gbcLabelTotalTime.gridy = 7;
        add(lblTotalTime, gbcLabelTotalTime);

        totalTimeSpinner = new JSpinner();
        totalTimeSpinner.setToolTipText(Option.TOTAL_TIME.getHelp());
        totalTimeSpinner.setModel(new DurationSpinnerModel());
        totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
        final GridBagConstraints gbcTotalTimeSpinner = new GridBagConstraints();
        gbcTotalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTotalTimeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTotalTimeSpinner.gridx = 1;
        gbcTotalTimeSpinner.gridy = 7;
        add(totalTimeSpinner, gbcTotalTimeSpinner);

        totalTimeSpinner.addChangeListener(e -> {
            final boolean enabled = totalTimeSpinner.getValue() == null;
            if (!enabled) {
                speedupSpinner.setValue(null);
            }
            speedupSpinner.setEnabled(enabled);
        });
        totalTimeSpinner.addChangeListener(changeListener);

        final JLabel lblMarkerSize = new JLabel("Marker Size");
        final GridBagConstraints gbcLabelMarkerSize = new GridBagConstraints();
        gbcLabelMarkerSize.anchor = GridBagConstraints.EAST;
        gbcLabelMarkerSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelMarkerSize.gridx = 0;
        gbcLabelMarkerSize.gridy = 8;
        add(lblMarkerSize, gbcLabelMarkerSize);

        markerSizeSpinner = new JSpinner();
        markerSizeSpinner.setToolTipText(Option.MARKER_SIZE.getHelp());
        markerSizeSpinner.setEditor(new EmptyZeroNumberEditor(markerSizeSpinner, Double.class));
        markerSizeSpinner.setModel(new EmptyNullSpinnerModel(6.0, 0.0, null, 1.0));
        final GridBagConstraints gbcMarkerSizeSpinner = new GridBagConstraints();
        gbcMarkerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarkerSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarkerSizeSpinner.gridx = 1;
        gbcMarkerSizeSpinner.gridy = 8;
        add(markerSizeSpinner, gbcMarkerSizeSpinner);
        markerSizeSpinner.addChangeListener(changeListener);

        final JLabel lblWaypointSize = new JLabel("Waypoint Size");
        final GridBagConstraints gbcLabelWaypointSize = new GridBagConstraints();
        gbcLabelWaypointSize.anchor = GridBagConstraints.EAST;
        gbcLabelWaypointSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelWaypointSize.gridx = 0;
        gbcLabelWaypointSize.gridy = 9;
        add(lblWaypointSize, gbcLabelWaypointSize);

        waypointSizeSpinner = new JSpinner();
        waypointSizeSpinner.setToolTipText(Option.WAYPOINT_SIZE.getHelp());
        waypointSizeSpinner.setEditor(new EmptyZeroNumberEditor(waypointSizeSpinner, Double.class));
        waypointSizeSpinner.setModel(new EmptyNullSpinnerModel(1.0, 0.0, null, 1.0));
        final GridBagConstraints gbcWaypointSizeSpinner = new GridBagConstraints();
        gbcWaypointSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcWaypointSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcWaypointSizeSpinner.gridx = 1;
        gbcWaypointSizeSpinner.gridy = 9;
        add(waypointSizeSpinner, gbcWaypointSizeSpinner);
        waypointSizeSpinner.addChangeListener(changeListener);

        final JLabel lblTailColor = new JLabel("Tail Color");
        final GridBagConstraints gbcLabelTailColor = new GridBagConstraints();
        gbcLabelTailColor.anchor = GridBagConstraints.EAST;
        gbcLabelTailColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailColor.gridx = 0;
        gbcLabelTailColor.gridy = 10;
        add(lblTailColor, gbcLabelTailColor);

        tailColorSelector = new ColorSelector();
        tailColorSelector.setToolTipText(Option.TAIL_COLOR.getHelp());
        final GridBagConstraints gbcTailColorSelector = new GridBagConstraints();
        gbcTailColorSelector.fill = GridBagConstraints.BOTH;
        gbcTailColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcTailColorSelector.gridx = 1;
        gbcTailColorSelector.gridy = 10;
        add(tailColorSelector, gbcTailColorSelector);
        tailColorSelector.addPropertyChangeListener("tail-color", propertyChangeListener);

        final JLabel lblTailDuration = new JLabel("Tail Duration");
        final GridBagConstraints gbcLabelTailDuration = new GridBagConstraints();
        gbcLabelTailDuration.anchor = GridBagConstraints.EAST;
        gbcLabelTailDuration.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailDuration.gridx = 0;
        gbcLabelTailDuration.gridy = 11;
        add(lblTailDuration, gbcLabelTailDuration);

        tailDurationSpinner = new JSpinner();
        tailDurationSpinner.setToolTipText(Option.TAIL_DURATION.getHelp());
        tailDurationSpinner.setModel(new DurationSpinnerModel());
        tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
        final GridBagConstraints gbcTailDurationSpinner = new GridBagConstraints();
        gbcTailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTailDurationSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTailDurationSpinner.gridx = 1;
        gbcTailDurationSpinner.gridy = 11;
        add(tailDurationSpinner, gbcTailDurationSpinner);
        tailDurationSpinner.addChangeListener(changeListener);

        final JLabel lblFps = new JLabel("FPS");
        final GridBagConstraints gbcLabelFps = new GridBagConstraints();
        gbcLabelFps.anchor = GridBagConstraints.EAST;
        gbcLabelFps.insets = new Insets(0, 0, 5, 5);
        gbcLabelFps.gridx = 0;
        gbcLabelFps.gridy = 12;
        add(lblFps, gbcLabelFps);

        fpsSpinner = new JSpinner();
        fpsSpinner.setToolTipText(Option.FPS.getHelp());
        fpsSpinner.setModel(new SpinnerNumberModel(0.1, 0.1, null, 1d));
        final GridBagConstraints gbcFpsSpinner = new GridBagConstraints();
        gbcFpsSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFpsSpinner.insets = new Insets(0, 0, 5, 0);
        gbcFpsSpinner.gridx = 1;
        gbcFpsSpinner.gridy = 12;
        add(fpsSpinner, gbcFpsSpinner);
        fpsSpinner.addChangeListener(changeListener);

        final JLabel lblTmsUrlTemplate = new JLabel("Background Map");
        final GridBagConstraints gbcLabelTmsUrlTemplate = new GridBagConstraints();
        gbcLabelTmsUrlTemplate.anchor = GridBagConstraints.EAST;
        gbcLabelTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
        gbcLabelTmsUrlTemplate.gridx = 0;
        gbcLabelTmsUrlTemplate.gridy = 13;
        add(lblTmsUrlTemplate, gbcLabelTmsUrlTemplate);

        tmsUrlTemplateComboBox = new JComboBox<>();
        tmsUrlTemplateComboBox.setToolTipText(Option.TMS_URL_TEMPLATE.getHelp());
        tmsUrlTemplateComboBox.setEditable(true);
        tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<>(mapTemplateList.toArray(new MapTemplate[0])));
        final GridBagConstraints gbcTmsUrlTemplateComboBox = new GridBagConstraints();
        gbcTmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcTmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
        gbcTmsUrlTemplateComboBox.gridx = 1;
        gbcTmsUrlTemplateComboBox.gridy = 13;
        add(tmsUrlTemplateComboBox, gbcTmsUrlTemplateComboBox);
        tmsUrlTemplateComboBox.setPreferredSize(new Dimension(10, tmsUrlTemplateComboBox.getPreferredSize().height));

        final JLabel lblAttribution = new JLabel("Attribution");
        final GridBagConstraints gbcLabelAttribution = new GridBagConstraints();
        gbcLabelAttribution.anchor = GridBagConstraints.EAST;
        gbcLabelAttribution.insets = new Insets(0, 0, 5, 5);
        gbcLabelAttribution.gridx = 0;
        gbcLabelAttribution.gridy = 14;
        add(lblAttribution, gbcLabelAttribution);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(3, 50));
        final GridBagConstraints gbcScrollPane = new GridBagConstraints();
        gbcScrollPane.fill = GridBagConstraints.BOTH;
        gbcScrollPane.insets = new Insets(0, 0, 5, 0);
        gbcScrollPane.gridx = 1;
        gbcScrollPane.gridy = 14;
        add(scrollPane, gbcScrollPane);

        attributionTextArea = new JTextArea();
        attributionTextArea.setToolTipText(Option.ATTRIBUTION.getHelp());
        scrollPane.setViewportView(attributionTextArea);

        final JLabel lblVisibility = new JLabel("Map Visibility");
        final GridBagConstraints gbcLabelVisibility = new GridBagConstraints();
        gbcLabelVisibility.anchor = GridBagConstraints.EAST;
        gbcLabelVisibility.insets = new Insets(0, 0, 5, 5);
        gbcLabelVisibility.gridx = 0;
        gbcLabelVisibility.gridy = 15;
        add(lblVisibility, gbcLabelVisibility);

        backgroundMapVisibilitySlider = new JSlider();
        backgroundMapVisibilitySlider.setMinorTickSpacing(5);
        backgroundMapVisibilitySlider.setPaintTicks(true);
        backgroundMapVisibilitySlider.setMajorTickSpacing(10);
        backgroundMapVisibilitySlider.setPaintLabels(true);
        backgroundMapVisibilitySlider.setToolTipText(Option.BACKGROUND_MAP_VISIBILITY.getHelp());
        final GridBagConstraints gbcBackgroundMapVisibilitySlider = new GridBagConstraints();
        gbcBackgroundMapVisibilitySlider.fill = GridBagConstraints.HORIZONTAL;
        gbcBackgroundMapVisibilitySlider.insets = new Insets(0, 0, 5, 0);
        gbcBackgroundMapVisibilitySlider.gridx = 1;
        gbcBackgroundMapVisibilitySlider.gridy = 15;
        add(backgroundMapVisibilitySlider, gbcBackgroundMapVisibilitySlider);
        // tmsUrlTemplateComboBox.addChangeListener(listener);
        backgroundMapVisibilitySlider.addChangeListener(changeListener);

        final JLabel lblFontSize = new JLabel("Font Size");
        final GridBagConstraints gbcLabelFontSize = new GridBagConstraints();
        gbcLabelFontSize.anchor = GridBagConstraints.EAST;
        gbcLabelFontSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelFontSize.gridx = 0;
        gbcLabelFontSize.gridy = 16;
        add(lblFontSize, gbcLabelFontSize);

        fontSizeSpinner = new JSpinner();
        fontSizeSpinner.setToolTipText(Option.FONT_SIZE.getHelp());
        fontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
        final GridBagConstraints gbcFontSizeSpinner = new GridBagConstraints();
        gbcFontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFontSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcFontSizeSpinner.gridx = 1;
        gbcFontSizeSpinner.gridy = 16;
        add(fontSizeSpinner, gbcFontSizeSpinner);
        fontSizeSpinner.addChangeListener(changeListener);

        final JLabel lblSkipIdle = new JLabel("Skip Idle");
        final GridBagConstraints gbcLabelSkipIdle = new GridBagConstraints();
        gbcLabelSkipIdle.anchor = GridBagConstraints.EAST;
        gbcLabelSkipIdle.insets = new Insets(0, 0, 5, 5);
        gbcLabelSkipIdle.gridx = 0;
        gbcLabelSkipIdle.gridy = 17;
        add(lblSkipIdle, gbcLabelSkipIdle);

        skipIdleCheckBox = new JCheckBox("");
        skipIdleCheckBox.setToolTipText(Option.SKIP_IDLE.getHelp());
        final GridBagConstraints gbcSkipIdleCheckBox = new GridBagConstraints();
        gbcSkipIdleCheckBox.anchor = GridBagConstraints.WEST;
        gbcSkipIdleCheckBox.insets = new Insets(0, 0, 5, 0);
        gbcSkipIdleCheckBox.gridx = 1;
        gbcSkipIdleCheckBox.gridy = 17;
        add(skipIdleCheckBox, gbcSkipIdleCheckBox);
        skipIdleCheckBox.addItemListener(e -> configurationChanged());

        final JLabel lblFlashbackColor = new JLabel("Flashback Color");
        final GridBagConstraints gbcLabelFlashbackColor = new GridBagConstraints();
        gbcLabelFlashbackColor.anchor = GridBagConstraints.EAST;
        gbcLabelFlashbackColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelFlashbackColor.gridx = 0;
        gbcLabelFlashbackColor.gridy = 18;
        add(lblFlashbackColor, gbcLabelFlashbackColor);

        flashbackColorSelector = new ColorSelector();
        flashbackColorSelector.setToolTipText(Option.FLASHBACK_COLOR.getHelp());
        final GridBagConstraints gbcFlashbackColorSelector = new GridBagConstraints();
        gbcFlashbackColorSelector.fill = GridBagConstraints.BOTH;
        gbcFlashbackColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcFlashbackColorSelector.gridx = 1;
        gbcFlashbackColorSelector.gridy = 18;
        add(flashbackColorSelector, gbcFlashbackColorSelector);
        flashbackColorSelector.addPropertyChangeListener("color", propertyChangeListener);

        final JLabel lblFlashbackDuration = new JLabel("Flashback Duration");
        final GridBagConstraints gbcLabelFlashbackDuration = new GridBagConstraints();
        gbcLabelFlashbackDuration.anchor = GridBagConstraints.EAST;
        gbcLabelFlashbackDuration.insets = new Insets(0, 0, 0, 5);
        gbcLabelFlashbackDuration.gridx = 0;
        gbcLabelFlashbackDuration.gridy = 19;
        add(lblFlashbackDuration, gbcLabelFlashbackDuration);

        flashbackDurationSpinner = new JSpinner();
        flashbackDurationSpinner.setToolTipText(Option.FLASHBACK_DURATION.getHelp());
        flashbackDurationSpinner.setModel(new DurationSpinnerModel());
        flashbackDurationSpinner.setEditor(new DurationEditor(flashbackDurationSpinner));
        final GridBagConstraints gbcFlashbackDurationSpinner = new GridBagConstraints();
        gbcFlashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFlashbackDurationSpinner.gridx = 1;
        gbcFlashbackDurationSpinner.gridy = 19;
        add(flashbackDurationSpinner, gbcFlashbackDurationSpinner);
        flashbackDurationSpinner.addChangeListener(changeListener);

        final JLabel lblKeepLastFrame = new JLabel("Keep Last Frame");
        final GridBagConstraints gbcLabelKeepLastFrame = new GridBagConstraints();
        gbcLabelKeepLastFrame.anchor = GridBagConstraints.EAST;
        gbcLabelKeepLastFrame.insets = new Insets(0, 0, 0, 5);
        gbcLabelKeepLastFrame.gridx = 0;
        gbcLabelKeepLastFrame.gridy = 20;
        add(lblKeepLastFrame, gbcLabelKeepLastFrame);

        keepLastFrameSpinner = new JSpinner();
        keepLastFrameSpinner.setToolTipText(Option.KEEP_LAST_FRAME.getHelp());
        keepLastFrameSpinner.setModel(new DurationSpinnerModel());
        keepLastFrameSpinner.setEditor(new DurationEditor(keepLastFrameSpinner));
        final GridBagConstraints gbcKeepLastFrameSpinner = new GridBagConstraints();
        gbcKeepLastFrameSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcKeepLastFrameSpinner.gridx = 1;
        gbcKeepLastFrameSpinner.gridy = 20;
        add(keepLastFrameSpinner, gbcKeepLastFrameSpinner);
        keepLastFrameSpinner.addChangeListener(changeListener);

        final JLabel lblPhotosDirectorySelector = new JLabel("Photo Directory");
        final GridBagConstraints gbcLabelPhotosDirectorySelector = new GridBagConstraints();
        gbcLabelPhotosDirectorySelector.anchor = GridBagConstraints.EAST;
        gbcLabelPhotosDirectorySelector.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotosDirectorySelector.gridx = 0;
        gbcLabelPhotosDirectorySelector.gridy = 21;
        add(lblPhotosDirectorySelector, gbcLabelPhotosDirectorySelector);

        photosDirectorySelector = new FileSelector(DIRECTORIES_ONLY) {
            private static final long serialVersionUID = 7372002778976603240L;

            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };

        photosDirectorySelector.setToolTipText(Option.PHOTO_DIR.getHelp());
        final GridBagConstraints gbcPhotosDirectorySelector = new GridBagConstraints();
        gbcPhotosDirectorySelector.fill = GridBagConstraints.BOTH;
        gbcPhotosDirectorySelector.insets = new Insets(0, 0, 5, 0);
        gbcPhotosDirectorySelector.gridx = 1;
        gbcPhotosDirectorySelector.gridy = 21;
        add(photosDirectorySelector, gbcPhotosDirectorySelector);

        photosDirectorySelector.addPropertyChangeListener("filename", propertyChangeListener);

        final JLabel lblPhotoTime = new JLabel("Show Photos For");
        final GridBagConstraints gbcLabelPhotoTime = new GridBagConstraints();
        gbcLabelPhotoTime.anchor = GridBagConstraints.EAST;
        gbcLabelPhotoTime.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotoTime.gridx = 0;
        gbcLabelPhotoTime.gridy = 22;
        add(lblPhotoTime, gbcLabelPhotoTime);

        photoTimeSpinner = new JSpinner();
        photoTimeSpinner.setToolTipText(Option.PHOTO_TIME.getHelp());
        photoTimeSpinner.setModel(new DurationSpinnerModel());
        photoTimeSpinner.setEditor(new DurationEditor(photoTimeSpinner));
        final GridBagConstraints gbcPhotoTimeSpinner = new GridBagConstraints();
        gbcPhotoTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcPhotoTimeSpinner.gridx = 1;
        gbcPhotoTimeSpinner.gridy = 22;
        add(photoTimeSpinner, gbcPhotoTimeSpinner);
        photoTimeSpinner.addChangeListener(changeListener);
    }

    private List<MapTemplate> readMaps() {
        final SAXParserFactory factory = SAXParserFactory.newInstance();
        final SAXParser saxParser;
        try {
            saxParser = factory.newSAXParser();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new RuntimeException(e);
        }

        final List<MapTemplate> labeledItems = new ArrayList<>();

        try {
            try (InputStream is = getClass().getResourceAsStream("/maps.xml")) {
                saxParser.parse(is, new DefaultHandler() {
                    private final StringBuilder sb = new StringBuilder();
                    private String name;
                    private String url;
                    private String attributionText;

                    @Override
                    public void endElement(final String uri, final String localName, final String qName) {
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
                    public void characters(final char[] ch, final int start, final int length) {
                        sb.append(ch, start, length);
                    }
                });
            } catch (final SAXException e) {
                throw new RuntimeException(e);
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }

        labeledItems.sort(Comparator.comparing(MapTemplate::toString));

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
        photoTimeSpinner.setValue(c.getPhotoTime());

        final String tmsUrlTemplate = c.getTmsUrlTemplate();
        found:
        {
            for (final MapTemplate mapTemplate : mapTemplateList) {
                if (mapTemplate.getUrl().equals(tmsUrlTemplate)) {
                    tmsUrlTemplateComboBox.setSelectedItem(mapTemplate);
                    break found;
                }
            }
            tmsUrlTemplateComboBox.setSelectedItem(tmsUrlTemplate);
        }

        attributionTextArea.setText(c.getAttribution());
        skipIdleCheckBox.setSelected(c.isSkipIdle());
        if (c.getTailColor() != null) { // old saved files may not include this setting
            tailColorSelector.setColor(c.getTailColor());
        }
        outputFileSelector.setFilename(c.getOutput().toString());
        fontSizeSpinner.setValue(c.getFontSize());
        markerSizeSpinner.setValue(c.getMarkerSize());
        waypointSizeSpinner.setValue(c.getWaypointSize());
        flashbackColorSelector.setColor(c.getFlashbackColor());
        flashbackDurationSpinner.setValue(c.getFlashbackDuration());
    }


    public void buildConfiguration(final Configuration.Builder builder, final boolean replacePlaceholders) {
        final Long td = (Long) tailDurationSpinner.getValue();
        final Object tmsItem = tmsUrlTemplateComboBox.getSelectedItem();
        final String tmsUrlTemplate = tmsItem instanceof MapTemplate ? ((MapTemplate) tmsItem).getUrl() : (String) tmsItem;
        final String attribution = replacePlaceholders
                ? attributionTextArea.getText().replace("%MAP_ATTRIBUTION%",
                    tmsItem instanceof MapTemplate && ((MapTemplate) tmsItem).getAttributionText() != null
                            ? ((MapTemplate) tmsItem).getAttributionText() : "").trim()
                : attributionTextArea.getText().trim();

        builder.height((Integer) heightSpinner.getValue())
                .width((Integer) widthSpinner.getValue())
                .margin((Integer) marginSpinner.getValue())
                .zoom((Integer) zoomSpinner.getValue())
                .minLat((Double) minLatSpinner.getValue())
                .maxLat((Double) maxLatSpinner.getValue())
                .minLon((Double) minLonSpinner.getValue())
                .maxLon((Double) maxLonSpinner.getValue())
                .speedup((Double) speedupSpinner.getValue())
                .tailColor(tailColorSelector.getColor())
                .tailDuration(td == null ? 0L : td)
                .fps((Double) fpsSpinner.getValue())
                .totalTime((Long) totalTimeSpinner.getValue())
                .keepLastFrame((Long) keepLastFrameSpinner.getValue())
                .backgroundMapVisibility(backgroundMapVisibilitySlider.getValue() / 100f)
                .tmsUrlTemplate(tmsUrlTemplate == null || tmsUrlTemplate.isEmpty() ? null : tmsUrlTemplate) // NOPMD -- null = not set
                .skipIdle(skipIdleCheckBox.isSelected())
                .flashbackColor(flashbackColorSelector.getColor())
                .flashbackDuration((Long) flashbackDurationSpinner.getValue())
                .output(new File(outputFileSelector.getFilename()))
                .fontSize((Integer) fontSizeSpinner.getValue())
                .markerSize((Double) markerSizeSpinner.getValue())
                .waypointSize((Double) waypointSizeSpinner.getValue())
                .photoDirectory(photosDirectorySelector.getFilename())
                .photoTime((Long) photoTimeSpinner.getValue())
                .attribution(attribution);
    }

    protected abstract void configurationChanged();

}
