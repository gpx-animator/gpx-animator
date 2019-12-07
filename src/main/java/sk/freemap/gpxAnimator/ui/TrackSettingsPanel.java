package sk.freemap.gpxAnimator.ui;

import sk.freemap.gpxAnimator.Option;
import sk.freemap.gpxAnimator.TrackConfiguration;
import sk.freemap.gpxAnimator.TrackConfiguration.Builder;
import sk.freemap.gpxAnimator.UserException;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import static javax.swing.JFileChooser.FILES_ONLY;

abstract class TrackSettingsPanel extends JPanel {

    private static final long serialVersionUID = 2492074184123083022L;
    private final JLabel lblLabel;
    private final JTextField labelTextField;
    private final JLabel lblLineWidth;
    private final JLabel lblTimeOffset;
    private final JLabel lblForcedPointTime;
    private final JSpinner forcedPointTimeIntervalSpinner;
    private final JSpinner timeOffsetSpinner;
    private final JSpinner lineWidthSpinner;
    private final JLabel lblColor_1;
    private final ColorSelector colorSelector;
    private final FileSelector inputGpxFileSelector;
    private final JLabel lblTrimGpxStart;
    private final JSpinner trimGpxStartSpinner;
    private final JLabel lblTrimGpxEnd;
    private final JSpinner trimGpxEndSpinner;
    private final JCheckBox enableIconCheckBox;
    private final JLabel lblEnableIcon;


    public TrackSettingsPanel() {
        setBounds(100, 100, 595, 419);
        setBorder(new EmptyBorder(5, 5, 5, 5));
        final GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{0, 0, 0};
        gbl_contentPane.rowHeights = new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        gbl_contentPane.columnWeights = new double[]{0.0, 1.0, Double.MIN_VALUE};
        gbl_contentPane.rowWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE};
        setLayout(gbl_contentPane);

        final JLabel lblGpx = new JLabel("Input GPX File");
        final GridBagConstraints gbc_lblGpx = new GridBagConstraints();
        gbc_lblGpx.insets = new Insets(0, 0, 5, 5);
        gbc_lblGpx.anchor = GridBagConstraints.EAST;
        gbc_lblGpx.gridx = 0;
        gbc_lblGpx.gridy = 0;
        add(lblGpx, gbc_lblGpx);

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
        final GridBagConstraints gbc_inputGpxFileSelector = new GridBagConstraints();
        gbc_inputGpxFileSelector.insets = new Insets(0, 0, 5, 0);
        gbc_inputGpxFileSelector.fill = GridBagConstraints.BOTH;
        gbc_inputGpxFileSelector.gridx = 1;
        gbc_inputGpxFileSelector.gridy = 0;
        add(inputGpxFileSelector, gbc_inputGpxFileSelector);

        lblLabel = new JLabel("Label");
        final GridBagConstraints gbc_lblLabel = new GridBagConstraints();
        gbc_lblLabel.anchor = GridBagConstraints.EAST;
        gbc_lblLabel.insets = new Insets(0, 0, 5, 5);
        gbc_lblLabel.gridx = 0;
        gbc_lblLabel.gridy = 1;
        add(lblLabel, gbc_lblLabel);

        labelTextField = new JTextField();
        labelTextField.setToolTipText(Option.LABEL.getHelp());
        lblLabel.setLabelFor(labelTextField);
        final GridBagConstraints gbc_labelTextField = new GridBagConstraints();
        gbc_labelTextField.insets = new Insets(0, 0, 5, 0);
        gbc_labelTextField.fill = GridBagConstraints.HORIZONTAL;
        gbc_labelTextField.gridx = 1;
        gbc_labelTextField.gridy = 1;
        add(labelTextField, gbc_labelTextField);
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

        lblColor_1 = new JLabel("Color");
        final GridBagConstraints gbc_lblColor_1 = new GridBagConstraints();
        gbc_lblColor_1.anchor = GridBagConstraints.EAST;
        gbc_lblColor_1.insets = new Insets(0, 0, 5, 5);
        gbc_lblColor_1.gridx = 0;
        gbc_lblColor_1.gridy = 2;
        add(lblColor_1, gbc_lblColor_1);

        colorSelector = new ColorSelector();
        colorSelector.setToolTipText(Option.COLOR.getHelp());
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

        lineWidthSpinner = new JSpinner();
        lineWidthSpinner.setToolTipText(Option.LINE_WIDTH.getHelp());
        lineWidthSpinner.setModel(new SpinnerNumberModel(Float.valueOf(0f), Float.valueOf(0f), null, Float.valueOf(0.5f)));
        final GridBagConstraints gbc_lineWidthSpinner = new GridBagConstraints();
        gbc_lineWidthSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_lineWidthSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_lineWidthSpinner.gridx = 1;
        gbc_lineWidthSpinner.gridy = 3;
        add(lineWidthSpinner, gbc_lineWidthSpinner);

        lblTimeOffset = new JLabel("Time Offset");
        final GridBagConstraints gbc_lblTimeOffset = new GridBagConstraints();
        gbc_lblTimeOffset.anchor = GridBagConstraints.EAST;
        gbc_lblTimeOffset.insets = new Insets(0, 0, 5, 5);
        gbc_lblTimeOffset.gridx = 0;
        gbc_lblTimeOffset.gridy = 4;
        add(lblTimeOffset, gbc_lblTimeOffset);

        timeOffsetSpinner = new JSpinner();
        timeOffsetSpinner.setToolTipText(Option.TIME_OFFSET.getHelp());
        timeOffsetSpinner.setModel(new DurationSpinnerModel());
        timeOffsetSpinner.setEditor(new DurationEditor(timeOffsetSpinner));
        final GridBagConstraints gbc_timeOffsetSpinner = new GridBagConstraints();
        gbc_timeOffsetSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_timeOffsetSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_timeOffsetSpinner.gridx = 1;
        gbc_timeOffsetSpinner.gridy = 4;
        add(timeOffsetSpinner, gbc_timeOffsetSpinner);

        lblForcedPointTime = new JLabel("Forced Point Time Interval");
        final GridBagConstraints gbc_lblForcedPointTime = new GridBagConstraints();
        gbc_lblForcedPointTime.anchor = GridBagConstraints.EAST;
        gbc_lblForcedPointTime.insets = new Insets(0, 0, 5, 5);
        gbc_lblForcedPointTime.gridx = 0;
        gbc_lblForcedPointTime.gridy = 5;
        add(lblForcedPointTime, gbc_lblForcedPointTime);

        forcedPointTimeIntervalSpinner = new JSpinner();
        forcedPointTimeIntervalSpinner.setToolTipText(Option.FORCED_POINT_TIME_INTERVAL.getHelp());
        forcedPointTimeIntervalSpinner.setModel(new DurationSpinnerModel());
        forcedPointTimeIntervalSpinner.setEditor(new DurationEditor(forcedPointTimeIntervalSpinner));
        final GridBagConstraints gbc_forcedPointTimeIntervalSpinner = new GridBagConstraints();
        gbc_forcedPointTimeIntervalSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_forcedPointTimeIntervalSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_forcedPointTimeIntervalSpinner.gridx = 1;
        gbc_forcedPointTimeIntervalSpinner.gridy = 5;
        add(forcedPointTimeIntervalSpinner, gbc_forcedPointTimeIntervalSpinner);

        lblTrimGpxStart = new JLabel("Trim Start of GPX File");
        final GridBagConstraints gbc_lblTrimGpxStart = new GridBagConstraints();
        gbc_lblTrimGpxStart.anchor = GridBagConstraints.EAST;
        gbc_lblTrimGpxStart.insets = new Insets(0, 0, 5, 5);
        gbc_lblTrimGpxStart.gridx = 0;
        gbc_lblTrimGpxStart.gridy = 6;
        add(lblTrimGpxStart, gbc_lblTrimGpxStart);

        trimGpxStartSpinner = new JSpinner();
        trimGpxStartSpinner.setToolTipText(Option.TRIM_GPX_START.getHelp());
        trimGpxStartSpinner.setModel(new DurationSpinnerModel());
        trimGpxStartSpinner.setEditor(new DurationEditor(trimGpxStartSpinner));
        final GridBagConstraints gbc_trimGpxStartSpinner = new GridBagConstraints();
        gbc_trimGpxStartSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_trimGpxStartSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_trimGpxStartSpinner.gridx = 1;
        gbc_trimGpxStartSpinner.gridy = 6;
        add(trimGpxStartSpinner, gbc_trimGpxStartSpinner);

        lblTrimGpxEnd = new JLabel("Trim End of GPX File");
        final GridBagConstraints gbc_lblTrimGpxEnd = new GridBagConstraints();
        gbc_lblTrimGpxEnd.anchor = GridBagConstraints.EAST;
        gbc_lblTrimGpxEnd.insets = new Insets(0, 0, 5, 5);
        gbc_lblTrimGpxEnd.gridx = 0;
        gbc_lblTrimGpxEnd.gridy = 7;
        add(lblTrimGpxEnd, gbc_lblTrimGpxEnd);

        trimGpxEndSpinner = new JSpinner();
        trimGpxEndSpinner.setToolTipText(Option.TRIM_GPX_END.getHelp());
        trimGpxEndSpinner.setModel(new DurationSpinnerModel());
        trimGpxEndSpinner.setEditor(new DurationEditor(trimGpxEndSpinner));
        final GridBagConstraints gbc_trimGpxEndSpinner = new GridBagConstraints();
        gbc_trimGpxEndSpinner.fill = GridBagConstraints.HORIZONTAL;
        gbc_trimGpxEndSpinner.insets = new Insets(0, 0, 5, 0);
        gbc_trimGpxEndSpinner.gridx = 1;
        gbc_trimGpxEndSpinner.gridy = 7;
        add(trimGpxEndSpinner, gbc_trimGpxEndSpinner);

        enableIconCheckBox = new JCheckBox("");
        enableIconCheckBox.setToolTipText(Option.ICON_ENABLE.getHelp());
        final GridBagConstraints gbc_enableIconCheckBox = new GridBagConstraints();
        gbc_enableIconCheckBox.fill = GridBagConstraints.HORIZONTAL;
        gbc_enableIconCheckBox.anchor = GridBagConstraints.EAST;
        gbc_enableIconCheckBox.insets = new Insets(0, 0, 5, 0);
        gbc_enableIconCheckBox.gridx = 1;
        gbc_enableIconCheckBox.gridy = 8;
        add(enableIconCheckBox, gbc_enableIconCheckBox);

        lblEnableIcon = new JLabel("Enable Icon");
        final GridBagConstraints gbc_lblEnableIcon = new GridBagConstraints();
        gbc_lblEnableIcon.anchor = GridBagConstraints.EAST;
        gbc_lblEnableIcon.insets = new Insets(0, 0, 5, 5);
        gbc_lblEnableIcon.gridx = 0;
        gbc_lblEnableIcon.gridy = 8;
        add(lblEnableIcon, gbc_lblEnableIcon);

        final JButton btnNewButton = new JButton("Remove Track");
        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(final ActionEvent e) {
                remove();
            }
        });
        final GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
        gbc_btnNewButton.anchor = GridBagConstraints.EAST;
        gbc_btnNewButton.gridwidth = 3;
        gbc_btnNewButton.insets = new Insets(0, 0, 0, 5);
        gbc_btnNewButton.gridx = 0;
        gbc_btnNewButton.gridy = 9;
        add(btnNewButton, gbc_btnNewButton);

        final PropertyChangeListener propertyChangeListener = new PropertyChangeListener() {
            @Override
            public void propertyChange(final PropertyChangeEvent evt) {
                configurationChanged();
            }
        };
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

        final ChangeListener changeListener = new ChangeListener() {
            @Override
            public void stateChanged(final ChangeEvent e) {
                configurationChanged();
            }
        };

        lineWidthSpinner.addChangeListener(changeListener);
        timeOffsetSpinner.addChangeListener(changeListener);
        forcedPointTimeIntervalSpinner.addChangeListener(changeListener);
        trimGpxStartSpinner.addChangeListener(changeListener);
        trimGpxEndSpinner.addChangeListener(changeListener);
        enableIconCheckBox.addChangeListener(changeListener);
    }


    protected abstract void labelChanged(String label);


    public TrackConfiguration createConfiguration() throws UserException {
        final Builder b = TrackConfiguration.createBuilder();

        b.inputGpx(new File(inputGpxFileSelector.getFilename()));
        b.label(labelTextField.getText());
        b.color(colorSelector.getColor());
        b.lineWidth((Float) lineWidthSpinner.getValue());
        b.forcedPointInterval((Long) forcedPointTimeIntervalSpinner.getValue());
        b.timeOffset((Long) timeOffsetSpinner.getValue());
        b.trimGpxStart((Long) trimGpxStartSpinner.getValue());
        b.trimGpxEnd((Long) trimGpxEndSpinner.getValue());
        b.enableIcon(enableIconCheckBox.isSelected());
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
        enableIconCheckBox.setSelected(c.isEnableIcon());
        labelChanged(c.getLabel());
    }


    protected abstract void remove();

    protected abstract void configurationChanged();

}
