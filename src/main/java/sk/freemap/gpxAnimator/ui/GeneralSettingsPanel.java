package sk.freemap.gpxAnimator.ui;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NonNls;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import sk.freemap.gpxAnimator.Configuration;
import sk.freemap.gpxAnimator.Option;
import sk.freemap.gpxAnimator.Preferences;

import javax.swing.AbstractAction;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
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
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JFileChooser.FILES_ONLY;

abstract class GeneralSettingsPanel extends JPanel {

    private static final long serialVersionUID = -2024548578211891192L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

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
    private final transient ColorSelector backgroundColorSelector;
    private final transient ColorSelector flashbackColorSelector;
    private final transient JSpinner flashbackDurationSpinner;
    private final transient JSpinner keepLastFrameSpinner;
    private final transient JSpinner totalTimeSpinner;
    private final transient FileSelector logoFileSelector;
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

        final JLabel lblOutput = new JLabel(resourceBundle.getString("ui.panel.generalsettings.output.label"));
        final GridBagConstraints gbcLabelOutput = new GridBagConstraints();
        gbcLabelOutput.anchor = GridBagConstraints.LINE_END;
        gbcLabelOutput.insets = new Insets(0, 0, 5, 5);
        gbcLabelOutput.gridx = 0;
        gbcLabelOutput.gridy = 0;
        add(lblOutput, gbcLabelOutput);

        outputFileSelector = new FileSelector(FILES_ONLY) {
            private static final long serialVersionUID = 7372002778976603239L;

            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.jpeg"), "jpg")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.png"), "png")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.h264"), "mp4", "mov", "mkv")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.mpeg1"), "mpg")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.mpeg4"), "avi")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.msmpeg4"), "wmv", "asf")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.theora"), "ogv")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.flv"), "flv")); //NON-NLS
                outputFileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                        resourceBundle.getString("ui.panel.generalsettings.output.format.rv10"), "rm")); //NON-NLS
                return Type.SAVE;
            }

            @Override
            protected String transformFilename(final String filename) {
                if ((filename.toLowerCase(Locale.getDefault()).endsWith(".png") //NON-NLS
                        || filename.toLowerCase(Locale.getDefault()).endsWith(".jpg")) //NON-NLS
                        && Collator.getInstance().compare(String.format(filename, 100), String.format(filename, 200)) == 0) {
                    final int n = filename.lastIndexOf('.');
                    return String.format("%s%%08d%s", filename.substring(0, n), filename.substring(n)); //NON-NLS
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

        final JLabel lblWidthHeight = new JLabel(resourceBundle.getString("ui.panel.generalsettings.widthheight.label"));
        final GridBagConstraints gbcLabelWidthHeight = new GridBagConstraints();
        gbcLabelWidthHeight.anchor = GridBagConstraints.LINE_END;
        gbcLabelWidthHeight.insets = new Insets(0, 0, 5, 5);
        gbcLabelWidthHeight.gridx = 0;
        gbcLabelWidthHeight.gridy = 1;
        add(lblWidthHeight, gbcLabelWidthHeight);

        final JPanel widthHeightPanel = new JPanel();
        final GridBagConstraints gbcWidthHeightPanel = new GridBagConstraints();
        gbcWidthHeightPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthHeightPanel.insets = new Insets(0, 0, 5, 0);
        gbcWidthHeightPanel.gridx = 1;
        gbcWidthHeightPanel.gridy = 1;
        add(widthHeightPanel, gbcWidthHeightPanel);
        final GridBagLayout gblWidthHeightPanel = new GridBagLayout();
        gblWidthHeightPanel.columnWeights = new double[]{10.0, 10.0, 1.0, Double.MIN_VALUE};
        widthHeightPanel.setLayout(gblWidthHeightPanel);

        widthSpinner = new JSpinner();
        widthSpinner.setToolTipText(Option.WIDTH.getHelp());
        widthSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
        final GridBagConstraints gbcWidthSpinner = new GridBagConstraints();
        gbcWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthSpinner.insets = new Insets(0, 0, 5, 5);
        gbcWidthSpinner.gridx = 0;
        gbcWidthSpinner.gridy = 0;
        widthHeightPanel.add(widthSpinner, gbcWidthSpinner);
        final ChangeListener changeListener = e -> configurationChanged();
        widthSpinner.addChangeListener(changeListener);

        heightSpinner = new JSpinner();
        heightSpinner.setToolTipText(Option.HEIGHT.getHelp());
        heightSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        heightSpinner.setEditor(new EmptyZeroNumberEditor(heightSpinner, Integer.class));
        final GridBagConstraints gbcHeightSpinner = new GridBagConstraints();
        gbcHeightSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcHeightSpinner.insets = new Insets(0, 0, 5, 5);
        gbcHeightSpinner.gridx = 1;
        gbcHeightSpinner.gridy = 0;
        widthHeightPanel.add(heightSpinner, gbcHeightSpinner);
        heightSpinner.addChangeListener(changeListener);

        final Object[][] standardVideoSizes = {
                {"SD, PAL", 768, 576}, //NON-NLS
                {"HD-Ready", 1280, 720}, //NON-NLS
                {"Full-HD", 1920, 1080}, //NON-NLS
                {"WQUXGA", 3840, 2400}, //NON-NLS
                {"DCI 4K Flat/Masked", 3996, 2160}, //NON-NLS
                {"DCI 4K CinemaScope", 4096, 1716}, //NON-NLS
                {"4K2K, DCI 4K, Cinema 4K", 4096, 2160}, //NON-NLS
                {"4K UHD, 4K, QFHD, 2160p/i", 3840, 2160} //NON-NLS
        };
        final JPopupMenu widthHeightPopup = new JPopupMenu();
        for (final Object[] standardVideoSize : standardVideoSizes) {
            final String text = (String) standardVideoSize[0];
            final int width = (int) standardVideoSize[1];
            final int height = (int) standardVideoSize[2];
            final String name = String.format("%s (%d x %d)", text, width, height); //NON-NLS
            widthHeightPopup.add(new JMenuItem(new AbstractAction(name) {
                private static final long serialVersionUID = -1125796034755504311L;

                public void actionPerformed(final ActionEvent e) {
                    setVideoSize(width, height);
                }
            }));
        }
        final JButton widthHeightButton = new JButton(resourceBundle.getString("ui.panel.generalsettings.widthheight.button"));
        widthHeightButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                widthHeightPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        final GridBagConstraints gbcWidthHeightButton = new GridBagConstraints();
        gbcWidthHeightButton.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthHeightButton.insets = new Insets(0, 0, 5, 0);
        gbcWidthHeightButton.gridx = 2;
        gbcWidthHeightButton.gridy = 0;
        widthHeightPanel.add(widthHeightButton, gbcWidthHeightButton);

        final JLabel lblZoom = new JLabel(resourceBundle.getString("ui.panel.generalsettings.zoom.label"));
        final GridBagConstraints gbcLabelZoom = new GridBagConstraints();
        gbcLabelZoom.anchor = GridBagConstraints.LINE_END;
        gbcLabelZoom.insets = new Insets(0, 0, 5, 5);
        gbcLabelZoom.gridx = 0;
        gbcLabelZoom.gridy = 2;
        add(lblZoom, gbcLabelZoom);

        zoomSpinner = new JSpinner();
        zoomSpinner.setToolTipText(Option.ZOOM.getHelp());
        zoomSpinner.setModel(new EmptyNullSpinnerModel(1, 0, 18, 1));
        zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));
        final GridBagConstraints gbcZoomSpinner = new GridBagConstraints();
        gbcZoomSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcZoomSpinner.insets = new Insets(0, 0, 5, 0);
        gbcZoomSpinner.gridx = 1;
        gbcZoomSpinner.gridy = 2;
        add(zoomSpinner, gbcZoomSpinner);
        zoomSpinner.addChangeListener(changeListener);

        final JLabel lblBoundingBox = new JLabel(resourceBundle.getString("ui.panel.generalsettings.boundingbox.label"));
        final GridBagConstraints gbcLabelBoundingBox = new GridBagConstraints();
        gbcLabelBoundingBox.anchor = GridBagConstraints.LINE_END;
        gbcLabelBoundingBox.insets = new Insets(0, 0, 5, 5);
        gbcLabelBoundingBox.gridx = 0;
        gbcLabelBoundingBox.gridy = 3;
        add(lblBoundingBox, gbcLabelBoundingBox);

        final JPanel panel = new JPanel();
        final GridBagConstraints gbcPanel = new GridBagConstraints();
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcPanel.insets = new Insets(0, 0, 5, 0);
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 3;
        add(panel, gbcPanel);
        final GridBagLayout gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[]{0, 40, 40, 40, 0};
        gblPanel.rowHeights = new int[]{20, 0, 0, 0};
        gblPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gblPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        panel.setLayout(gblPanel);

        final JLabel lblMaxLat = new JLabel(resourceBundle.getString("ui.panel.generalsettings.latitude.max.label"));
        final GridBagConstraints gbcLabelMaxLat = new GridBagConstraints();
        gbcLabelMaxLat.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLat.anchor = GridBagConstraints.LINE_END;
        gbcLabelMaxLat.gridx = 1;
        gbcLabelMaxLat.gridy = 0;
        panel.add(lblMaxLat, gbcLabelMaxLat);

        final JLabel lblMinLon = new JLabel(resourceBundle.getString("ui.panel.generalsettings.longitude.min.label"));
        final GridBagConstraints gbcLabelMinLon = new GridBagConstraints();
        gbcLabelMinLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMinLon.anchor = GridBagConstraints.LINE_END;
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
        gbcMaxLatSpinner.anchor = GridBagConstraints.PAGE_START;
        gbcMaxLatSpinner.insets = new Insets(0, 0, 5, 5);
        gbcMaxLatSpinner.gridx = 2;
        gbcMaxLatSpinner.gridy = 0;
        panel.add(maxLatSpinner, gbcMaxLatSpinner);
        maxLatSpinner.addChangeListener(changeListener);

        final JLabel lblMaxLon = new JLabel(resourceBundle.getString("ui.panel.generalsettings.longitude.max.label"));
        final GridBagConstraints gbcLabelMaxLon = new GridBagConstraints();
        gbcLabelMaxLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLon.anchor = GridBagConstraints.LINE_END;
        gbcLabelMaxLon.gridx = 2;
        gbcLabelMaxLon.gridy = 1;
        panel.add(lblMaxLon, gbcLabelMaxLon);

        final JLabel lblMinLat = new JLabel(resourceBundle.getString("ui.panel.generalsettings.latitude.min.label"));
        final GridBagConstraints gbcLabelMinLat = new GridBagConstraints();
        gbcLabelMinLat.insets = new Insets(0, 0, 0, 5);
        gbcLabelMinLat.anchor = GridBagConstraints.LINE_END;
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

        final JLabel lblMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.margin.label"));
        final GridBagConstraints gbcLabelMargin = new GridBagConstraints();
        gbcLabelMargin.anchor = GridBagConstraints.LINE_END;
        gbcLabelMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelMargin.gridx = 0;
        gbcLabelMargin.gridy = 4;
        add(lblMargin, gbcLabelMargin);

        marginSpinner = new JSpinner();
        marginSpinner.setToolTipText(Option.MARGIN.getHelp());
        marginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final GridBagConstraints gbcMarginSpinner = new GridBagConstraints();
        gbcMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarginSpinner.gridx = 1;
        gbcMarginSpinner.gridy = 4;
        add(marginSpinner, gbcMarginSpinner);
        marginSpinner.addChangeListener(changeListener);

        final JLabel lblSpeedup = new JLabel(resourceBundle.getString("ui.panel.generalsettings.speedup.label"));
        final GridBagConstraints gbcLabelSpeedup = new GridBagConstraints();
        gbcLabelSpeedup.anchor = GridBagConstraints.LINE_END;
        gbcLabelSpeedup.insets = new Insets(0, 0, 5, 5);
        gbcLabelSpeedup.gridx = 0;
        gbcLabelSpeedup.gridy = 5;
        add(lblSpeedup, gbcLabelSpeedup);

        speedupSpinner = new JSpinner();
        speedupSpinner.setToolTipText(Option.SPEEDUP.getHelp());
        speedupSpinner.setModel(new EmptyNullSpinnerModel((double) 0, (double) 0, null, 1d));
        speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
        final GridBagConstraints gbcSpeedupSpinner = new GridBagConstraints();
        gbcSpeedupSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcSpeedupSpinner.insets = new Insets(0, 0, 5, 0);
        gbcSpeedupSpinner.gridx = 1;
        gbcSpeedupSpinner.gridy = 5;
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

        final JLabel lblTotalTime = new JLabel(resourceBundle.getString("ui.panel.generalsettings.totaltime.label"));
        final GridBagConstraints gbcLabelTotalTime = new GridBagConstraints();
        gbcLabelTotalTime.anchor = GridBagConstraints.LINE_END;
        gbcLabelTotalTime.insets = new Insets(0, 0, 5, 5);
        gbcLabelTotalTime.gridx = 0;
        gbcLabelTotalTime.gridy = 6;
        add(lblTotalTime, gbcLabelTotalTime);

        totalTimeSpinner = new JSpinner();
        totalTimeSpinner.setToolTipText(Option.TOTAL_TIME.getHelp());
        totalTimeSpinner.setModel(new DurationSpinnerModel());
        totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
        final GridBagConstraints gbcTotalTimeSpinner = new GridBagConstraints();
        gbcTotalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTotalTimeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTotalTimeSpinner.gridx = 1;
        gbcTotalTimeSpinner.gridy = 6;
        add(totalTimeSpinner, gbcTotalTimeSpinner);

        totalTimeSpinner.addChangeListener(e -> {
            final boolean enabled = totalTimeSpinner.getValue() == null;
            if (!enabled) {
                speedupSpinner.setValue(null);
            }
            speedupSpinner.setEnabled(enabled);
        });
        totalTimeSpinner.addChangeListener(changeListener);

        final JLabel lblMarkerSize = new JLabel(resourceBundle.getString("ui.panel.generalsettings.markersize.label"));
        final GridBagConstraints gbcLabelMarkerSize = new GridBagConstraints();
        gbcLabelMarkerSize.anchor = GridBagConstraints.LINE_END;
        gbcLabelMarkerSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelMarkerSize.gridx = 0;
        gbcLabelMarkerSize.gridy = 7;
        add(lblMarkerSize, gbcLabelMarkerSize);

        markerSizeSpinner = new JSpinner();
        markerSizeSpinner.setToolTipText(Option.MARKER_SIZE.getHelp());
        markerSizeSpinner.setEditor(new EmptyZeroNumberEditor(markerSizeSpinner, Double.class));
        markerSizeSpinner.setModel(new EmptyNullSpinnerModel(6.0, 0.0, null, 1.0));
        final GridBagConstraints gbcMarkerSizeSpinner = new GridBagConstraints();
        gbcMarkerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarkerSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarkerSizeSpinner.gridx = 1;
        gbcMarkerSizeSpinner.gridy = 7;
        add(markerSizeSpinner, gbcMarkerSizeSpinner);
        markerSizeSpinner.addChangeListener(changeListener);

        final JLabel lblWaypointSize = new JLabel(resourceBundle.getString("ui.panel.generalsettings.waypointsize.label"));
        final GridBagConstraints gbcLabelWaypointSize = new GridBagConstraints();
        gbcLabelWaypointSize.anchor = GridBagConstraints.LINE_END;
        gbcLabelWaypointSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelWaypointSize.gridx = 0;
        gbcLabelWaypointSize.gridy = 8;
        add(lblWaypointSize, gbcLabelWaypointSize);

        waypointSizeSpinner = new JSpinner();
        waypointSizeSpinner.setToolTipText(Option.WAYPOINT_SIZE.getHelp());
        waypointSizeSpinner.setEditor(new EmptyZeroNumberEditor(waypointSizeSpinner, Double.class));
        waypointSizeSpinner.setModel(new EmptyNullSpinnerModel(1.0, 0.0, null, 1.0));
        final GridBagConstraints gbcWaypointSizeSpinner = new GridBagConstraints();
        gbcWaypointSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcWaypointSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcWaypointSizeSpinner.gridx = 1;
        gbcWaypointSizeSpinner.gridy = 8;
        add(waypointSizeSpinner, gbcWaypointSizeSpinner);
        waypointSizeSpinner.addChangeListener(changeListener);

        final JLabel lblTailColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.tailcolor.label"));
        final GridBagConstraints gbcLabelTailColor = new GridBagConstraints();
        gbcLabelTailColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelTailColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailColor.gridx = 0;
        gbcLabelTailColor.gridy = 9;
        add(lblTailColor, gbcLabelTailColor);

        tailColorSelector = new ColorSelector();
        tailColorSelector.setToolTipText(Option.TAIL_COLOR.getHelp());
        final GridBagConstraints gbcTailColorSelector = new GridBagConstraints();
        gbcTailColorSelector.fill = GridBagConstraints.BOTH;
        gbcTailColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcTailColorSelector.gridx = 1;
        gbcTailColorSelector.gridy = 9;
        add(tailColorSelector, gbcTailColorSelector);
        tailColorSelector.addPropertyChangeListener("tail-color", propertyChangeListener);

        final JLabel lblTailDuration = new JLabel(resourceBundle.getString("ui.panel.generalsettings.tailduration.label"));
        final GridBagConstraints gbcLabelTailDuration = new GridBagConstraints();
        gbcLabelTailDuration.anchor = GridBagConstraints.LINE_END;
        gbcLabelTailDuration.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailDuration.gridx = 0;
        gbcLabelTailDuration.gridy = 10;
        add(lblTailDuration, gbcLabelTailDuration);

        tailDurationSpinner = new JSpinner();
        tailDurationSpinner.setToolTipText(Option.TAIL_DURATION.getHelp());
        tailDurationSpinner.setModel(new DurationSpinnerModel());
        tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
        final GridBagConstraints gbcTailDurationSpinner = new GridBagConstraints();
        gbcTailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTailDurationSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTailDurationSpinner.gridx = 1;
        gbcTailDurationSpinner.gridy = 10;
        add(tailDurationSpinner, gbcTailDurationSpinner);
        tailDurationSpinner.addChangeListener(changeListener);

        final JLabel lblFps = new JLabel(resourceBundle.getString("ui.panel.generalsettings.fps.label"));
        final GridBagConstraints gbcLabelFps = new GridBagConstraints();
        gbcLabelFps.anchor = GridBagConstraints.LINE_END;
        gbcLabelFps.insets = new Insets(0, 0, 5, 5);
        gbcLabelFps.gridx = 0;
        gbcLabelFps.gridy = 11;
        add(lblFps, gbcLabelFps);

        fpsSpinner = new JSpinner();
        fpsSpinner.setToolTipText(Option.FPS.getHelp());
        fpsSpinner.setModel(new SpinnerNumberModel(0.1, 0.1, null, 1d));
        final GridBagConstraints gbcFpsSpinner = new GridBagConstraints();
        gbcFpsSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFpsSpinner.insets = new Insets(0, 0, 5, 0);
        gbcFpsSpinner.gridx = 1;
        gbcFpsSpinner.gridy = 11;
        add(fpsSpinner, gbcFpsSpinner);
        fpsSpinner.addChangeListener(changeListener);

        final JLabel lblTmsUrlTemplate = new JLabel(resourceBundle.getString("ui.panel.generalsettings.map.label"));
        final GridBagConstraints gbcLabelTmsUrlTemplate = new GridBagConstraints();
        gbcLabelTmsUrlTemplate.anchor = GridBagConstraints.LINE_END;
        gbcLabelTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
        gbcLabelTmsUrlTemplate.gridx = 0;
        gbcLabelTmsUrlTemplate.gridy = 12;
        add(lblTmsUrlTemplate, gbcLabelTmsUrlTemplate);

        tmsUrlTemplateComboBox = new JComboBox<>();
        tmsUrlTemplateComboBox.setToolTipText(Option.TMS_URL_TEMPLATE.getHelp());
        tmsUrlTemplateComboBox.setEditable(true);
        tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<>(mapTemplateList.toArray(new MapTemplate[0])));
        final GridBagConstraints gbcTmsUrlTemplateComboBox = new GridBagConstraints();
        gbcTmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcTmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
        gbcTmsUrlTemplateComboBox.gridx = 1;
        gbcTmsUrlTemplateComboBox.gridy = 12;
        add(tmsUrlTemplateComboBox, gbcTmsUrlTemplateComboBox);
        tmsUrlTemplateComboBox.setPreferredSize(new Dimension(10, tmsUrlTemplateComboBox.getPreferredSize().height));

        final JLabel lblAttribution = new JLabel(resourceBundle.getString("ui.panel.generalsettings.attribution.label"));
        final GridBagConstraints gbcLabelAttribution = new GridBagConstraints();
        gbcLabelAttribution.anchor = GridBagConstraints.LINE_END;
        gbcLabelAttribution.insets = new Insets(0, 0, 5, 5);
        gbcLabelAttribution.gridx = 0;
        gbcLabelAttribution.gridy = 13;
        add(lblAttribution, gbcLabelAttribution);

        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(3, 50));
        final GridBagConstraints gbcScrollPane = new GridBagConstraints();
        gbcScrollPane.fill = GridBagConstraints.BOTH;
        gbcScrollPane.insets = new Insets(0, 0, 5, 0);
        gbcScrollPane.gridx = 1;
        gbcScrollPane.gridy = 13;
        add(scrollPane, gbcScrollPane);

        attributionTextArea = new JTextArea();
        attributionTextArea.setToolTipText(Option.ATTRIBUTION.getHelp());
        scrollPane.setViewportView(attributionTextArea);

        final JLabel lblVisibility = new JLabel(resourceBundle.getString("ui.panel.generalsettings.visibility.label"));
        final GridBagConstraints gbcLabelVisibility = new GridBagConstraints();
        gbcLabelVisibility.anchor = GridBagConstraints.LINE_END;
        gbcLabelVisibility.insets = new Insets(0, 0, 5, 5);
        gbcLabelVisibility.gridx = 0;
        gbcLabelVisibility.gridy = 14;
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
        gbcBackgroundMapVisibilitySlider.gridy = 14;
        add(backgroundMapVisibilitySlider, gbcBackgroundMapVisibilitySlider);
        // tmsUrlTemplateComboBox.addChangeListener(listener);
        backgroundMapVisibilitySlider.addChangeListener(changeListener);

        final JLabel lblFontSize = new JLabel(resourceBundle.getString("ui.panel.generalsettings.fontsize.label"));
        final GridBagConstraints gbcLabelFontSize = new GridBagConstraints();
        gbcLabelFontSize.anchor = GridBagConstraints.LINE_END;
        gbcLabelFontSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelFontSize.gridx = 0;
        gbcLabelFontSize.gridy = 15;
        add(lblFontSize, gbcLabelFontSize);

        fontSizeSpinner = new JSpinner();
        fontSizeSpinner.setToolTipText(Option.FONT_SIZE.getHelp());
        fontSizeSpinner.setModel(new SpinnerNumberModel(1, 1, null, 1));
        final GridBagConstraints gbcFontSizeSpinner = new GridBagConstraints();
        gbcFontSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFontSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcFontSizeSpinner.gridx = 1;
        gbcFontSizeSpinner.gridy = 15;
        add(fontSizeSpinner, gbcFontSizeSpinner);
        fontSizeSpinner.addChangeListener(changeListener);

        final JLabel lblSkipIdle = new JLabel(resourceBundle.getString("ui.panel.generalsettings.skipidle.label"));
        final GridBagConstraints gbcLabelSkipIdle = new GridBagConstraints();
        gbcLabelSkipIdle.anchor = GridBagConstraints.LINE_END;
        gbcLabelSkipIdle.insets = new Insets(0, 0, 5, 5);
        gbcLabelSkipIdle.gridx = 0;
        gbcLabelSkipIdle.gridy = 16;
        add(lblSkipIdle, gbcLabelSkipIdle);

        skipIdleCheckBox = new JCheckBox("");
        skipIdleCheckBox.setToolTipText(Option.SKIP_IDLE.getHelp());
        final GridBagConstraints gbcSkipIdleCheckBox = new GridBagConstraints();
        gbcSkipIdleCheckBox.anchor = GridBagConstraints.LINE_START;
        gbcSkipIdleCheckBox.insets = new Insets(0, 0, 5, 0);
        gbcSkipIdleCheckBox.gridx = 1;
        gbcSkipIdleCheckBox.gridy = 16;
        add(skipIdleCheckBox, gbcSkipIdleCheckBox);
        skipIdleCheckBox.addItemListener(e -> configurationChanged());

        final JLabel lblBackgroundColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.backgroundcolor.label"));
        final GridBagConstraints gbcLabelBackgroundColor = new GridBagConstraints();
        gbcLabelBackgroundColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelBackgroundColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelBackgroundColor.gridx = 0;
        gbcLabelBackgroundColor.gridy = 17;
        add(lblBackgroundColor, gbcLabelBackgroundColor);

        backgroundColorSelector = new ColorSelector();
        backgroundColorSelector.setToolTipText(Option.BACKGROUND_COLOR.getHelp());
        final GridBagConstraints gbcBackgroundColorSelector = new GridBagConstraints();
        gbcBackgroundColorSelector.fill = GridBagConstraints.BOTH;
        gbcBackgroundColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcBackgroundColorSelector.gridx = 1;
        gbcBackgroundColorSelector.gridy = 17;
        add(backgroundColorSelector, gbcBackgroundColorSelector);
        backgroundColorSelector.addPropertyChangeListener("color", propertyChangeListener);

        final JLabel lblFlashbackColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.flashbackcolor.label"));
        final GridBagConstraints gbcLabelFlashbackColor = new GridBagConstraints();
        gbcLabelFlashbackColor.anchor = GridBagConstraints.LINE_END;
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

        final JLabel lblFlashbackDuration = new JLabel(resourceBundle.getString("ui.panel.generalsettings.flashbackduration.label"));
        final GridBagConstraints gbcLabelFlashbackDuration = new GridBagConstraints();
        gbcLabelFlashbackDuration.anchor = GridBagConstraints.LINE_END;
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

        final JLabel lblKeepLastFrame = new JLabel(resourceBundle.getString("ui.panel.generalsettings.keeplastframe.label"));
        final GridBagConstraints gbcLabelKeepLastFrame = new GridBagConstraints();
        gbcLabelKeepLastFrame.anchor = GridBagConstraints.LINE_END;
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

        final JLabel lblLogo = new JLabel(resourceBundle.getString("ui.panel.generalsettings.logo.label"));
        final GridBagConstraints gbcLabelLogo = new GridBagConstraints();
        gbcLabelLogo.anchor = GridBagConstraints.LINE_END;
        gbcLabelLogo.insets = new Insets(0, 0, 5, 5);
        gbcLabelLogo.gridx = 0;
        gbcLabelLogo.gridy = 21;
        add(lblLogo, gbcLabelLogo);

        logoFileSelector = new FileSelector(FILES_ONLY) {
            private static final long serialVersionUID = 7372002776386603239L;

            @Override
            protected Type configure(final JFileChooser logoFileChooser) {
                logoFileChooser.setAcceptAllFileFilterUsed(false);
                logoFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.logo.format.all"), "jpg", "png")); //NON-NLS
                logoFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.logo.format.jpeg"), "jpg")); //NON-NLS
                logoFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.logo.format.png"), "png")); //NON-NLS
                return Type.OPEN;
            }
        };

        logoFileSelector.setToolTipText(Option.LOGO.getHelp());
        final GridBagConstraints gbcLogoFileSelector = new GridBagConstraints();
        gbcLogoFileSelector.fill = GridBagConstraints.BOTH;
        gbcLogoFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcLogoFileSelector.gridx = 1;
        gbcLogoFileSelector.gridy = 21;
        add(logoFileSelector, gbcLogoFileSelector);

        logoFileSelector.addPropertyChangeListener("filename", propertyChangeListener);

        final JLabel lblPhotosDirectorySelector = new JLabel(resourceBundle.getString("ui.panel.generalsettings.photodirectory.label"));
        final GridBagConstraints gbcLabelPhotosDirectorySelector = new GridBagConstraints();
        gbcLabelPhotosDirectorySelector.anchor = GridBagConstraints.LINE_END;
        gbcLabelPhotosDirectorySelector.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotosDirectorySelector.gridx = 0;
        gbcLabelPhotosDirectorySelector.gridy = 22;
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
        gbcPhotosDirectorySelector.gridy = 22;
        add(photosDirectorySelector, gbcPhotosDirectorySelector);

        photosDirectorySelector.addPropertyChangeListener("filename", propertyChangeListener);

        final JLabel lblPhotoTime = new JLabel(resourceBundle.getString("ui.panel.generalsettings.phototime.label"));
        final GridBagConstraints gbcLabelPhotoTime = new GridBagConstraints();
        gbcLabelPhotoTime.anchor = GridBagConstraints.LINE_END;
        gbcLabelPhotoTime.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotoTime.gridx = 0;
        gbcLabelPhotoTime.gridy = 23;
        add(lblPhotoTime, gbcLabelPhotoTime);

        photoTimeSpinner = new JSpinner();
        photoTimeSpinner.setToolTipText(Option.PHOTO_TIME.getHelp());
        photoTimeSpinner.setModel(new DurationSpinnerModel());
        photoTimeSpinner.setEditor(new DurationEditor(photoTimeSpinner));
        final GridBagConstraints gbcPhotoTimeSpinner = new GridBagConstraints();
        gbcPhotoTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcPhotoTimeSpinner.gridx = 1;
        gbcPhotoTimeSpinner.gridy = 23;
        add(photoTimeSpinner, gbcPhotoTimeSpinner);
        photoTimeSpinner.addChangeListener(changeListener);
    }

    private void setVideoSize(final int width, final int height) {
        widthSpinner.setValue(width);
        heightSpinner.setValue(height);
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
            try (InputStream is = getClass().getResourceAsStream("/maps.xml")) { //NON-NLS
                saxParser.parse(is, new DefaultHandler() {
                    private final StringBuilder sb = new StringBuilder();
                    private String name;
                    private String url;
                    private String attributionText;

                    @Override
                    @SuppressWarnings("checkstyle:MissingSwitchDefault") // Every other case can be ignored!
                    @SuppressFBWarnings(value = "SF_SWITCH_NO_DEFAULT", justification = "Every other case can be ignored!") //NON-NLS NON-NLS
                    public void endElement(final String uri, final String localName, @NonNls final String qName) {
                        switch (qName) {
                            case "name":
                                name = sb.toString().trim();
                                break;
                            case "url":
                                url = sb.toString().trim();
                                break;
                            case "attribution-text":
                                attributionText = sb.toString().trim();
                                break;
                            case "entry":
                                labeledItems.add(new MapTemplate(name, url, attributionText));
                                break;
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
        keepLastFrameSpinner.setValue(c.getKeepLastFrame());
        backgroundMapVisibilitySlider.setValue((int) (c.getBackgroundMapVisibility() * 100));
        photoTimeSpinner.setValue(c.getPhotoTime());

        final String tmsUrlTemplate = c.getTmsUrlTemplate();
        if (tmsUrlTemplate != null && !tmsUrlTemplate.isBlank()) {
            found:
            {
                final Collator collator = Collator.getInstance();
                for (final MapTemplate mapTemplate : mapTemplateList) {
                    if (collator.compare(mapTemplate.getUrl(), tmsUrlTemplate) == 0) {
                        tmsUrlTemplateComboBox.setSelectedItem(mapTemplate);
                        break found;
                    }
                }
                tmsUrlTemplateComboBox.setSelectedItem(tmsUrlTemplate);
            }
        }

        attributionTextArea.setText(c.getAttribution());
        skipIdleCheckBox.setSelected(c.isSkipIdle());
        if (c.getTailColor() != null) { // old saved files may not include this setting
            tailColorSelector.setColor(c.getTailColor());
        }
        outputFileSelector.setFilename(c.getOutput().toString());
        logoFileSelector.setFilename(c.getLogo() != null ? c.getLogo().toString() : "");
        fontSizeSpinner.setValue(c.getFontSize());
        markerSizeSpinner.setValue(c.getMarkerSize());
        waypointSizeSpinner.setValue(c.getWaypointSize());
        backgroundColorSelector.setColor(c.getBackgroundColor());
        flashbackColorSelector.setColor(c.getFlashbackColor());
        flashbackDurationSpinner.setValue(c.getFlashbackDuration());
    }


    public void buildConfiguration(final Configuration.Builder builder, final boolean replacePlaceholders) {
        final Long td = (Long) tailDurationSpinner.getValue();
        final Object tmsItem = tmsUrlTemplateComboBox.getSelectedItem();
        final String tmsUrlTemplate = tmsItem instanceof MapTemplate ? ((MapTemplate) tmsItem).getUrl() : (String) tmsItem;
        final String attribution = replacePlaceholders
                ? attributionTextArea.getText().replace("%MAP_ATTRIBUTION%", //NON-NLS
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
                .backgroundColor(backgroundColorSelector.getColor())
                .flashbackColor(flashbackColorSelector.getColor())
                .flashbackDuration((Long) flashbackDurationSpinner.getValue())
                .output(new File(outputFileSelector.getFilename()))
                .fontSize((Integer) fontSizeSpinner.getValue())
                .markerSize((Double) markerSizeSpinner.getValue())
                .waypointSize((Double) waypointSizeSpinner.getValue())
                .logo(new File(logoFileSelector.getFilename()))
                .photoDirectory(photosDirectorySelector.getFilename())
                .photoTime((Long) photoTimeSpinner.getValue())
                .attribution(attribution);
    }

    protected abstract void configurationChanged();

}
