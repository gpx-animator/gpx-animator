/*
 *  Copyright Contributors to the GPX Animator project.
 *
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
package app.gpx_animator.ui.swing;

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.Option;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.MapTemplate;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.data.SpeedUnit;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.util.MapUtil;
import org.jetbrains.annotations.NotNull;

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
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serial;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static app.gpx_animator.core.util.Utils.isEqual;
import static javax.swing.JFileChooser.DIRECTORIES_ONLY;
import static javax.swing.JFileChooser.FILES_ONLY;

abstract class GeneralSettingsPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = -2024548578211891192L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient FileSelector outputFileSelector;
    private final transient JSpinner widthSpinner;
    private final transient JSpinner heightSpinner;
    private final transient JSpinner viewportWidthSpinner;
    private final transient JSpinner viewportHeightSpinner;
    private final transient JSpinner viewportInertiaSpinner;
    private final transient JSpinner zoomSpinner;
    private final transient JSpinner marginSpinner;
    private final transient JSpinner logoMarginSpinner;
    private final transient JSpinner attributionMarginSpinner;
    private final transient JSpinner informationMarginSpinner;
    private final transient JSpinner commentMarginSpinner;
    private final transient JSpinner speedupSpinner;
    private final transient JSpinner markerSizeSpinner;
    private final transient JSpinner waypointSizeSpinner;
    private final transient ColorSelector tailColorSelector;
    private final transient JSpinner tailDurationSpinner;
    private final transient JSpinner fpsSpinner;
    private final transient JComboBox<MapTemplate> tmsUrlTemplateComboBox;
    private final transient JComboBox<SpeedUnit> speedUnitComboBox;
    private final transient JSlider backgroundMapVisibilitySlider;
    private final transient FontSelector fontSelector;
    private final transient FontSelector waypointFontSelector;
    private final transient JComboBox<Position> logoLocationComboBox;
    private final transient JComboBox<Position> attributionLocationComboBox;
    private final transient JComboBox<Position> informationLocationComboBox;
    private final transient JComboBox<Position> commentLocationComboBox;
    private final transient JCheckBox skipIdleCheckBox;
    private final transient JCheckBox preDrawTrackCheckBox;
    private final transient ColorSelector backgroundColorSelector;
    private final transient FileSelector backgroundImageSelector;
    private final transient ColorSelector flashbackColorSelector;
    private final transient JSpinner flashbackDurationSpinner;
    private final transient JSpinner keepLastFrameSpinner;
    private final transient JSpinner totalTimeSpinner;
    private final transient FileSelector logoFileSelector;
    private final transient FileSelector photosDirectorySelector;
    private final transient JSpinner photoTimeSpinner;
    private final transient JSpinner photoAnimationDurationSpinner;
    private final transient JTextArea attributionTextArea;
    private final transient JSpinner maxLatSpinner;
    private final transient JSpinner minLonSpinner;
    private final transient JSpinner maxLonSpinner;
    private final transient JSpinner minLatSpinner;

    private transient List<MapTemplate> mapTemplateList;

    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    GeneralSettingsPanel() {
        mapTemplateList = MapUtil.readMaps();

        setBorder(new EmptyBorder(5, 5, 5, 5));
        final var gridBagLayout = new GridBagLayout();
        gridBagLayout.columnWidths  = new int[]    {91,  100, 0,  0};
        gridBagLayout.columnWeights = new double[] {0.0, 1.0, 0.0, Double.MIN_VALUE};
        gridBagLayout.rowHeights    = new int[]    {14,  20,  20,  20,  20,  20,  14,  20,  20,  20,  20,  20,  20,  20,  20,  50,  45,  20,  21,
                23,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  20,  0};
        gridBagLayout.rowWeights    = new double[] {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
        setLayout(gridBagLayout);

        final var lblOutput = new JLabel(resourceBundle.getString("ui.panel.generalsettings.output.label"));
        final var gbcLabelOutput = new GridBagConstraints();
        gbcLabelOutput.anchor = GridBagConstraints.LINE_END;
        gbcLabelOutput.insets = new Insets(0, 0, 5, 5);
        gbcLabelOutput.gridx = 0;
        gbcLabelOutput.gridy = 0;
        add(lblOutput, gbcLabelOutput);

        outputFileSelector = new FileSelector(FILES_ONLY) {
            @Serial
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
                        && isEqual(String.format(filename, 100), String.format(filename, 200))) {
                    @SuppressWarnings("MagicCharacter") final var n = filename.lastIndexOf('.');
                    return String.format("%s%%08d%s", filename.substring(0, n), filename.substring(n)); //NON-NLS
                } else {
                    return filename;
                }
            }
        };

        outputFileSelector.setToolTipText(Option.OUTPUT.getHelp());
        final var gbcOutputFileSelector = new GridBagConstraints();
        gbcOutputFileSelector.fill = GridBagConstraints.BOTH;
        gbcOutputFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcOutputFileSelector.gridx = 1;
        gbcOutputFileSelector.gridy = 0;
        add(outputFileSelector, gbcOutputFileSelector);

        final PropertyChangeListener propertyChangeListener = evt -> configurationChanged();
        outputFileSelector.addPropertyChangeListener(FileSelector.PROPERTY_FILENAME, propertyChangeListener);

        final var lblWidthHeight = new JLabel(resourceBundle.getString("ui.panel.generalsettings.widthheight.label"));
        final var gbcLabelWidthHeight = new GridBagConstraints();
        gbcLabelWidthHeight.anchor = GridBagConstraints.LINE_END;
        gbcLabelWidthHeight.insets = new Insets(0, 0, 5, 5);
        gbcLabelWidthHeight.gridx = 0;
        gbcLabelWidthHeight.gridy = 1;
        add(lblWidthHeight, gbcLabelWidthHeight);

        final var widthHeightPanel = new JPanel();
        final var gbcWidthHeightPanel = new GridBagConstraints();
        gbcWidthHeightPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthHeightPanel.insets = new Insets(0, 0, 5, 0);
        gbcWidthHeightPanel.gridx = 1;
        gbcWidthHeightPanel.gridy = 1;
        add(widthHeightPanel, gbcWidthHeightPanel);
        final var gblWidthHeightPanel = new GridBagLayout();
        gblWidthHeightPanel.columnWeights = new double[]{10.0, 10.0, 1.0, Double.MIN_VALUE};
        widthHeightPanel.setLayout(gblWidthHeightPanel);

        widthSpinner = new JSpinner();
        widthSpinner.setToolTipText(Option.WIDTH.getHelp());
        widthSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        widthSpinner.setEditor(new EmptyZeroNumberEditor(widthSpinner, Integer.class));
        final var gbcWidthSpinner = new GridBagConstraints();
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
        final var gbcHeightSpinner = new GridBagConstraints();
        gbcHeightSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcHeightSpinner.insets = new Insets(0, 0, 5, 5);
        gbcHeightSpinner.gridx = 1;
        gbcHeightSpinner.gridy = 0;
        widthHeightPanel.add(heightSpinner, gbcHeightSpinner);
        heightSpinner.addChangeListener(changeListener);

        final Object[][] standardVideoSizes = {
                {"Default", null, null}, //NON-NLS
                {"SD, PAL", 768, 576}, //NON-NLS
                {"HD-Ready", 1280, 720}, //NON-NLS
                {"Full-HD", 1920, 1080}, //NON-NLS
                {"WQUXGA", 3840, 2400}, //NON-NLS
                {"DCI 4K Flat/Masked", 3996, 2160}, //NON-NLS
                {"DCI 4K CinemaScope", 4096, 1716}, //NON-NLS
                {"4K2K, DCI 4K, Cinema 4K", 4096, 2160}, //NON-NLS
                {"4K UHD, 4K, QFHD, 2160p/i", 3840, 2160} //NON-NLS
        };
        final var widthHeightPopup = new JPopupMenu();
        for (final var standardVideoSize : standardVideoSizes) {
            final var text = (String) standardVideoSize[0];
            final var width = (Integer) standardVideoSize[1];
            final var height = (Integer) standardVideoSize[2];
            final var name = width == null && height == null
                    ? resourceBundle.getString("ui.panel.generalsettings.widthheight.default")
                    : String.format("%s (%d x %d)", text, width, height); //NON-NLS
            widthHeightPopup.add(new JMenuItem(new AbstractAction(name) {
                @Serial
                private static final long serialVersionUID = -1125796034755504311L;

                public void actionPerformed(final ActionEvent e) {
                    setVideoSize(width, height);
                }
            }));
        }
        final var widthHeightButton = new JButton(resourceBundle.getString("ui.panel.generalsettings.widthheight.button"));
        widthHeightButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                widthHeightPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        final var gbcWidthHeightButton = new GridBagConstraints();
        gbcWidthHeightButton.fill = GridBagConstraints.HORIZONTAL;
        gbcWidthHeightButton.insets = new Insets(0, 0, 5, 0);
        gbcWidthHeightButton.gridx = 2;
        gbcWidthHeightButton.gridy = 0;
        widthHeightPanel.add(widthHeightButton, gbcWidthHeightButton);

        final var lblViewportWidthHeight = new JLabel(resourceBundle.getString("ui.panel.generalsettings.viewport.widthheight.label"));
        final var gbcLabelViewportWidthHeight = new GridBagConstraints();
        gbcLabelViewportWidthHeight.anchor = GridBagConstraints.LINE_END;
        gbcLabelViewportWidthHeight.insets = new Insets(0, 0, 5, 5);
        gbcLabelViewportWidthHeight.gridx = 0;
        gbcLabelViewportWidthHeight.gridy = 2;
        add(lblViewportWidthHeight, gbcLabelViewportWidthHeight);

        final var viewportWidthHeightPanel = new JPanel();
        final var gbcViewportWidthHeightPanel = new GridBagConstraints();
        gbcViewportWidthHeightPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcViewportWidthHeightPanel.insets = new Insets(0, 0, 5, 0);
        gbcViewportWidthHeightPanel.gridx = 1;
        gbcViewportWidthHeightPanel.gridy = 2;
        add(viewportWidthHeightPanel, gbcViewportWidthHeightPanel);
        final var gblViewportWidthHeightPanel = new GridBagLayout();
        gblViewportWidthHeightPanel.columnWeights = new double[]{10.0, 10.0, 1.0, Double.MIN_VALUE};
        viewportWidthHeightPanel.setLayout(gblViewportWidthHeightPanel);

        viewportWidthSpinner = new JSpinner();
        viewportWidthSpinner.setToolTipText(Option.VIEWPORT_WIDTH.getHelp());
        viewportWidthSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        viewportWidthSpinner.setEditor(new EmptyZeroNumberEditor(viewportWidthSpinner, Integer.class));
        final var gbcViewportWidthSpinner = new GridBagConstraints();
        gbcViewportWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcViewportWidthSpinner.insets = new Insets(0, 0, 5, 5);
        gbcViewportWidthSpinner.gridx = 0;
        gbcViewportWidthSpinner.gridy = 0;
        viewportWidthHeightPanel.add(viewportWidthSpinner, gbcViewportWidthSpinner);
        viewportWidthSpinner.addChangeListener(changeListener);

        viewportHeightSpinner = new JSpinner();
        viewportHeightSpinner.setToolTipText(Option.VIEWPORT_HEIGHT.getHelp());
        viewportHeightSpinner.setModel(new EmptyNullSpinnerModel(1, 0, null, 10));
        viewportHeightSpinner.setEditor(new EmptyZeroNumberEditor(viewportHeightSpinner, Integer.class));
        final var gbcViewportHeightSpinner = new GridBagConstraints();
        gbcViewportHeightSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcViewportHeightSpinner.insets = new Insets(0, 0, 5, 5);
        gbcViewportHeightSpinner.gridx = 1;
        gbcViewportHeightSpinner.gridy = 0;
        viewportWidthHeightPanel.add(viewportHeightSpinner, gbcViewportHeightSpinner);
        viewportHeightSpinner.addChangeListener(changeListener);

        final var viewportWidthHeightPopup = new JPopupMenu();
        for (final var standardVideoSize : standardVideoSizes) {
            final var text = (String) standardVideoSize[0];
            final var width = (Integer) standardVideoSize[1];
            final var height = (Integer) standardVideoSize[2];
            final var name = width == null && height == null
                    ? resourceBundle.getString("ui.panel.generalsettings.widthheight.default")
                    : String.format("%s (%d x %d)", text, width, height); //NON-NLS
            viewportWidthHeightPopup.add(new JMenuItem(new AbstractAction(name) {
                @Serial
                private static final long serialVersionUID = 3877332066001457485L;

                public void actionPerformed(final ActionEvent e) {
                    setViewportSize(width, height);
                }
            }));
        }
        final var viewportWidthHeightButton = new JButton(resourceBundle.getString("ui.panel.generalsettings.widthheight.button"));
        viewportWidthHeightButton.addMouseListener(new MouseAdapter() {
            public void mousePressed(final MouseEvent e) {
                viewportWidthHeightPopup.show(e.getComponent(), e.getX(), e.getY());
            }
        });

        final var gbcViewportWidthHeightButton = new GridBagConstraints();
        gbcViewportWidthHeightButton.fill = GridBagConstraints.HORIZONTAL;
        gbcViewportWidthHeightButton.insets = new Insets(0, 0, 5, 0);
        gbcViewportWidthHeightButton.gridx = 2;
        gbcViewportWidthHeightButton.gridy = 0;
        viewportWidthHeightPanel.add(viewportWidthHeightButton, gbcViewportWidthHeightButton);

        final var lblViewportInertia = new JLabel(resourceBundle.getString("ui.panel.generalsettings.viewport.inertia.label"));
        final var gbcLabelViewportInertia = new GridBagConstraints();
        gbcLabelViewportInertia.anchor = GridBagConstraints.LINE_END;
        gbcLabelViewportInertia.insets = new Insets(0, 0, 5, 5);
        gbcLabelViewportInertia.gridx = 0;
        gbcLabelViewportInertia.gridy = 3;
        add(lblViewportInertia, gbcLabelViewportInertia);

        viewportInertiaSpinner = new JSpinner();
        viewportInertiaSpinner.setToolTipText(Option.VIEWPORT_INERTIA.getHelp());
        viewportInertiaSpinner.setModel(new EmptyNullSpinnerModel(1, 0, 100, 1));
        viewportInertiaSpinner.setEditor(new EmptyZeroNumberEditor(viewportInertiaSpinner, Integer.class));
        final var gbcViewportInertiaSpinner = new GridBagConstraints();
        gbcViewportInertiaSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcViewportInertiaSpinner.insets = new Insets(0, 0, 5, 0);
        gbcViewportInertiaSpinner.gridx = 1;
        gbcViewportInertiaSpinner.gridy = 3;
        add(viewportInertiaSpinner, gbcViewportInertiaSpinner);
        viewportInertiaSpinner.addChangeListener(changeListener);

        final var lblZoom = new JLabel(resourceBundle.getString("ui.panel.generalsettings.zoom.label"));
        final var gbcLabelZoom = new GridBagConstraints();
        gbcLabelZoom.anchor = GridBagConstraints.LINE_END;
        gbcLabelZoom.insets = new Insets(0, 0, 5, 5);
        gbcLabelZoom.gridx = 0;
        gbcLabelZoom.gridy = 4;
        add(lblZoom, gbcLabelZoom);

        zoomSpinner = new JSpinner();
        zoomSpinner.setToolTipText(Option.ZOOM.getHelp());
        zoomSpinner.setModel(new EmptyNullSpinnerModel(1, 0, 18, 1));
        zoomSpinner.setEditor(new EmptyZeroNumberEditor(zoomSpinner, Integer.class));
        final var gbcZoomSpinner = new GridBagConstraints();
        gbcZoomSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcZoomSpinner.insets = new Insets(0, 0, 5, 0);
        gbcZoomSpinner.gridx = 1;
        gbcZoomSpinner.gridy = 4;
        add(zoomSpinner, gbcZoomSpinner);
        zoomSpinner.addChangeListener(changeListener);

        final var lblBoundingBox = new JLabel(resourceBundle.getString("ui.panel.generalsettings.boundingbox.label"));
        final var gbcLabelBoundingBox = new GridBagConstraints();
        gbcLabelBoundingBox.anchor = GridBagConstraints.LINE_END;
        gbcLabelBoundingBox.insets = new Insets(0, 0, 5, 5);
        gbcLabelBoundingBox.gridx = 0;
        gbcLabelBoundingBox.gridy = 5;
        add(lblBoundingBox, gbcLabelBoundingBox);

        final var boundingboxPanel = new JPanel();
        final var gbcPanel = new GridBagConstraints();
        gbcPanel.fill = GridBagConstraints.HORIZONTAL;
        gbcPanel.insets = new Insets(0, 0, 5, 0);
        gbcPanel.gridx = 1;
        gbcPanel.gridy = 5;
        add(boundingboxPanel, gbcPanel);
        final var gblPanel = new GridBagLayout();
        gblPanel.columnWidths = new int[]{0, 40, 40, 40, 0};
        gblPanel.rowHeights = new int[]{20, 0, 0, 0};
        gblPanel.columnWeights = new double[]{0.0, 1.0, 1.0, 1.0, Double.MIN_VALUE};
        gblPanel.rowWeights = new double[]{0.0, 0.0, 0.0, Double.MIN_VALUE};
        boundingboxPanel.setLayout(gblPanel);

        final var lblMaxLat = new JLabel(resourceBundle.getString("ui.panel.generalsettings.latitude.max.label"));
        final var gbcLabelMaxLat = new GridBagConstraints();
        gbcLabelMaxLat.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLat.anchor = GridBagConstraints.LINE_END;
        gbcLabelMaxLat.gridx = 1;
        gbcLabelMaxLat.gridy = 0;
        boundingboxPanel.add(lblMaxLat, gbcLabelMaxLat);

        final var lblMinLon = new JLabel(resourceBundle.getString("ui.panel.generalsettings.longitude.min.label"));
        final var gbcLabelMinLon = new GridBagConstraints();
        gbcLabelMinLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMinLon.anchor = GridBagConstraints.LINE_END;
        gbcLabelMinLon.gridx = 0;
        gbcLabelMinLon.gridy = 1;
        boundingboxPanel.add(lblMinLon, gbcLabelMinLon);

        minLonSpinner = new JSpinner();
        minLonSpinner.setToolTipText(Option.MIN_LON.getHelp());
        minLonSpinner.setEditor(new EmptyZeroNumberEditor(minLonSpinner, Double.class));
        minLonSpinner.setModel(new EmptyNullSpinnerModel(null, -180.0, 180.0, 0.1, false, 5));
        final var gbcMinLonSpinner = new GridBagConstraints();
        gbcMinLonSpinner.insets = new Insets(0, 0, 5, 5);
        gbcMinLonSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMinLonSpinner.gridx = 1;
        gbcMinLonSpinner.gridy = 1;
        boundingboxPanel.add(minLonSpinner, gbcMinLonSpinner);
        minLonSpinner.addChangeListener(changeListener);

        maxLatSpinner = new JSpinner();
        maxLatSpinner.setToolTipText(Option.MAX_LAT.getHelp());
        maxLatSpinner.setEditor(new EmptyZeroNumberEditor(maxLatSpinner, Double.class));
        maxLatSpinner.setModel(new EmptyNullSpinnerModel(null, -90.0, 90.0, 0.1, false, 5));
        final var gbcMaxLatSpinner = new GridBagConstraints();
        gbcMaxLatSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMaxLatSpinner.anchor = GridBagConstraints.PAGE_START;
        gbcMaxLatSpinner.insets = new Insets(0, 0, 5, 5);
        gbcMaxLatSpinner.gridx = 2;
        gbcMaxLatSpinner.gridy = 0;
        boundingboxPanel.add(maxLatSpinner, gbcMaxLatSpinner);
        maxLatSpinner.addChangeListener(changeListener);

        final var lblMaxLon = new JLabel(resourceBundle.getString("ui.panel.generalsettings.longitude.max.label"));
        final var gbcLabelMaxLon = new GridBagConstraints();
        gbcLabelMaxLon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMaxLon.anchor = GridBagConstraints.LINE_END;
        gbcLabelMaxLon.gridx = 2;
        gbcLabelMaxLon.gridy = 1;
        boundingboxPanel.add(lblMaxLon, gbcLabelMaxLon);

        final var lblMinLat = new JLabel(resourceBundle.getString("ui.panel.generalsettings.latitude.min.label"));
        final var gbcLabelMinLat = new GridBagConstraints();
        gbcLabelMinLat.insets = new Insets(0, 0, 0, 5);
        gbcLabelMinLat.anchor = GridBagConstraints.LINE_END;
        gbcLabelMinLat.gridx = 1;
        gbcLabelMinLat.gridy = 2;
        boundingboxPanel.add(lblMinLat, gbcLabelMinLat);

        minLatSpinner = new JSpinner();
        minLatSpinner.setToolTipText(Option.MIN_LAT.getHelp());
        minLatSpinner.setEditor(new EmptyZeroNumberEditor(minLatSpinner, Double.class));
        minLatSpinner.setModel(new EmptyNullSpinnerModel(null, -90.0, 90.0, 0.1, false, 5));
        final var gbcMinLatSpinner = new GridBagConstraints();
        gbcMinLatSpinner.insets = new Insets(0, 0, 0, 5);
        gbcMinLatSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMinLatSpinner.gridx = 2;
        gbcMinLatSpinner.gridy = 2;
        boundingboxPanel.add(minLatSpinner, gbcMinLatSpinner);
        minLatSpinner.addChangeListener(changeListener);

        maxLonSpinner = new JSpinner();
        maxLonSpinner.setToolTipText(Option.MAX_LON.getHelp());
        maxLonSpinner.setEditor(new EmptyZeroNumberEditor(maxLonSpinner, Double.class));
        maxLonSpinner.setModel(new EmptyNullSpinnerModel(null, -180.0, 180.0, 0.1, false, 5));
        final var gbcMaxLonSpinner = new GridBagConstraints();
        gbcMaxLonSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMaxLonSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMaxLonSpinner.gridx = 3;
        gbcMaxLonSpinner.gridy = 1;
        boundingboxPanel.add(maxLonSpinner, gbcMaxLonSpinner);
        maxLonSpinner.addChangeListener(changeListener);

        final var lblMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.margin.label"));
        final var gbcLabelMargin = new GridBagConstraints();
        gbcLabelMargin.anchor = GridBagConstraints.LINE_END;
        gbcLabelMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelMargin.gridx = 0;
        gbcLabelMargin.gridy = 6;
        add(lblMargin, gbcLabelMargin);

        marginSpinner = new JSpinner();
        marginSpinner.setToolTipText(Option.MARGIN.getHelp());
        marginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final var gbcMarginSpinner = new GridBagConstraints();
        gbcMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarginSpinner.gridx = 1;
        gbcMarginSpinner.gridy = 6;
        add(marginSpinner, gbcMarginSpinner);
        marginSpinner.addChangeListener(changeListener);

        final var lblSpeedup = new JLabel(resourceBundle.getString("ui.panel.generalsettings.speedup.label"));
        final var gbcLabelSpeedup = new GridBagConstraints();
        gbcLabelSpeedup.anchor = GridBagConstraints.LINE_END;
        gbcLabelSpeedup.insets = new Insets(0, 0, 5, 5);
        gbcLabelSpeedup.gridx = 0;
        gbcLabelSpeedup.gridy = 7;
        add(lblSpeedup, gbcLabelSpeedup);

        speedupSpinner = new JSpinner();
        speedupSpinner.setToolTipText(Option.SPEEDUP.getHelp());
        speedupSpinner.setModel(new EmptyNullSpinnerModel((double) 0, (double) 0, null, 1d));
        speedupSpinner.setEditor(new EmptyZeroNumberEditor(speedupSpinner, Double.class));
        final var gbcSpeedupSpinner = new GridBagConstraints();
        gbcSpeedupSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcSpeedupSpinner.insets = new Insets(0, 0, 5, 0);
        gbcSpeedupSpinner.gridx = 1;
        gbcSpeedupSpinner.gridy = 7;
        add(speedupSpinner, gbcSpeedupSpinner);

        speedupSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                final var enabled = speedupSpinner.getValue() == null;
                if (!enabled) {
                    totalTimeSpinner.setValue(null);
                }
                totalTimeSpinner.setEnabled(enabled);
            }
        });
        speedupSpinner.addChangeListener(changeListener);

        final var lblTotalTime = new JLabel(resourceBundle.getString("ui.panel.generalsettings.totaltime.label"));
        final var gbcLabelTotalTime = new GridBagConstraints();
        gbcLabelTotalTime.anchor = GridBagConstraints.LINE_END;
        gbcLabelTotalTime.insets = new Insets(0, 0, 5, 5);
        gbcLabelTotalTime.gridx = 0;
        gbcLabelTotalTime.gridy = 8;
        add(lblTotalTime, gbcLabelTotalTime);

        totalTimeSpinner = new JSpinner();
        totalTimeSpinner.setToolTipText(Option.TOTAL_TIME.getHelp());
        totalTimeSpinner.setModel(new DurationSpinnerModel());
        totalTimeSpinner.setEditor(new DurationEditor(totalTimeSpinner));
        final var gbcTotalTimeSpinner = new GridBagConstraints();
        gbcTotalTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTotalTimeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTotalTimeSpinner.gridx = 1;
        gbcTotalTimeSpinner.gridy = 8;
        add(totalTimeSpinner, gbcTotalTimeSpinner);

        totalTimeSpinner.addChangeListener(e -> {
            final var enabled = totalTimeSpinner.getValue() == null;
            if (!enabled) {
                speedupSpinner.setValue(null);
            }
            speedupSpinner.setEnabled(enabled);
        });
        totalTimeSpinner.addChangeListener(changeListener);

        final var lblMarkerSize = new JLabel(resourceBundle.getString("ui.panel.generalsettings.markersize.label"));
        final var gbcLabelMarkerSize = new GridBagConstraints();
        gbcLabelMarkerSize.anchor = GridBagConstraints.LINE_END;
        gbcLabelMarkerSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelMarkerSize.gridx = 0;
        gbcLabelMarkerSize.gridy = 9;
        add(lblMarkerSize, gbcLabelMarkerSize);

        markerSizeSpinner = new JSpinner();
        markerSizeSpinner.setToolTipText(Option.MARKER_SIZE.getHelp());
        markerSizeSpinner.setEditor(new EmptyZeroNumberEditor(markerSizeSpinner, Double.class));
        markerSizeSpinner.setModel(new EmptyNullSpinnerModel(6.0, 0.0, null, 1.0));
        final var gbcMarkerSizeSpinner = new GridBagConstraints();
        gbcMarkerSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcMarkerSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcMarkerSizeSpinner.gridx = 1;
        gbcMarkerSizeSpinner.gridy = 9;
        add(markerSizeSpinner, gbcMarkerSizeSpinner);
        markerSizeSpinner.addChangeListener(changeListener);

        final var lblWaypointSize = new JLabel(resourceBundle.getString("ui.panel.generalsettings.waypointsize.label"));
        final var gbcLabelWaypointSize = new GridBagConstraints();
        gbcLabelWaypointSize.anchor = GridBagConstraints.LINE_END;
        gbcLabelWaypointSize.insets = new Insets(0, 0, 5, 5);
        gbcLabelWaypointSize.gridx = 0;
        gbcLabelWaypointSize.gridy = 10;
        add(lblWaypointSize, gbcLabelWaypointSize);

        waypointSizeSpinner = new JSpinner();
        waypointSizeSpinner.setToolTipText(Option.WAYPOINT_SIZE.getHelp());
        waypointSizeSpinner.setEditor(new EmptyZeroNumberEditor(waypointSizeSpinner, Double.class));
        waypointSizeSpinner.setModel(new EmptyNullSpinnerModel(1.0, 0.0, null, 1.0));
        final var gbcWaypointSizeSpinner = new GridBagConstraints();
        gbcWaypointSizeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcWaypointSizeSpinner.insets = new Insets(0, 0, 5, 0);
        gbcWaypointSizeSpinner.gridx = 1;
        gbcWaypointSizeSpinner.gridy = 10;
        add(waypointSizeSpinner, gbcWaypointSizeSpinner);
        waypointSizeSpinner.addChangeListener(changeListener);

        final var lblTailColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.tailcolor.label"));
        final var gbcLabelTailColor = new GridBagConstraints();
        gbcLabelTailColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelTailColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailColor.gridx = 0;
        gbcLabelTailColor.gridy = 11;
        add(lblTailColor, gbcLabelTailColor);

        tailColorSelector = new ColorSelector();
        tailColorSelector.setToolTipText(Option.TAIL_COLOR.getHelp());
        final var gbcTailColorSelector = new GridBagConstraints();
        gbcTailColorSelector.fill = GridBagConstraints.BOTH;
        gbcTailColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcTailColorSelector.gridx = 1;
        gbcTailColorSelector.gridy = 11;
        add(tailColorSelector, gbcTailColorSelector);
        tailColorSelector.addPropertyChangeListener(ColorSelector.PROPERTY_COLOR, propertyChangeListener);

        final var lblTailDuration = new JLabel(resourceBundle.getString("ui.panel.generalsettings.tailduration.label"));
        final var gbcLabelTailDuration = new GridBagConstraints();
        gbcLabelTailDuration.anchor = GridBagConstraints.LINE_END;
        gbcLabelTailDuration.insets = new Insets(0, 0, 5, 5);
        gbcLabelTailDuration.gridx = 0;
        gbcLabelTailDuration.gridy = 12;
        add(lblTailDuration, gbcLabelTailDuration);

        tailDurationSpinner = new JSpinner();
        tailDurationSpinner.setToolTipText(Option.TAIL_DURATION.getHelp());
        tailDurationSpinner.setModel(new DurationSpinnerModel());
        tailDurationSpinner.setEditor(new DurationEditor(tailDurationSpinner));
        final var gbcTailDurationSpinner = new GridBagConstraints();
        gbcTailDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTailDurationSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTailDurationSpinner.gridx = 1;
        gbcTailDurationSpinner.gridy = 12;
        add(tailDurationSpinner, gbcTailDurationSpinner);
        tailDurationSpinner.addChangeListener(changeListener);

        final var lblFps = new JLabel(resourceBundle.getString("ui.panel.generalsettings.fps.label"));
        final var gbcLabelFps = new GridBagConstraints();
        gbcLabelFps.anchor = GridBagConstraints.LINE_END;
        gbcLabelFps.insets = new Insets(0, 0, 5, 5);
        gbcLabelFps.gridx = 0;
        gbcLabelFps.gridy = 13;
        add(lblFps, gbcLabelFps);

        fpsSpinner = new JSpinner();
        fpsSpinner.setToolTipText(Option.FPS.getHelp());
        fpsSpinner.setModel(new SpinnerNumberModel(0.1, 0.1, null, 1d));
        final var gbcFpsSpinner = new GridBagConstraints();
        gbcFpsSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFpsSpinner.insets = new Insets(0, 0, 5, 0);
        gbcFpsSpinner.gridx = 1;
        gbcFpsSpinner.gridy = 13;
        add(fpsSpinner, gbcFpsSpinner);
        fpsSpinner.addChangeListener(changeListener);

        final var lblTmsUrlTemplate = new JLabel(resourceBundle.getString("ui.panel.generalsettings.map.label"));
        final var gbcLabelTmsUrlTemplate = new GridBagConstraints();
        gbcLabelTmsUrlTemplate.anchor = GridBagConstraints.LINE_END;
        gbcLabelTmsUrlTemplate.insets = new Insets(0, 0, 5, 5);
        gbcLabelTmsUrlTemplate.gridx = 0;
        gbcLabelTmsUrlTemplate.gridy = 14;
        add(lblTmsUrlTemplate, gbcLabelTmsUrlTemplate);

        tmsUrlTemplateComboBox = new JComboBox<>();
        tmsUrlTemplateComboBox.setToolTipText(Option.TMS_URL_TEMPLATE.getHelp());
        tmsUrlTemplateComboBox.setEditable(true);
        tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<>(mapTemplateList.toArray(new MapTemplate[0])));
        final var gbcTmsUrlTemplateComboBox = new GridBagConstraints();
        gbcTmsUrlTemplateComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcTmsUrlTemplateComboBox.insets = new Insets(0, 0, 5, 0);
        gbcTmsUrlTemplateComboBox.gridx = 1;
        gbcTmsUrlTemplateComboBox.gridy = 14;
        add(tmsUrlTemplateComboBox, gbcTmsUrlTemplateComboBox);
        tmsUrlTemplateComboBox.setPreferredSize(new Dimension(10, tmsUrlTemplateComboBox.getPreferredSize().height));

        final var lblAttribution = new JLabel(resourceBundle.getString("ui.panel.generalsettings.attribution.label"));
        final var gbcLabelAttribution = new GridBagConstraints();
        gbcLabelAttribution.anchor = GridBagConstraints.LINE_END;
        gbcLabelAttribution.insets = new Insets(0, 0, 5, 5);
        gbcLabelAttribution.gridx = 0;
        gbcLabelAttribution.gridy = 15;
        add(lblAttribution, gbcLabelAttribution);

        final var scrollPane = new JScrollPane();
        scrollPane.setPreferredSize(new Dimension(3, 50));
        final var gbcScrollPane = new GridBagConstraints();
        gbcScrollPane.fill = GridBagConstraints.BOTH;
        gbcScrollPane.insets = new Insets(0, 0, 5, 0);
        gbcScrollPane.gridx = 1;
        gbcScrollPane.gridy = 15;
        add(scrollPane, gbcScrollPane);

        attributionTextArea = new JTextArea();
        attributionTextArea.setToolTipText(Option.ATTRIBUTION.getHelp());
        final var attributionPopupMenu = new JPopupMenu();
        Map.of("%APPNAME_VERSION%", "application name and version", "%MAP_ATTRIBUTION%", "map attribution text")
                .forEach((variable, title) -> {
                    final var menuItem = new JMenuItem("insert %s".formatted(title));
                    menuItem.addActionListener(l -> {
                        if (attributionTextArea.getSelectedText() != null) {
                            attributionTextArea.replaceSelection(variable);
                        } else {
                            attributionTextArea.insert(variable, attributionTextArea.getCaretPosition());
                        }
                    });
                    attributionPopupMenu.add(menuItem);
                });
        attributionTextArea.setComponentPopupMenu(attributionPopupMenu);
        scrollPane.setViewportView(attributionTextArea);

        final var lblVisibility = new JLabel(resourceBundle.getString("ui.panel.generalsettings.visibility.label"));
        final var gbcLabelVisibility = new GridBagConstraints();
        gbcLabelVisibility.anchor = GridBagConstraints.LINE_END;
        gbcLabelVisibility.insets = new Insets(0, 0, 5, 5);
        gbcLabelVisibility.gridx = 0;
        gbcLabelVisibility.gridy = 16;
        add(lblVisibility, gbcLabelVisibility);

        backgroundMapVisibilitySlider = new JSlider();
        backgroundMapVisibilitySlider.setMinorTickSpacing(5);
        backgroundMapVisibilitySlider.setPaintTicks(true);
        backgroundMapVisibilitySlider.setMajorTickSpacing(10);
        backgroundMapVisibilitySlider.setPaintLabels(true);
        backgroundMapVisibilitySlider.setToolTipText(Option.BACKGROUND_MAP_VISIBILITY.getHelp());
        final var gbcBackgroundMapVisibilitySlider = new GridBagConstraints();
        gbcBackgroundMapVisibilitySlider.fill = GridBagConstraints.HORIZONTAL;
        gbcBackgroundMapVisibilitySlider.insets = new Insets(0, 0, 5, 0);
        gbcBackgroundMapVisibilitySlider.gridx = 1;
        gbcBackgroundMapVisibilitySlider.gridy = 16;
        add(backgroundMapVisibilitySlider, gbcBackgroundMapVisibilitySlider);
        backgroundMapVisibilitySlider.addChangeListener(changeListener);

        final var lblPreDrawTrack = new JLabel(resourceBundle.getString("ui.panel.generalsettings.predrawtrack.label"));
        final var gbcLabelPreDrawTrack = new GridBagConstraints();
        gbcLabelPreDrawTrack.anchor = GridBagConstraints.LINE_END;
        gbcLabelPreDrawTrack.insets = new Insets(0, 0, 5, 5);
        gbcLabelPreDrawTrack.gridx = 0;
        gbcLabelPreDrawTrack.gridy = 17;
        add(lblPreDrawTrack, gbcLabelPreDrawTrack);

        preDrawTrackCheckBox = new JCheckBox("");
        preDrawTrackCheckBox.setToolTipText(Option.PRE_DRAW_TRACK.getHelp());
        final var gbcPreDrawTrackBox = new GridBagConstraints();
        gbcPreDrawTrackBox.anchor = GridBagConstraints.LINE_START;
        gbcPreDrawTrackBox.insets = new Insets(0, 0, 5, 0);
        gbcPreDrawTrackBox.gridx = 1;
        gbcPreDrawTrackBox.gridy = 17;
        add(preDrawTrackCheckBox, gbcPreDrawTrackBox);
        preDrawTrackCheckBox.addItemListener(e -> configurationChanged());

        final var lblSkipIdle = new JLabel(resourceBundle.getString("ui.panel.generalsettings.skipidle.label"));
        final var gbcLabelSkipIdle = new GridBagConstraints();
        gbcLabelSkipIdle.anchor = GridBagConstraints.LINE_END;
        gbcLabelSkipIdle.insets = new Insets(0, 0, 5, 5);
        gbcLabelSkipIdle.gridx = 0;
        gbcLabelSkipIdle.gridy = 18;
        add(lblSkipIdle, gbcLabelSkipIdle);

        skipIdleCheckBox = new JCheckBox("");
        skipIdleCheckBox.setToolTipText(Option.SKIP_IDLE.getHelp());
        final var gbcSkipIdleCheckBox = new GridBagConstraints();
        gbcSkipIdleCheckBox.anchor = GridBagConstraints.LINE_START;
        gbcSkipIdleCheckBox.insets = new Insets(0, 0, 5, 0);
        gbcSkipIdleCheckBox.gridx = 1;
        gbcSkipIdleCheckBox.gridy = 18;
        add(skipIdleCheckBox, gbcSkipIdleCheckBox);
        skipIdleCheckBox.addItemListener(e -> configurationChanged());

        final var lblBackgroundColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.backgroundcolor.label"));
        final var gbcLabelBackgroundColor = new GridBagConstraints();
        gbcLabelBackgroundColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelBackgroundColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelBackgroundColor.gridx = 0;
        gbcLabelBackgroundColor.gridy = 19;
        add(lblBackgroundColor, gbcLabelBackgroundColor);

        backgroundColorSelector = new ColorSelector();
        backgroundColorSelector.setToolTipText(Option.BACKGROUND_COLOR.getHelp());
        final var gbcBackgroundColorSelector = new GridBagConstraints();
        gbcBackgroundColorSelector.fill = GridBagConstraints.BOTH;
        gbcBackgroundColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcBackgroundColorSelector.gridx = 1;
        gbcBackgroundColorSelector.gridy = 19;
        add(backgroundColorSelector, gbcBackgroundColorSelector);
        backgroundColorSelector.addPropertyChangeListener(ColorSelector.PROPERTY_COLOR, propertyChangeListener);

        final var lblBackgroundImage = new JLabel(resourceBundle.getString("ui.panel.generalsettings.backgroundimage.label"));
        final var gbcLabelBackgroundImage = new GridBagConstraints();
        gbcLabelBackgroundImage.anchor = GridBagConstraints.LINE_END;
        gbcLabelBackgroundImage.insets = new Insets(0, 0, 5, 5);
        gbcLabelBackgroundImage.gridx = 0;
        gbcLabelBackgroundImage.gridy = 20;
        add(lblBackgroundImage, gbcLabelBackgroundImage);

        backgroundImageSelector = createImageFileSelector();
        backgroundImageSelector.setToolTipText(Option.BACKGROUND_IMAGE.getHelp());
        final var gbcBackgroundImageSelector = new GridBagConstraints();
        gbcBackgroundImageSelector.fill = GridBagConstraints.BOTH;
        gbcBackgroundImageSelector.insets = new Insets(0, 0, 5, 0);
        gbcBackgroundImageSelector.gridx = 1;
        gbcBackgroundImageSelector.gridy = 20;
        add(backgroundImageSelector, gbcBackgroundImageSelector);
        backgroundImageSelector.addPropertyChangeListener(FileSelector.PROPERTY_FILENAME, propertyChangeListener);

        final var lblFlashbackColor = new JLabel(resourceBundle.getString("ui.panel.generalsettings.flashbackcolor.label"));
        final var gbcLabelFlashbackColor = new GridBagConstraints();
        gbcLabelFlashbackColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelFlashbackColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelFlashbackColor.gridx = 0;
        gbcLabelFlashbackColor.gridy = 21;
        add(lblFlashbackColor, gbcLabelFlashbackColor);

        flashbackColorSelector = new ColorSelector();
        flashbackColorSelector.setToolTipText(Option.FLASHBACK_COLOR.getHelp());
        final var gbcFlashbackColorSelector = new GridBagConstraints();
        gbcFlashbackColorSelector.fill = GridBagConstraints.BOTH;
        gbcFlashbackColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcFlashbackColorSelector.gridx = 1;
        gbcFlashbackColorSelector.gridy = 21;
        add(flashbackColorSelector, gbcFlashbackColorSelector);
        flashbackColorSelector.addPropertyChangeListener(ColorSelector.PROPERTY_COLOR, propertyChangeListener);

        final var lblFlashbackDuration = new JLabel(resourceBundle.getString("ui.panel.generalsettings.flashbackduration.label"));
        final var gbcLabelFlashbackDuration = new GridBagConstraints();
        gbcLabelFlashbackDuration.anchor = GridBagConstraints.LINE_END;
        gbcLabelFlashbackDuration.insets = new Insets(0, 0, 0, 5);
        gbcLabelFlashbackDuration.gridx = 0;
        gbcLabelFlashbackDuration.gridy = 22;
        add(lblFlashbackDuration, gbcLabelFlashbackDuration);

        flashbackDurationSpinner = new JSpinner();
        flashbackDurationSpinner.setToolTipText(Option.FLASHBACK_DURATION.getHelp());
        flashbackDurationSpinner.setModel(new DurationSpinnerModel());
        flashbackDurationSpinner.setEditor(new DurationEditor(flashbackDurationSpinner));
        final var gbcFlashbackDurationSpinner = new GridBagConstraints();
        gbcFlashbackDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcFlashbackDurationSpinner.gridx = 1;
        gbcFlashbackDurationSpinner.gridy = 22;
        add(flashbackDurationSpinner, gbcFlashbackDurationSpinner);
        flashbackDurationSpinner.addChangeListener(changeListener);

        final var lblKeepLastFrame = new JLabel(resourceBundle.getString("ui.panel.generalsettings.keeplastframe.label"));
        final var gbcLabelKeepLastFrame = new GridBagConstraints();
        gbcLabelKeepLastFrame.anchor = GridBagConstraints.LINE_END;
        gbcLabelKeepLastFrame.insets = new Insets(0, 0, 0, 5);
        gbcLabelKeepLastFrame.gridx = 0;
        gbcLabelKeepLastFrame.gridy = 23;
        add(lblKeepLastFrame, gbcLabelKeepLastFrame);

        keepLastFrameSpinner = new JSpinner();
        keepLastFrameSpinner.setToolTipText(Option.KEEP_LAST_FRAME.getHelp());
        keepLastFrameSpinner.setModel(new DurationSpinnerModel());
        keepLastFrameSpinner.setEditor(new DurationEditor(keepLastFrameSpinner));
        final var gbcKeepLastFrameSpinner = new GridBagConstraints();
        gbcKeepLastFrameSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcKeepLastFrameSpinner.gridx = 1;
        gbcKeepLastFrameSpinner.gridy = 23;
        add(keepLastFrameSpinner, gbcKeepLastFrameSpinner);
        keepLastFrameSpinner.addChangeListener(changeListener);

        final var lblLogo = new JLabel(resourceBundle.getString("ui.panel.generalsettings.logo.label"));
        final var gbcLabelLogo = new GridBagConstraints();
        gbcLabelLogo.anchor = GridBagConstraints.LINE_END;
        gbcLabelLogo.insets = new Insets(0, 0, 5, 5);
        gbcLabelLogo.gridx = 0;
        gbcLabelLogo.gridy = 24;
        add(lblLogo, gbcLabelLogo);

        logoFileSelector = createImageFileSelector();
        logoFileSelector.setToolTipText(Option.LOGO.getHelp());
        final var gbcLogoFileSelector = new GridBagConstraints();
        gbcLogoFileSelector.fill = GridBagConstraints.BOTH;
        gbcLogoFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcLogoFileSelector.gridx = 1;
        gbcLogoFileSelector.gridy = 24;
        add(logoFileSelector, gbcLogoFileSelector);

        logoFileSelector.addPropertyChangeListener(FileSelector.PROPERTY_FILENAME, propertyChangeListener);

        final var lblPhotosDirectorySelector = new JLabel(resourceBundle.getString("ui.panel.generalsettings.photodirectory.label"));
        final var gbcLabelPhotosDirectorySelector = new GridBagConstraints();
        gbcLabelPhotosDirectorySelector.anchor = GridBagConstraints.LINE_END;
        gbcLabelPhotosDirectorySelector.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotosDirectorySelector.gridx = 0;
        gbcLabelPhotosDirectorySelector.gridy = 25;
        add(lblPhotosDirectorySelector, gbcLabelPhotosDirectorySelector);

        photosDirectorySelector = new FileSelector(DIRECTORIES_ONLY) {
            @Serial
            private static final long serialVersionUID = 7372002778976603240L;

            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };

        photosDirectorySelector.setToolTipText(Option.PHOTO_DIR.getHelp());
        final var gbcPhotosDirectorySelector = new GridBagConstraints();
        gbcPhotosDirectorySelector.fill = GridBagConstraints.BOTH;
        gbcPhotosDirectorySelector.insets = new Insets(0, 0, 5, 0);
        gbcPhotosDirectorySelector.gridx = 1;
        gbcPhotosDirectorySelector.gridy = 25;
        add(photosDirectorySelector, gbcPhotosDirectorySelector);

        photosDirectorySelector.addPropertyChangeListener(FileSelector.PROPERTY_FILENAME, propertyChangeListener);

        final var lblPhotoTime = new JLabel(resourceBundle.getString("ui.panel.generalsettings.phototime.label"));
        final var gbcLabelPhotoTime = new GridBagConstraints();
        gbcLabelPhotoTime.anchor = GridBagConstraints.LINE_END;
        gbcLabelPhotoTime.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotoTime.gridx = 0;
        gbcLabelPhotoTime.gridy = 26;
        add(lblPhotoTime, gbcLabelPhotoTime);

        photoTimeSpinner = new JSpinner();
        photoTimeSpinner.setToolTipText(Option.PHOTO_TIME.getHelp());
        photoTimeSpinner.setModel(new DurationSpinnerModel());
        photoTimeSpinner.setEditor(new DurationEditor(photoTimeSpinner));
        final var gbcPhotoTimeSpinner = new GridBagConstraints();
        gbcPhotoTimeSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcPhotoTimeSpinner.gridx = 1;
        gbcPhotoTimeSpinner.gridy = 26;
        add(photoTimeSpinner, gbcPhotoTimeSpinner);
        photoTimeSpinner.addChangeListener(changeListener);

        final var lblPhotoAnimationDuration = new JLabel(resourceBundle.getString("ui.panel.generalsettings.photoanimationduration.label"));
        final var gbcLabelPhotoAnimationDuration = new GridBagConstraints();
        gbcLabelPhotoAnimationDuration.anchor = GridBagConstraints.LINE_END;
        gbcLabelPhotoAnimationDuration.insets = new Insets(0, 0, 0, 5);
        gbcLabelPhotoAnimationDuration.gridx = 0;
        gbcLabelPhotoAnimationDuration.gridy = 27;
        add(lblPhotoAnimationDuration, gbcLabelPhotoAnimationDuration);

        photoAnimationDurationSpinner = new JSpinner();
        photoAnimationDurationSpinner.setToolTipText(Option.PHOTO_ANIMATION_DURATION.getHelp());
        photoAnimationDurationSpinner.setModel(new DurationSpinnerModel());
        photoAnimationDurationSpinner.setEditor(new DurationEditor(photoAnimationDurationSpinner));
        final var gbcPhotoAnimationDurationSpinner = new GridBagConstraints();
        gbcPhotoAnimationDurationSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcPhotoAnimationDurationSpinner.gridx = 1;
        gbcPhotoAnimationDurationSpinner.gridy = 27;
        add(photoAnimationDurationSpinner, gbcPhotoAnimationDurationSpinner);
        photoAnimationDurationSpinner.addChangeListener(changeListener);

        final var lblLogoPosition = new JLabel(resourceBundle.getString("ui.panel.generalsettings.logoPosition.label"));
        final var gbcLabelLogoPosition = new GridBagConstraints();
        gbcLabelLogoPosition.anchor = GridBagConstraints.LINE_END;
        gbcLabelLogoPosition.insets = new Insets(0, 0, 5, 5);
        gbcLabelLogoPosition.gridx = 0;
        gbcLabelLogoPosition.gridy = 28;
        add(lblLogoPosition, gbcLabelLogoPosition);

        logoLocationComboBox = new JComboBox<>();
        logoLocationComboBox.setToolTipText(Option.LOGO_POSITION.getHelp());
        Position.fillComboBox(logoLocationComboBox);
        final var gbcLogoPositioning = new GridBagConstraints();
        gbcLogoPositioning.fill = GridBagConstraints.HORIZONTAL;
        gbcLogoPositioning.gridx = 1;
        gbcLogoPositioning.gridy = 28;
        add(logoLocationComboBox, gbcLogoPositioning);

        final var lblLogoMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.logoMargin.label"));
        final var gbcLabelLogoMargin = new GridBagConstraints();
        gbcLabelLogoMargin.anchor = GridBagConstraints.LINE_END;
        gbcLabelLogoMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelLogoMargin.gridx = 0;
        gbcLabelLogoMargin.gridy = 29;
        add(lblLogoMargin, gbcLabelLogoMargin);

        logoMarginSpinner = new JSpinner();
        logoMarginSpinner.setToolTipText(Option.LOGO_MARGIN.getHelp());
        logoMarginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final var gbcLogoMarginSpinner = new GridBagConstraints();
        gbcLogoMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcLogoMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcLogoMarginSpinner.gridx = 1;
        gbcLogoMarginSpinner.gridy = 29;
        add(logoMarginSpinner, gbcLogoMarginSpinner);
        logoMarginSpinner.addChangeListener(changeListener);

        final var lblAttriPosition = new JLabel(resourceBundle.getString("ui.panel.generalsettings.attributionPosition.label"));
        final var gbcLabelAttributionPosition = new GridBagConstraints();
        gbcLabelAttributionPosition.anchor = GridBagConstraints.LINE_END;
        gbcLabelAttributionPosition.insets = new Insets(0, 0, 5, 5);
        gbcLabelAttributionPosition.gridx = 0;
        gbcLabelAttributionPosition.gridy = 30;
        add(lblAttriPosition, gbcLabelAttributionPosition);

        attributionLocationComboBox = new JComboBox<>();
        attributionLocationComboBox.setToolTipText(Option.ATTRIBUTION_POSITION.getHelp());
        Position.fillComboBox(attributionLocationComboBox);
        final var gbcAttributionPositioning = new GridBagConstraints();
        gbcAttributionPositioning.fill = GridBagConstraints.HORIZONTAL;
        gbcAttributionPositioning.gridx = 1;
        gbcAttributionPositioning.gridy = 30;
        add(attributionLocationComboBox, gbcAttributionPositioning);

        final var lblAttributionMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.attributionMargin.label"));
        final var gbcAttributionLabelMargin = new GridBagConstraints();
        gbcAttributionLabelMargin.anchor = GridBagConstraints.LINE_END;
        gbcAttributionLabelMargin.insets = new Insets(0, 0, 5, 5);
        gbcAttributionLabelMargin.gridx = 0;
        gbcAttributionLabelMargin.gridy = 31;
        add(lblAttributionMargin, gbcAttributionLabelMargin);

        attributionMarginSpinner = new JSpinner();
        attributionMarginSpinner.setToolTipText(Option.ATTRIBUTION_MARGIN.getHelp());
        attributionMarginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final var gbcAttributionMarginSpinner = new GridBagConstraints();
        gbcAttributionMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcAttributionMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcAttributionMarginSpinner.gridx = 1;
        gbcAttributionMarginSpinner.gridy = 31;
        add(attributionMarginSpinner, gbcAttributionMarginSpinner);
        attributionMarginSpinner.addChangeListener(changeListener);

        final var lblInformationPosition = new JLabel(resourceBundle.getString("ui.panel.generalsettings.informationPosition.label"));
        final var gbcLabelInfoPosition = new GridBagConstraints();
        gbcLabelInfoPosition.anchor = GridBagConstraints.LINE_END;
        gbcLabelInfoPosition.insets = new Insets(0, 0, 5, 5);
        gbcLabelInfoPosition.gridx = 0;
        gbcLabelInfoPosition.gridy = 32;
        add(lblInformationPosition, gbcLabelInfoPosition);

        informationLocationComboBox = new JComboBox<>();
        informationLocationComboBox.setToolTipText(Option.INFORMATION_POSITION.getHelp());
        Position.fillComboBox(informationLocationComboBox);
        final var gbcInformationPosition = new GridBagConstraints();
        gbcInformationPosition.fill = GridBagConstraints.HORIZONTAL;
        gbcInformationPosition.gridx = 1;
        gbcInformationPosition.gridy = 32;
        add(informationLocationComboBox, gbcInformationPosition);

        final var lblInformationMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.informationMargin.label"));
        final var gbcLabelInformationMargin = new GridBagConstraints();
        gbcLabelInformationMargin.anchor = GridBagConstraints.LINE_END;
        gbcLabelInformationMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelInformationMargin.gridx = 0;
        gbcLabelInformationMargin.gridy = 33;
        add(lblInformationMargin, gbcLabelInformationMargin);

        informationMarginSpinner = new JSpinner();
        informationMarginSpinner.setToolTipText(Option.INFORMATION_MARGIN.getHelp());
        informationMarginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final var gbcInformationMarginSpinner = new GridBagConstraints();
        gbcInformationMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcInformationMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcInformationMarginSpinner.gridx = 1;
        gbcInformationMarginSpinner.gridy = 33;
        add(informationMarginSpinner, gbcInformationMarginSpinner);
        informationMarginSpinner.addChangeListener(changeListener);

        final var lblCommentPosition = new JLabel(resourceBundle.getString("ui.panel.generalsettings.commentPosition.label"));
        final var gbcLabelCommentPosition = new GridBagConstraints();
        gbcLabelCommentPosition.anchor = GridBagConstraints.LINE_END;
        gbcLabelCommentPosition.insets = new Insets(0, 0, 5, 5);
        gbcLabelCommentPosition.gridx = 0;
        gbcLabelCommentPosition.gridy = 34;
        add(lblCommentPosition, gbcLabelCommentPosition);

        commentLocationComboBox = new JComboBox<>();
        commentLocationComboBox.setToolTipText(Option.COMMENT_POSITION.getHelp());
        Position.fillComboBox(commentLocationComboBox);
        final var gbcCommentPosition = new GridBagConstraints();
        gbcCommentPosition.fill = GridBagConstraints.HORIZONTAL;
        gbcCommentPosition.gridx = 1;
        gbcCommentPosition.gridy = 34;
        add(commentLocationComboBox, gbcCommentPosition);

        final var lblCommentMargin = new JLabel(resourceBundle.getString("ui.panel.generalsettings.commentMargin.label"));
        final var gbcLabelCommentMargin = new GridBagConstraints();
        gbcLabelCommentMargin.anchor = GridBagConstraints.LINE_END;
        gbcLabelCommentMargin.insets = new Insets(0, 0, 5, 5);
        gbcLabelCommentMargin.gridx = 0;
        gbcLabelCommentMargin.gridy = 35;
        add(lblCommentMargin, gbcLabelCommentMargin);

        commentMarginSpinner = new JSpinner();
        commentMarginSpinner.setToolTipText(Option.COMMENT_MARGIN.getHelp());
        commentMarginSpinner.setModel(new SpinnerNumberModel(0, 0, null, 1));
        final var gbcCommentMarginSpinner = new GridBagConstraints();
        gbcCommentMarginSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcCommentMarginSpinner.insets = new Insets(0, 0, 5, 0);
        gbcCommentMarginSpinner.gridx = 1;
        gbcCommentMarginSpinner.gridy = 35;
        add(commentMarginSpinner, gbcCommentMarginSpinner);
        commentMarginSpinner.addChangeListener(changeListener);

        final var lblFont = new JLabel(resourceBundle.getString("ui.panel.generalsettings.font.label"));
        final var gbcLabelFont = new GridBagConstraints();
        gbcLabelFont.anchor = GridBagConstraints.LINE_END;
        gbcLabelFont.insets = new Insets(0, 0, 5, 5);
        gbcLabelFont.gridx = 0;
        gbcLabelFont.gridy = 36;
        add(lblFont, gbcLabelFont);

        fontSelector = new FontSelector();
        fontSelector.setToolTipText(Option.FONT.getHelp());
        final var gbcFontName = new GridBagConstraints();
        gbcFontName.fill = GridBagConstraints.HORIZONTAL;
        gbcFontName.gridx = 1;
        gbcFontName.gridy = 36;
        add(fontSelector, gbcFontName);

        final var lblWaypointFont = new JLabel(resourceBundle.getString("ui.panel.generalsettings.waypointfont.label"));
        final var gbcLabelWaypointFont = new GridBagConstraints();
        gbcLabelWaypointFont.anchor = GridBagConstraints.LINE_END;
        gbcLabelWaypointFont.insets = new Insets(0, 0, 5, 5);
        gbcLabelWaypointFont.gridx = 0;
        gbcLabelWaypointFont.gridy = 37;
        add(lblWaypointFont, gbcLabelWaypointFont);

        waypointFontSelector = new FontSelector();
        waypointFontSelector.setToolTipText(Option.WAYPOINT_FONT.getHelp());
        final var gbcWaypointFontName = new GridBagConstraints();
        gbcWaypointFontName.fill = GridBagConstraints.HORIZONTAL;
        gbcWaypointFontName.gridx = 1;
        gbcWaypointFontName.gridy = 37;
        add(waypointFontSelector, gbcWaypointFontName);

        final var lblSpeedUnit = new JLabel(resourceBundle.getString("ui.panel.generalsettings.speedunit.label"));
        final var gbcLabelSpeedUnit = new GridBagConstraints();
        gbcLabelSpeedUnit.anchor = GridBagConstraints.LINE_END;
        gbcLabelSpeedUnit.insets = new Insets(0, 0, 5, 5);
        gbcLabelSpeedUnit.gridx = 0;
        gbcLabelSpeedUnit.gridy = 38;
        add(lblSpeedUnit, gbcLabelSpeedUnit);

        speedUnitComboBox = new JComboBox<>();
        speedUnitComboBox.setToolTipText(Option.SPEED_UNIT.getHelp());
        SpeedUnit.fillComboBox(speedUnitComboBox);
        final var gbcSpeedUnit = new GridBagConstraints();
        gbcSpeedUnit.fill = GridBagConstraints.HORIZONTAL;
        gbcSpeedUnit.gridx = 1;
        gbcSpeedUnit.gridy = 38;
        add(speedUnitComboBox, gbcSpeedUnit);
    }

    public void updateMaps(@NotNull final List<MapTemplate> mapTemplateList) {
        this.mapTemplateList = mapTemplateList;
        tmsUrlTemplateComboBox.setModel(new DefaultComboBoxModel<>(mapTemplateList.toArray(new MapTemplate[0])));
    }

    private FileSelector createImageFileSelector() {
         return new FileSelector(FILES_ONLY) {
            @Serial
            private static final long serialVersionUID = 7335143013605319472L;

            @Override
            protected Type configure(final JFileChooser imageFileChooser) {
                imageFileChooser.setAcceptAllFileFilterUsed(false);
                imageFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.image.format.all"),
                                "jpg", "png")); //NON-NLS
                imageFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.image.format.jpeg"),
                                "jpg")); //NON-NLS
                imageFileChooser.addChoosableFileFilter(
                        new FileNameExtensionFilter(resourceBundle.getString("ui.panel.generalsettings.image.format.png"),
                                "png")); //NON-NLS
                return Type.OPEN;
            }
        };

    }

    private void setVideoSize(final Integer width, final Integer height) {
        widthSpinner.setValue(width);
        heightSpinner.setValue(height);
    }

    private void setViewportSize(final Integer width, final Integer height) {
        viewportWidthSpinner.setValue(width);
        viewportHeightSpinner.setValue(height);
    }

    public void setConfiguration(final Configuration c) {
        heightSpinner.setValue(c.getHeight());
        widthSpinner.setValue(c.getWidth());
        viewportHeightSpinner.setValue(c.getViewportHeight());
        viewportWidthSpinner.setValue(c.getViewportWidth());
        viewportInertiaSpinner.setValue(c.getViewportInertia());
        marginSpinner.setValue(c.getMargin());
        logoMarginSpinner.setValue(c.getLogoMargin());
        informationMarginSpinner.setValue(c.getInformationMargin());
        attributionMarginSpinner.setValue(c.getAttributionMargin());
        commentMarginSpinner.setValue(c.getCommentMargin());
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
        photosDirectorySelector.setFilename(c.getPhotoDirectory());
        photoTimeSpinner.setValue(c.getPhotoTime());
        photoAnimationDurationSpinner.setValue(c.getPhotoAnimationDuration());

        final var tmsUrlTemplate = c.getTmsUrlTemplate();
        found:
        {
            if (tmsUrlTemplate != null) {
                for (final var mapTemplate : mapTemplateList) {
                    if (isEqual(mapTemplate.url(), tmsUrlTemplate)) {
                        tmsUrlTemplateComboBox.setSelectedItem(mapTemplate);
                        break found;
                    }
                }
            }
            tmsUrlTemplateComboBox.setSelectedItem(tmsUrlTemplate);
        }

        attributionTextArea.setText(c.getAttribution());
        preDrawTrackCheckBox.setSelected(c.isPreDrawTrack());
        skipIdleCheckBox.setSelected(c.isSkipIdle());
        if (c.getTailColor() != null) { // old saved files may not include this setting
            tailColorSelector.setColor(c.getTailColor());
        }
        outputFileSelector.setFilename(c.getOutput().toString());
        logoFileSelector.setFilename(c.getLogo() != null ? c.getLogo().toString() : "");
        fontSelector.setSelectedFont(c.getFont());
        markerSizeSpinner.setValue(c.getMarkerSize());
        waypointFontSelector.setSelectedFont(c.getWaypointFont());
        waypointSizeSpinner.setValue(c.getWaypointSize());
        backgroundColorSelector.setColor(c.getBackgroundColor());
        backgroundImageSelector.setFilename(c.getBackgroundImage() != null ? c.getBackgroundImage().toString() : "");
        flashbackColorSelector.setColor(c.getFlashbackColor());
        flashbackDurationSpinner.setValue(c.getFlashbackDuration());
        logoLocationComboBox.setSelectedItem(c.getLogoPosition() != null ? c.getLogoPosition() : Position.TOP_LEFT);
        attributionLocationComboBox.setSelectedItem(c.getAttributionPosition() != null ? c.getAttributionPosition() : Position.BOTTOM_LEFT);
        informationLocationComboBox.setSelectedItem(c.getInformationPosition() != null ? c.getInformationPosition() : Position.BOTTOM_RIGHT);
        commentLocationComboBox.setSelectedItem(c.getCommentPosition() != null ? c.getCommentPosition() : Position.BOTTOM_CENTER);
        speedUnitComboBox.setSelectedItem(c.getSpeedUnit() != null ? c.getSpeedUnit() : SpeedUnit.KMH);
    }


    public void buildConfiguration(final Configuration.Builder builder, final boolean replacePlaceholders) {
        final var td = (Long) tailDurationSpinner.getValue();
        final var tmsItem = tmsUrlTemplateComboBox.getSelectedItem();
        final var tmsUrlTemplate = tmsItem instanceof MapTemplate ? ((MapTemplate) tmsItem).url() : (String) tmsItem;
        final var attribution = generateAttributionText(replacePlaceholders, tmsItem);
        final var speedUnit = (SpeedUnit) speedUnitComboBox.getSelectedItem();

        builder.height((Integer) heightSpinner.getValue())
                .width((Integer) widthSpinner.getValue())
                .viewportHeight((Integer) viewportHeightSpinner.getValue())
                .viewportWidth((Integer) viewportWidthSpinner.getValue())
                .viewportInertia((Integer) viewportInertiaSpinner.getValue())
                .margin((Integer) marginSpinner.getValue())
                .logoMargin((Integer) logoMarginSpinner.getValue())
                .informationMargin((Integer) informationMarginSpinner.getValue())
                .attributionMargin((Integer) attributionMarginSpinner.getValue())
                .commentMargin((Integer) commentMarginSpinner.getValue())
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
                .logoPosition((Position) logoLocationComboBox.getSelectedItem())
                .informationPosition((Position) informationLocationComboBox.getSelectedItem())
                .commentPosition((Position) commentLocationComboBox.getSelectedItem())
                .preDrawTrack(preDrawTrackCheckBox.isSelected())
                .skipIdle(skipIdleCheckBox.isSelected())
                .backgroundColor(backgroundColorSelector.getColor())
                .backgroundImage(new File(backgroundImageSelector.getFilename()))
                .flashbackColor(flashbackColorSelector.getColor())
                .flashbackDuration((Long) flashbackDurationSpinner.getValue())
                .output(new File(outputFileSelector.getFilename()))
                .font(fontSelector.getSelectedFont())
                .markerSize((Double) markerSizeSpinner.getValue())
                .waypointFont(waypointFontSelector.getSelectedFont())
                .waypointSize((Double) waypointSizeSpinner.getValue())
                .logo(new File(logoFileSelector.getFilename()))
                .photoDirectory(photosDirectorySelector.getFilename())
                .photoTime((Long) photoTimeSpinner.getValue())
                .photoAnimationDuration((Long) photoAnimationDurationSpinner.getValue())
                .attribution(attribution)
                .attributionPosition((Position) attributionLocationComboBox.getSelectedItem())
                .speedUnit(speedUnit);
    }

    private String generateAttributionText(final boolean replacePlaceholders, final Object tmsItem) {
        if (!replacePlaceholders) {
            return attributionTextArea.getText().trim();
        }

        return attributionTextArea.getText()
                .replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION)  //NON-NLS
                .replace("%MAP_ATTRIBUTION%", //NON-NLS
                        tmsItem instanceof MapTemplate && ((MapTemplate) tmsItem).attributionText() != null
                                ? ((MapTemplate) tmsItem).attributionText() : "")
                .trim();
    }

    protected abstract void configurationChanged();

}
