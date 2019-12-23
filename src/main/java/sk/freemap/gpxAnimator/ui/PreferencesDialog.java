package sk.freemap.gpxAnimator.ui;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import sk.freemap.gpxAnimator.Preferences;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class PreferencesDialog extends JDialog {

    private final FileSelector tileCachePathSelector;
    private final JSpinner tileCacheTimeLimitSpinner;

    private final JButton cancelButton;
    private final JButton saveButton;

    public PreferencesDialog(final JFrame owner) {
        super(owner, true);
        setTitle("Preferences");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        tileCachePathSelector = new FileSelector(DIRECTORIES_ONLY) {
            private static final long serialVersionUID = 7372002778979993241L;
            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };
        tileCachePathSelector.setToolTipText("path to a directory to use for caching map tiles");

        tileCacheTimeLimitSpinner = new JSpinner();
        tileCacheTimeLimitSpinner.setToolTipText("time a cached map tile is valid");
        tileCacheTimeLimitSpinner.setModel(new DurationSpinnerModel());
        tileCacheTimeLimitSpinner.setEditor(new DurationEditor(tileCacheTimeLimitSpinner));

        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        }));

        saveButton = new JButton("Save");
        saveButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            savePreferences();
            setVisible(false);
            dispose();
        }));

        loadPreferences();

        setContentPane(buildContent());
        pack();
        setLocationRelativeTo(owner);
    }

    private JComponent buildContent() {
        return FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("right:p, 5dlu, fill:[200dlu, pref]")
                .rows("p, 5dlu, p, 5dlu, p")

                .add("Tile Cache Directory")  .xy(1, 1)
                .add(tileCachePathSelector) .xy(3, 1)
                .add("Tile Cache Time Limit") .xy(1, 3)
                .add(tileCacheTimeLimitSpinner).xy(3, 3)
                .addBar(cancelButton, saveButton).xyw(1, 5, 3, CellConstraints.RIGHT, CellConstraints.FILL)
                .build();
    }

    private void loadPreferences() {
        tileCachePathSelector.setFilename(Preferences.getTileCacheDir());
        tileCacheTimeLimitSpinner.setValue(Preferences.getTileCacheTimeLimit());
    }

    private void savePreferences() {
        Preferences.setTileCacheDir(tileCachePathSelector.getFilename());
        Preferences.setTileCacheTimeLimit((Long) tileCacheTimeLimitSpinner.getValue());
    }

}
