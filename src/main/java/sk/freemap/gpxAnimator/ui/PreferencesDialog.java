package sk.freemap.gpxAnimator.ui;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import sk.freemap.gpxAnimator.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class PreferencesDialog extends JDialog {

    private static final long serialVersionUID = -8767146323054030406L;

    public PreferencesDialog(final JFrame owner) {
        super(owner, true);
        setTitle("Preferences");
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final FileSelector tileCachePathSelector = new FileSelector(DIRECTORIES_ONLY) {
            private static final long serialVersionUID = 7372002778979993241L;
            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };
        tileCachePathSelector.setToolTipText("path to a directory to use for caching map tiles");

        final JSpinner tileCacheTimeLimitSpinner = new JSpinner();
        tileCacheTimeLimitSpinner.setToolTipText("time a cached map tile is valid");
        tileCacheTimeLimitSpinner.setModel(new DurationSpinnerModel());
        tileCacheTimeLimitSpinner.setEditor(new DurationEditor(tileCacheTimeLimitSpinner));

        final JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        }));

        final JButton saveButton = new JButton("Save");
        saveButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            Preferences.setTileCacheDir(tileCachePathSelector.getFilename());
            Preferences.setTileCacheTimeLimit((Long) tileCacheTimeLimitSpinner.getValue());
            setVisible(false);
            dispose();
        }));

        setContentPane(FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("right:p, 5dlu, fill:[200dlu, pref]")
                .rows("p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p")

                .addSeparator("Caching Map Tiles").xyw(1, 1, 3)
                .add("Tile Cache Directory").xy(1, 3)
                .add(tileCachePathSelector).xy(3, 3)
                .add("Tile Cache Time Limit").xy(1, 5)
                .add(tileCacheTimeLimitSpinner).xy(3, 5)

                .addSeparator("").xyw(1, 7, 3)
                .addBar(cancelButton, saveButton).xyw(1, 9, 3, CellConstraints.RIGHT, CellConstraints.FILL)
                .build());

        tileCachePathSelector.setFilename(Preferences.getTileCacheDir());
        tileCacheTimeLimitSpinner.setValue(Preferences.getTileCacheTimeLimit());

        pack();
        setLocationRelativeTo(owner);
    }

}
