package app.gpx_animator.ui;

import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import app.gpx_animator.Preferences;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import java.util.ResourceBundle;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class PreferencesDialog extends JDialog {

    private static final long serialVersionUID = -8767146323054030406L;

    public PreferencesDialog(final JFrame owner) {
        super(owner, true);

        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        setTitle(resourceBundle.getString("ui.dialog.preferences.title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final FileSelector tileCachePathSelector = new FileSelector(DIRECTORIES_ONLY) {
            private static final long serialVersionUID = 7372002778979993241L;
            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };
        tileCachePathSelector.setToolTipText(resourceBundle.getString("ui.dialog.preferences.cachepath.tooltip"));

        final JSpinner tileCacheTimeLimitSpinner = new JSpinner();
        tileCacheTimeLimitSpinner.setToolTipText(resourceBundle.getString("ui.dialog.preferences.cachetimelimit.tooltip"));
        tileCacheTimeLimitSpinner.setModel(new DurationSpinnerModel());
        tileCacheTimeLimitSpinner.setEditor(new DurationEditor(tileCacheTimeLimitSpinner));

        final JButton cancelButton = new JButton(resourceBundle.getString("ui.dialog.preferences.button.cancel"));
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        }));

        final JButton saveButton = new JButton(resourceBundle.getString("ui.dialog.preferences.button.save"));
        saveButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            Preferences.setTileCacheDir(tileCachePathSelector.getFilename());
            Preferences.setTileCacheTimeLimit((Long) tileCacheTimeLimitSpinner.getValue());
            setVisible(false);
            dispose();
        }));

        setContentPane(FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("right:p, 5dlu, fill:[200dlu, pref]") //NON-NLS
                .rows("p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p") //NON-NLS

                .addSeparator(resourceBundle.getString("ui.dialog.preferences.cache.separator")).xyw(1, 1, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.cachepath.label")).xy(1, 3)
                .add(tileCachePathSelector).xy(3, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.cachetimelimit.label")).xy(1, 5)
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
