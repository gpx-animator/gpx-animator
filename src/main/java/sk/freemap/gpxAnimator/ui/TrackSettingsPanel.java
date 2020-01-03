package sk.freemap.gpxAnimator.ui;

import sk.freemap.gpxAnimator.Option;
import sk.freemap.gpxAnimator.TrackConfiguration;
import sk.freemap.gpxAnimator.TrackConfiguration.Builder;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
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
import java.util.Vector;

import static javax.swing.JFileChooser.FILES_ONLY;

abstract class TrackSettingsPanel extends JPanel {

    private static final long serialVersionUID = 2492074184123083022L;

    private final transient JTextField labelTextField;
    private final transient JSpinner forcedPointTimeIntervalSpinner;
    private final transient JSpinner timeOffsetSpinner;
    private final transient JSpinner lineWidthSpinner;
    private final transient ColorSelector colorSelector;
    private final transient FileSelector inputGpxFileSelector;
    private final transient JSpinner trimGpxStartSpinner;
    private final transient JSpinner trimGpxEndSpinner;
    private final transient JComboBox<TrackIcon> trackIconComboBox;


    @SuppressWarnings("checkstyle:MethodLength") // TODO Refactor when doing the redesign task https://github.com/zdila/gpx-animator/issues/60
    TrackSettingsPanel() {
        setBounds(100, 100, 595, 419);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        final GridBagLayout gblContentPane = new GridBagLayout();
        gblContentPane.columnWidths = new int[]{0, 0, 0};
        gblContentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gblContentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gblContentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gblContentPane);

        final JLabel lblGpx = new JLabel("Input GPX File");
        final GridBagConstraints gbcLabelGpx = new GridBagConstraints();
        gbcLabelGpx.insets = new Insets(0, 0, 5, 5);
        gbcLabelGpx.anchor = GridBagConstraints.EAST;
        gbcLabelGpx.gridx = 0;
        gbcLabelGpx.gridy = 0;
        add(lblGpx, gbcLabelGpx);

        inputGpxFileSelector = new FileSelector(FILES_ONLY) {
            private static final long serialVersionUID = -7085193817022374995L;

            @Override
            protected Type configure(final JFileChooser gpxFileChooser) {
                gpxFileChooser.setAcceptAllFileFilterUsed(false);
                gpxFileChooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "All supported GPS files";
                    }

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory() || f.getName().endsWith(".gpx") || f.getName().endsWith(".gpx.gz");
                    }
                });
                gpxFileChooser.addChoosableFileFilter(new FileNameExtensionFilter("GPX Files", "gpx"));
                gpxFileChooser.addChoosableFileFilter(new FileFilter() {
                    @Override
                    public String getDescription() {
                        return "GZipped GPX Files";
                    }

                    @Override
                    public boolean accept(final File f) {
                        return f.isDirectory() || f.getName().endsWith(".gpx.gz");
                    }
                });
                return Type.OPEN;
            }
        };

        inputGpxFileSelector.setToolTipText(Option.INPUT.getHelp());
        lblGpx.setLabelFor(inputGpxFileSelector);
        final GridBagConstraints gbcInputGpxFileSelector = new GridBagConstraints();
        gbcInputGpxFileSelector.insets = new Insets(0, 0, 5, 0);
        gbcInputGpxFileSelector.fill = GridBagConstraints.BOTH;
        gbcInputGpxFileSelector.gridx = 1;
        gbcInputGpxFileSelector.gridy = 0;
        add(inputGpxFileSelector, gbcInputGpxFileSelector);

        final JLabel lblLabel = new JLabel("Label");
        final GridBagConstraints gbcLabelLabel = new GridBagConstraints();
        gbcLabelLabel.anchor = GridBagConstraints.EAST;
        gbcLabelLabel.insets = new Insets(0, 0, 5, 5);
        gbcLabelLabel.gridx = 0;
        gbcLabelLabel.gridy = 1;
        add(lblLabel, gbcLabelLabel);

        labelTextField = new JTextField();
        labelTextField.setToolTipText(Option.LABEL.getHelp());
        lblLabel.setLabelFor(labelTextField);
        final GridBagConstraints gbcLabelTextField = new GridBagConstraints();
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

        final JLabel lblColor1 = new JLabel("Color");
        final GridBagConstraints gbcLabelColor1 = new GridBagConstraints();
        gbcLabelColor1.anchor = GridBagConstraints.EAST;
        gbcLabelColor1.insets = new Insets(0, 0, 5, 5);
        gbcLabelColor1.gridx = 0;
        gbcLabelColor1.gridy = 2;
        add(lblColor1, gbcLabelColor1);

        colorSelector = new ColorSelector();
        colorSelector.setToolTipText(Option.COLOR.getHelp());
        final GridBagConstraints gbcColorSelector = new GridBagConstraints();
        gbcColorSelector.insets = new Insets(0, 0, 5, 0);
        gbcColorSelector.fill = GridBagConstraints.BOTH;
        gbcColorSelector.gridx = 1;
        gbcColorSelector.gridy = 2;
        add(colorSelector, gbcColorSelector);

        final JLabel lblLineWidth = new JLabel("Line Width");
        final GridBagConstraints gbcLabelLineWidth = new GridBagConstraints();
        gbcLabelLineWidth.insets = new Insets(0, 0, 5, 5);
        gbcLabelLineWidth.anchor = GridBagConstraints.EAST;
        gbcLabelLineWidth.gridx = 0;
        gbcLabelLineWidth.gridy = 3;
        add(lblLineWidth, gbcLabelLineWidth);

        lineWidthSpinner = new JSpinner();
        lineWidthSpinner.setToolTipText(Option.LINE_WIDTH.getHelp());
        lineWidthSpinner.setModel(new SpinnerNumberModel(0f, 0f, null, 0.5f));
        final GridBagConstraints gbcLineWidthSpinner = new GridBagConstraints();
        gbcLineWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcLineWidthSpinner.insets = new Insets(0, 0, 5, 0);
        gbcLineWidthSpinner.gridx = 1;
        gbcLineWidthSpinner.gridy = 3;
        add(lineWidthSpinner, gbcLineWidthSpinner);

        final JLabel lblTimeOffset = new JLabel("Time Offset");
        final GridBagConstraints gbcLabelTimeOffset = new GridBagConstraints();
        gbcLabelTimeOffset.anchor = GridBagConstraints.EAST;
        gbcLabelTimeOffset.insets = new Insets(0, 0, 5, 5);
        gbcLabelTimeOffset.gridx = 0;
        gbcLabelTimeOffset.gridy = 4;
        add(lblTimeOffset, gbcLabelTimeOffset);

        timeOffsetSpinner = new JSpinner();
        timeOffsetSpinner.setToolTipText(Option.TIME_OFFSET.getHelp());
        timeOffsetSpinner.setModel(new DurationSpinnerModel());
        timeOffsetSpinner.setEditor(new DurationEditor(timeOffsetSpinner));
        final GridBagConstraints gbcTimeOffsetSpinner = new GridBagConstraints();
        gbcTimeOffsetSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTimeOffsetSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTimeOffsetSpinner.gridx = 1;
        gbcTimeOffsetSpinner.gridy = 4;
        add(timeOffsetSpinner, gbcTimeOffsetSpinner);

        final JLabel lblForcedPointTime = new JLabel("Forced Point Time Interval");
        final GridBagConstraints gbcLabelForcedPointTime = new GridBagConstraints();
        gbcLabelForcedPointTime.anchor = GridBagConstraints.EAST;
        gbcLabelForcedPointTime.insets = new Insets(0, 0, 5, 5);
        gbcLabelForcedPointTime.gridx = 0;
        gbcLabelForcedPointTime.gridy = 5;
        add(lblForcedPointTime, gbcLabelForcedPointTime);

        forcedPointTimeIntervalSpinner = new JSpinner();
        forcedPointTimeIntervalSpinner.setToolTipText(Option.FORCED_POINT_TIME_INTERVAL.getHelp());
        forcedPointTimeIntervalSpinner.setModel(new DurationSpinnerModel());
        forcedPointTimeIntervalSpinner.setEditor(new DurationEditor(forcedPointTimeIntervalSpinner));
        final GridBagConstraints gbcForcedPointTimeIntervalSpinner = new GridBagConstraints();
        gbcForcedPointTimeIntervalSpinner.insets = new Insets(0, 0, 5, 0);
        gbcForcedPointTimeIntervalSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcForcedPointTimeIntervalSpinner.gridx = 1;
        gbcForcedPointTimeIntervalSpinner.gridy = 5;
        add(forcedPointTimeIntervalSpinner, gbcForcedPointTimeIntervalSpinner);

        final JLabel lblTrimGpxStart = new JLabel("Trim Start of GPX File");
        final GridBagConstraints gbcLabelTrimGpxStart = new GridBagConstraints();
        gbcLabelTrimGpxStart.anchor = GridBagConstraints.EAST;
        gbcLabelTrimGpxStart.insets = new Insets(0, 0, 5, 5);
        gbcLabelTrimGpxStart.gridx = 0;
        gbcLabelTrimGpxStart.gridy = 6;
        add(lblTrimGpxStart, gbcLabelTrimGpxStart);

        trimGpxStartSpinner = new JSpinner();
        trimGpxStartSpinner.setToolTipText(Option.TRIM_GPX_START.getHelp());
        trimGpxStartSpinner.setModel(new DurationSpinnerModel());
        trimGpxStartSpinner.setEditor(new DurationEditor(trimGpxStartSpinner));
        final GridBagConstraints gbcTrimGpxStartSpinner = new GridBagConstraints();
        gbcTrimGpxStartSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTrimGpxStartSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTrimGpxStartSpinner.gridx = 1;
        gbcTrimGpxStartSpinner.gridy = 6;
        add(trimGpxStartSpinner, gbcTrimGpxStartSpinner);

        final JLabel lblTrimGpxEnd = new JLabel("Trim End of GPX File");
        final GridBagConstraints gbcLabelTrimGpxEnd = new GridBagConstraints();
        gbcLabelTrimGpxEnd.anchor = GridBagConstraints.EAST;
        gbcLabelTrimGpxEnd.insets = new Insets(0, 0, 5, 5);
        gbcLabelTrimGpxEnd.gridx = 0;
        gbcLabelTrimGpxEnd.gridy = 7;
        add(lblTrimGpxEnd, gbcLabelTrimGpxEnd);

        trimGpxEndSpinner = new JSpinner();
        trimGpxEndSpinner.setToolTipText(Option.TRIM_GPX_END.getHelp());
        trimGpxEndSpinner.setModel(new DurationSpinnerModel());
        trimGpxEndSpinner.setEditor(new DurationEditor(trimGpxEndSpinner));
        final GridBagConstraints gbcTrimGpxEndSpinner = new GridBagConstraints();
        gbcTrimGpxEndSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbcTrimGpxEndSpinner.insets = new Insets(0, 0, 5, 0);
        gbcTrimGpxEndSpinner.gridx = 1;
        gbcTrimGpxEndSpinner.gridy = 7;
        add(trimGpxEndSpinner, gbcTrimGpxEndSpinner);

        final Vector<TrackIcon> trackIcons = new Vector<>();
        trackIcons.add(new TrackIcon(""));
        trackIcons.add(new TrackIcon("Airplane"));
        trackIcons.add(new TrackIcon("Bicycle"));
        trackIcons.add(new TrackIcon("Bus"));
        trackIcons.add(new TrackIcon("Car"));
        trackIcons.add(new TrackIcon("Jogging"));
        trackIcons.add(new TrackIcon("Riding"));
        trackIcons.add(new TrackIcon("Sailing"));
        trackIcons.add(new TrackIcon("Ship"));
        trackIcons.add(new TrackIcon("Train"));
        trackIcons.add(new TrackIcon("Tramway"));
        trackIcons.add(new TrackIcon("Trekking"));

        trackIconComboBox = new JComboBox<>(trackIcons);
        trackIconComboBox.setToolTipText(Option.TRACK_ICON.getHelp());
        trackIconComboBox.setEditable(false);
        final GridBagConstraints gbcTrackIconComboBox = new GridBagConstraints();
        gbcTrackIconComboBox.fill = GridBagConstraints.HORIZONTAL;
        gbcTrackIconComboBox.insets = new Insets(0, 0, 5, 0);
        gbcTrackIconComboBox.gridx = 1;
        gbcTrackIconComboBox.gridy = 8;
        add(trackIconComboBox, gbcTrackIconComboBox);
        trackIconComboBox.setPreferredSize(new Dimension(10, trackIconComboBox.getPreferredSize().height));

        final JLabel lblEnableIcon = new JLabel("Choose Icon");
        final GridBagConstraints gbcLabelEnableIcon = new GridBagConstraints();
        gbcLabelEnableIcon.anchor = GridBagConstraints.EAST;
        gbcLabelEnableIcon.insets = new Insets(0, 0, 5, 5);
        gbcLabelEnableIcon.gridx = 0;
        gbcLabelEnableIcon.gridy = 8;
        add(lblEnableIcon, gbcLabelEnableIcon);

        final JButton btnNewButton = new JButton("Remove Track");
        btnNewButton.addActionListener(e -> remove());
        final GridBagConstraints gbcButtonNewButton = new GridBagConstraints();
        gbcButtonNewButton.anchor = GridBagConstraints.EAST;
        gbcButtonNewButton.gridwidth = 3;
        gbcButtonNewButton.insets = new Insets(0, 0, 0, 5);
        gbcButtonNewButton.gridx = 0;
        gbcButtonNewButton.gridy = 9;
        add(btnNewButton, gbcButtonNewButton);

        final PropertyChangeListener propertyChangeListener = evt -> configurationChanged();
        inputGpxFileSelector.addPropertyChangeListener("filename", propertyChangeListener);

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

        colorSelector.addPropertyChangeListener("color", propertyChangeListener);

        final ChangeListener changeListener = e -> configurationChanged();

        lineWidthSpinner.addChangeListener(changeListener);
        timeOffsetSpinner.addChangeListener(changeListener);
        forcedPointTimeIntervalSpinner.addChangeListener(changeListener);
        trimGpxStartSpinner.addChangeListener(changeListener);
        trimGpxEndSpinner.addChangeListener(changeListener);
        trackIconComboBox.addItemListener(e -> configurationChanged());
    }


    protected abstract void labelChanged(String label);


    public TrackConfiguration createConfiguration() {
        final Builder b = TrackConfiguration.createBuilder();

        b.inputGpx(new File(inputGpxFileSelector.getFilename()));
        b.label(labelTextField.getText());
        b.color(colorSelector.getColor());
        b.lineWidth((Float) lineWidthSpinner.getValue());
        b.forcedPointInterval((Long) forcedPointTimeIntervalSpinner.getValue());
        b.timeOffset((Long) timeOffsetSpinner.getValue());
        b.trimGpxStart((Long) trimGpxStartSpinner.getValue());
        b.trimGpxEnd((Long) trimGpxEndSpinner.getValue());
        b.trackIcon((TrackIcon) trackIconComboBox.getSelectedItem());
        return b.build();
    }


    public void setConfiguration(final TrackConfiguration c) {
        inputGpxFileSelector.setFilename(c.getInputGpx() == null ? null : c.getInputGpx().toString());
        labelTextField.setText(c.getLabel());
        colorSelector.setColor(c.getColor());
        lineWidthSpinner.setValue(c.getLineWidth());
        forcedPointTimeIntervalSpinner.setValue(c.getForcedPointInterval());
        timeOffsetSpinner.setValue(c.getTimeOffset());
        trimGpxStartSpinner.setValue(c.getTrimGpxStart());
        trimGpxEndSpinner.setValue(c.getTrimGpxEnd());
        trackIconComboBox.setSelectedItem(c.getTrackIcon());
        labelChanged(c.getLabel());
    }


    protected abstract void remove();

    protected abstract void configurationChanged();

}
