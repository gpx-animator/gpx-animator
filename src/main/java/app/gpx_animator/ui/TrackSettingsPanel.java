package app.gpx_animator.ui;

import app.gpx_animator.Option;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.TrackIcon;
import app.gpx_animator.core.configuration.TrackConfiguration;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.Serial;
import java.util.ResourceBundle;

import static javax.swing.JFileChooser.FILES_ONLY;

abstract class TrackSettingsPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 2492074184123083022L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient JTextArea labelTextField;
    private final transient JSpinner forcedPointTimeIntervalSpinner;
    private final transient JSpinner timeOffsetSpinner;
    private final transient JSpinner lineWidthSpinner;
    private final transient ColorSelector colorSelector;
    private final transient ColorSelector preDrawColorSelector;
    private final transient FileSelector inputGpxFileSelector;
    private final transient JSpinner trimGpxStartSpinner;
    private final transient JSpinner trimGpxEndSpinner;
    private final transient JComboBox<TrackIcon> trackIconComboBox;
    private final transient FileSelector inputIconFileSelector;
    private final transient JCheckBox mirrorIconCheckBox;

    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    TrackSettingsPanel() {
        setBounds(100, 100, 595, 419);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        final var gblContentPane = new GridBagLayout();
        gblContentPane.columnWidths = new int[]     {0,   0,   0};
        gblContentPane.columnWeights = new double[] {0.0, 1.0, Double.MIN_VALUE};
        gblContentPane.rowHeights = new int[]       {0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0,   0};
        gblContentPane.rowWeights = new double[]    {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gblContentPane);

        final var lblGpx = new JLabel(resourceBundle.getString("ui.panel.tracksettings.gpxfile.label"));
        final var gbcLabelGpx = new GridBagConstraints();
        gbcLabelGpx.insets = new Insets(0, 0, 5, 5);
        gbcLabelGpx.anchor = GridBagConstraints.LINE_END;
        gbcLabelGpx.gridx = 0;
        gbcLabelGpx.gridy = 0;
        add(lblGpx, gbcLabelGpx);

        inputGpxFileSelector = new FileSelector(FILES_ONLY) {
            @Serial
            private static final long serialVersionUID = -7085193817022374995L;

            @Override
            protected Type configure(final JFileChooser gpxFileChooser) {
                configureGpxFileChooser(resourceBundle, gpxFileChooser);
                return Type.OPEN;
            }
        };

        inputGpxFileSelector.setToolTipText(Option.INPUT.getHelp());
        lblGpx.setLabelFor(inputGpxFileSelector);
        final var gbcInputGpxFileSelector = new GridBagConstraints();
        gbcInputGpxFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcInputGpxFileSelector.fill = GridBagConstraints.BOTH;
        gbcInputGpxFileSelector.gridx = 1;
        gbcInputGpxFileSelector.gridy = 0;
        add(inputGpxFileSelector, gbcInputGpxFileSelector);

        final var lblLabel = new JLabel(resourceBundle.getString("ui.panel.tracksettings.label.label"));
        final var gbcLabelLabel = new GridBagConstraints();
        gbcLabelLabel.anchor = GridBagConstraints.LINE_END;
        gbcLabelLabel.insets = new Insets(0, 0, 5, 5);
        gbcLabelLabel.gridx = 0;
        gbcLabelLabel.gridy = 1;
        add(lblLabel, gbcLabelLabel);

        labelTextField = new JTextArea();
        labelTextField.setToolTipText(Option.LABEL.getHelp());
        lblLabel.setLabelFor(labelTextField);
        final var gbcLabelTextField = new GridBagConstraints();
        gbcLabelTextField.insets = new Insets(0, 0, 5, 0);
        gbcLabelTextField.fill = GridBagConstraints.HORIZONTAL;
        gbcLabelTextField.gridx = 1;
        gbcLabelTextField.gridy = 1;
        add(labelTextField, gbcLabelTextField);
        labelTextField.setColumns(10);
        labelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent documentEvent) {
                labelChanged();
            }

            @Override
            public void insertUpdate(final DocumentEvent documentEvent) {
                labelChanged();
            }

            @Override
            public void changedUpdate(final DocumentEvent documentEvent) {
                labelChanged();
            }

            private void labelChanged() {
                TrackSettingsPanel.this.labelChanged(labelTextField.getText());
            }
        });

        final var lblColor = new JLabel(resourceBundle.getString("ui.panel.tracksettings.color.label"));
        final var gbcLabelColor = new GridBagConstraints();
        gbcLabelColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelColor.gridx = 0;
        gbcLabelColor.gridy = 2;
        add(lblColor, gbcLabelColor);

        colorSelector = new ColorSelector();
        colorSelector.setToolTipText(Option.COLOR.getHelp());
        final var gbcColorSelector = new GridBagConstraints();
        gbcColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcColorSelector.fill = GridBagConstraints.BOTH;
        gbcColorSelector.gridx = 1;
        gbcColorSelector.gridy = 2;
        add(colorSelector, gbcColorSelector);

        final var lblPreDrawColor = new JLabel(resourceBundle.getString("ui.panel.tracksettings.predrawcolor.label"));
        final var gbcLabelPreDrawColor = new GridBagConstraints();
        gbcLabelPreDrawColor.anchor = GridBagConstraints.LINE_END;
        gbcLabelPreDrawColor.insets = new Insets(0, 0, 5, 5);
        gbcLabelPreDrawColor.gridx = 0;
        gbcLabelPreDrawColor.gridy = 3;
        add(lblPreDrawColor, gbcLabelPreDrawColor);

        preDrawColorSelector = new ColorSelector();
        preDrawColorSelector.setToolTipText(Option.PRE_DRAW_TRACK_COLOR.getHelp());
        final var gbcPreDrawColorSelector = new GridBagConstraints();
        gbcPreDrawColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcPreDrawColorSelector.fill = GridBagConstraints.BOTH;
        gbcPreDrawColorSelector.gridx = 1;
        gbcPreDrawColorSelector.gridy = 3;
        add(preDrawColorSelector, gbcPreDrawColorSelector);

        final var lblLineWidth = new JLabel(resourceBundle.getString("ui.panel.tracksettings.linewidth.label"));
        final var gbcLabelLineWidth = new GridBagConstraints();
        gbcLabelLineWidth.insets = new Insets(0, 0, 5, 5);
        gbcLabelLineWidth.anchor = GridBagConstraints.LINE_END;
        gbcLabelLineWidth.gridx = 0;
        gbcLabelLineWidth.gridy = 4;
        add(lblLineWidth, gbcLabelLineWidth);

        lineWidthSpinner = new JSpinner();
        lineWidthSpinner.setToolTipText(Option.LINE_WIDTH.getHelp());
        lineWidthSpinner.setModel(new SpinnerNumberModel(0f, 0f, null, 0.5f));
        final var gbcLineWidthSpinner = new GridBagConstraints();
        gbcLineWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcLineWidthSpinner.insets = new Insets(0, 0, 5, 0);
        gbcLineWidthSpinner.gridx = 1;
        gbcLineWidthSpinner.gridy = 4;
        add(lineWidthSpinner, gbcLineWidthSpinner);

        final var lblTimeOffset = new JLabel(resourceBundle.getString("ui.panel.tracksettings.timeoffset.label"));
        final var gbcLabelTimeOffset = new GridBagConstraints();
        gbcLabelTimeOffset.anchor = GridBagConstraints.LINE_END;
        gbcLabelTimeOffset.insets = new Insets(0, 0, 5, 5);
        gbcLabelTimeOffset.gridx = 0;
        gbcLabelTimeOffset.gridy = 5;
        add(lblTimeOffset, gbcLabelTimeOffset);

        timeOffsetSpinner = new JSpinner();
        timeOffsetSpinner.setToolTipText(Option.TIME_OFFSET.getHelp());
        timeOffsetSpinner.setModel(new DurationSpinnerModel());
        timeOffsetSpinner.setEditor(new DurationEditor(timeOffsetSpinner));
        final var gbcTimeOffsetSpinner = new GridBagConstraints();
        gbcTimeOffsetSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTimeOffsetSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTimeOffsetSpinner.gridx = 1;
        gbcTimeOffsetSpinner.gridy = 5;
        add(timeOffsetSpinner, gbcTimeOffsetSpinner);

        final var lblForcedPointTime = new JLabel(resourceBundle.getString("ui.panel.tracksettings.timepointinterval.label"));
        final var gbcLabelForcedPointTime = new GridBagConstraints();
        gbcLabelForcedPointTime.anchor = GridBagConstraints.LINE_END;
        gbcLabelForcedPointTime.insets = new Insets(0, 0, 5, 5);
        gbcLabelForcedPointTime.gridx = 0;
        gbcLabelForcedPointTime.gridy = 6;
        add(lblForcedPointTime, gbcLabelForcedPointTime);

        forcedPointTimeIntervalSpinner = new JSpinner();
        forcedPointTimeIntervalSpinner.setToolTipText(Option.FORCED_POINT_TIME_INTERVAL.getHelp());
        forcedPointTimeIntervalSpinner.setModel(new DurationSpinnerModel());
        forcedPointTimeIntervalSpinner.setEditor(new DurationEditor(forcedPointTimeIntervalSpinner));
        final var gbcForcedPointTimeIntervalSpinner = new GridBagConstraints();
        gbcForcedPointTimeIntervalSpinner.insets = new Insets(0, 0, 5, 0);
        gbcForcedPointTimeIntervalSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcForcedPointTimeIntervalSpinner.gridx = 1;
        gbcForcedPointTimeIntervalSpinner.gridy = 6;
        add(forcedPointTimeIntervalSpinner, gbcForcedPointTimeIntervalSpinner);

        final var lblTrimGpxStart = new JLabel(resourceBundle.getString("ui.panel.tracksettings.trimstart.label"));
        final var gbcLabelTrimGpxStart = new GridBagConstraints();
        gbcLabelTrimGpxStart.anchor = GridBagConstraints.LINE_END;
        gbcLabelTrimGpxStart.insets = new Insets(0, 0, 5, 5);
        gbcLabelTrimGpxStart.gridx = 0;
        gbcLabelTrimGpxStart.gridy = 7;
        add(lblTrimGpxStart, gbcLabelTrimGpxStart);

        trimGpxStartSpinner = new JSpinner();
        trimGpxStartSpinner.setToolTipText(Option.TRIM_GPX_START.getHelp());
        trimGpxStartSpinner.setModel(new DurationSpinnerModel());
        trimGpxStartSpinner.setEditor(new DurationEditor(trimGpxStartSpinner));
        final var gbcTrimGpxStartSpinner = new GridBagConstraints();
        gbcTrimGpxStartSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTrimGpxStartSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTrimGpxStartSpinner.gridx = 1;
        gbcTrimGpxStartSpinner.gridy = 7;
        add(trimGpxStartSpinner, gbcTrimGpxStartSpinner);

        final var lblTrimGpxEnd = new JLabel(resourceBundle.getString("ui.panel.tracksettings.trimend.label"));
        final var gbcLabelTrimGpxEnd = new GridBagConstraints();
        gbcLabelTrimGpxEnd.anchor = GridBagConstraints.LINE_END;
        gbcLabelTrimGpxEnd.insets = new Insets(0, 0, 5, 5);
        gbcLabelTrimGpxEnd.gridx = 0;
        gbcLabelTrimGpxEnd.gridy = 8;
        add(lblTrimGpxEnd, gbcLabelTrimGpxEnd);

        trimGpxEndSpinner = new JSpinner();
        trimGpxEndSpinner.setToolTipText(Option.TRIM_GPX_END.getHelp());
        trimGpxEndSpinner.setModel(new DurationSpinnerModel());
        trimGpxEndSpinner.setEditor(new DurationEditor(trimGpxEndSpinner));
        final var gbcTrimGpxEndSpinner = new GridBagConstraints();
        gbcTrimGpxEndSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTrimGpxEndSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTrimGpxEndSpinner.gridx = 1;
        gbcTrimGpxEndSpinner.gridy = 8;
        add(trimGpxEndSpinner, gbcTrimGpxEndSpinner);

        trackIconComboBox = new JComboBox<>(TrackIcon.getAllTrackIcons());
        trackIconComboBox.setToolTipText(Option.TRACK_ICON.getHelp());
        trackIconComboBox.setEditable(false);
        final var gbcTrackIconComboBox = new GridBagConstraints();
        gbcTrackIconComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcTrackIconComboBox.insets = new Insets(0, 0, 5, 0);
        gbcTrackIconComboBox.gridx = 1;
        gbcTrackIconComboBox.gridy = 9;
        add(trackIconComboBox, gbcTrackIconComboBox);
        trackIconComboBox.setPreferredSize(new Dimension(10, trackIconComboBox.getPreferredSize().height));

        final var lblEnableIcon = new JLabel(resourceBundle.getString("ui.panel.tracksettings.icon.label"));
        final var gbcLabelEnableIcon = new GridBagConstraints();
        gbcLabelEnableIcon.anchor = GridBagConstraints.LINE_END;
        gbcLabelEnableIcon.insets = new Insets(0, 0, 5, 5);
        gbcLabelEnableIcon.gridx = 0;
        gbcLabelEnableIcon.gridy = 9;
        add(lblEnableIcon, gbcLabelEnableIcon);

        final var lblIcon = new JLabel(resourceBundle.getString("ui.panel.tracksettings.iconfile.label"));
        final var gbcLabelIcon = new GridBagConstraints();
        gbcLabelIcon.insets = new Insets(0, 0, 5, 5);
        gbcLabelIcon.anchor = GridBagConstraints.LINE_END;
        gbcLabelIcon.gridx = 0;
        gbcLabelIcon.gridy = 10;
        add(lblIcon, gbcLabelIcon);

        inputIconFileSelector = new FileSelector(FILES_ONLY) {
            @Serial
            private static final long serialVersionUID = -7085193817022374995L;

            @Override
            protected Type configure(final JFileChooser iconFileChooser) {
                configureIconFileChooser(resourceBundle, iconFileChooser);
                return Type.OPEN;
            }
        };

        inputIconFileSelector.setToolTipText(Option.INPUT.getHelp());
        lblIcon.setLabelFor(inputIconFileSelector);
        final var gbcInputIconFileSelector = new GridBagConstraints();
        gbcInputIconFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcInputIconFileSelector.fill = GridBagConstraints.BOTH;
        gbcInputIconFileSelector.gridx = 1;
        gbcInputIconFileSelector.gridy = 10;
        add(inputIconFileSelector, gbcInputIconFileSelector);

        final var lblMirrorIcon = new JLabel(resourceBundle.getString("ui.panel.tracksettings.icon.mirror.label"));
        final var gbcLabelMirrorIcon = new GridBagConstraints();
        gbcLabelMirrorIcon.anchor = GridBagConstraints.LINE_END;
        gbcLabelMirrorIcon.insets = new Insets(0, 0, 5, 5);
        gbcLabelMirrorIcon.gridx = 0;
        gbcLabelMirrorIcon.gridy = 11;
        add(lblMirrorIcon, gbcLabelMirrorIcon);

        mirrorIconCheckBox = new JCheckBox("");
        mirrorIconCheckBox.setToolTipText(Option.TRACK_ICON_MIRROR.getHelp());
        final var gbcMirrorIconCheckBox = new GridBagConstraints();
        gbcMirrorIconCheckBox.fill = GridBagConstraints.HORIZONTAL;
        gbcMirrorIconCheckBox.insets = new Insets(0, 0, 5, 5);
        gbcMirrorIconCheckBox.gridx = 1;
        gbcMirrorIconCheckBox.gridy = 11;
        add(mirrorIconCheckBox, gbcMirrorIconCheckBox);

        final var btnNewButton = new JButton(resourceBundle.getString("ui.panel.tracksettings.button.remove"));
        btnNewButton.addActionListener(e -> remove());
        final var gbcButtonNewButton = new GridBagConstraints();
        gbcButtonNewButton.anchor = GridBagConstraints.LINE_END;
        gbcButtonNewButton.gridwidth = 3;
        gbcButtonNewButton.insets = new Insets(0, 0, 0, 5);
        gbcButtonNewButton.gridx = 0;
        gbcButtonNewButton.gridy = 12;
        add(btnNewButton, gbcButtonNewButton);

        final PropertyChangeListener propertyChangeListener = evt -> configurationChanged();
        inputGpxFileSelector.addPropertyChangeListener(FileSelector.PROPERTY_FILENAME, propertyChangeListener);

        labelTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void removeUpdate(final DocumentEvent e) {
                configurationChanged();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                configurationChanged();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                configurationChanged();
            }
        });

        colorSelector.addPropertyChangeListener(ColorSelector.PROPERTY_COLOR, propertyChangeListener);
        preDrawColorSelector.addPropertyChangeListener(ColorSelector.PROPERTY_COLOR, propertyChangeListener);

        final ChangeListener changeListener = e -> configurationChanged();

        lineWidthSpinner.addChangeListener(changeListener);
        timeOffsetSpinner.addChangeListener(changeListener);
        forcedPointTimeIntervalSpinner.addChangeListener(changeListener);
        trimGpxStartSpinner.addChangeListener(changeListener);
        trimGpxEndSpinner.addChangeListener(changeListener);
        trackIconComboBox.addItemListener(e -> configurationChanged());
        mirrorIconCheckBox.addChangeListener(changeListener);
    }

    public static void configureGpxFileChooser(final ResourceBundle resourceBundle, final JFileChooser fileChooser) {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return resourceBundle.getString("ui.panel.tracksettings.gpxfile.format.all");
            }

            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().endsWith(".gpx") || f.getName().endsWith(".gpx.gz"); //NON-NLS
            }
        });
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                resourceBundle.getString("ui.panel.tracksettings.gpxfile.format.gpx"), "gpx")); //NON-NLS
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return resourceBundle.getString("ui.panel.tracksettings.gpxfile.format.gpxgz");
            }

            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().endsWith(".gpx.gz"); //NON-NLS
            }
        });
    }

    public static void configureIconFileChooser(final ResourceBundle resourceBundle, final JFileChooser fileChooser) {
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setAcceptAllFileFilterUsed(false);
        fileChooser.addChoosableFileFilter(new FileFilter() {
            @Override
            public String getDescription() {
                return resourceBundle.getString("ui.panel.tracksettings.iconfile.format.all");
            }

            @Override
            public boolean accept(final File f) {
                return f.isDirectory() || f.getName().endsWith(".png"); //NON-NLS
            }
        });
        fileChooser.addChoosableFileFilter(new FileNameExtensionFilter(
                resourceBundle.getString("ui.panel.tracksettings.iconfile.format.png"), "png")); //NON-NLS
    }

    protected abstract void labelChanged(String label);


    public TrackConfiguration createConfiguration() {
        final var b = TrackConfiguration.createBuilder();

        b.inputGpx(new File(inputGpxFileSelector.getFilename()));
        b.label(labelTextField.getText());
        b.color(colorSelector.getColor());
        b.preDrawTrackColor(preDrawColorSelector.getColor());
        b.lineWidth((Float) lineWidthSpinner.getValue());
        b.forcedPointInterval((Long) forcedPointTimeIntervalSpinner.getValue());
        b.timeOffset((Long) timeOffsetSpinner.getValue());
        b.trimGpxStart((Long) trimGpxStartSpinner.getValue());
        b.trimGpxEnd((Long) trimGpxEndSpinner.getValue());
        b.trackIcon((TrackIcon) trackIconComboBox.getSelectedItem());
        b.inputIcon(new File(inputIconFileSelector.getFilename()));
        b.mirrorTrackIcon(mirrorIconCheckBox.isSelected());
        return b.build();
    }


    public void setConfiguration(final TrackConfiguration c) {
        inputGpxFileSelector.setFilename(c.getInputGpx() == null ? null : c.getInputGpx().toString());
        labelTextField.setText(c.getLabel());
        colorSelector.setColor(c.getColor());
        preDrawColorSelector.setColor(c.getPreDrawTrackColor());
        lineWidthSpinner.setValue(c.getLineWidth());
        forcedPointTimeIntervalSpinner.setValue(c.getForcedPointInterval());
        timeOffsetSpinner.setValue(c.getTimeOffset());
        trimGpxStartSpinner.setValue(c.getTrimGpxStart());
        trimGpxEndSpinner.setValue(c.getTrimGpxEnd());
        trackIconComboBox.setSelectedItem(c.getTrackIcon());
        inputIconFileSelector.setFilename(c.getInputIcon() == null ? null : c.getInputIcon().toString());
        mirrorIconCheckBox.setSelected(c.isTrackIconMirrored());
        labelChanged(c.getLabel());
    }


    protected abstract void remove();

    protected abstract void configurationChanged();

}
