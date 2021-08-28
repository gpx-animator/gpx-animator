/*
 *  Copyright Contributors to the GPX Animator project.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package app.gpx_animator.ui.swing;

import app.gpx_animator.core.preferences.Preferences;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.io.Serial;

import static javax.swing.JFileChooser.DIRECTORIES_ONLY;

public class PreferencesDialog extends EscapeDialog {

    @Serial
    private static final long serialVersionUID = -8767146323054030406L;

    public PreferencesDialog(final JFrame owner) {
        super(owner);

        final var resourceBundle = Preferences.getResourceBundle();

        setTitle(resourceBundle.getString("ui.dialog.preferences.title"));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        final var tileCachePathSelector = new FileSelector(DIRECTORIES_ONLY) {
            @Serial
            private static final long serialVersionUID = 7372002778979993241L;
            @Override
            protected Type configure(final JFileChooser outputFileChooser) {
                return Type.OPEN;
            }
        };
        tileCachePathSelector.setToolTipText(resourceBundle.getString("ui.dialog.preferences.cachepath.tooltip"));

        final var tileCacheTimeLimitSpinner = new JSpinner();
        tileCacheTimeLimitSpinner.setToolTipText(resourceBundle.getString("ui.dialog.preferences.cachetimelimit.tooltip"));
        tileCacheTimeLimitSpinner.setModel(new DurationSpinnerModel());
        tileCacheTimeLimitSpinner.setEditor(new DurationEditor(tileCacheTimeLimitSpinner));

        final var trackColorPanel = new JPanel(new BorderLayout());
        final var trackColorRandom = new JCheckBox(resourceBundle.getString("ui.dialog.preferences.track.color.random"));
        final var trackColorSelector = new ColorSelector();
        trackColorRandom.setSelected(Preferences.getTrackColorRandom());
        trackColorSelector.setColor(Preferences.getTrackColorDefault());
        trackColorSelector.setEnabled(!Preferences.getTrackColorRandom());
        trackColorRandom.addActionListener((event) -> trackColorSelector.setEnabled(!trackColorRandom.isSelected()));
        trackColorPanel.add(trackColorRandom, BorderLayout.LINE_START);
        trackColorPanel.add(trackColorSelector, BorderLayout.CENTER);

        final var enablePreview = new JCheckBox();
        enablePreview.setSelected(Preferences.isPreviewEnabled());

        final var cancelButton = new JButton(resourceBundle.getString("ui.dialog.preferences.button.cancel"));
        cancelButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            setVisible(false);
            dispose();
        }));

        final var saveButton = new JButton(resourceBundle.getString("ui.dialog.preferences.button.save"));
        saveButton.addActionListener(e -> SwingUtilities.invokeLater(() -> {
            Preferences.setTileCacheDir(tileCachePathSelector.getFilename());
            Preferences.setTileCacheTimeLimit((Long) tileCacheTimeLimitSpinner.getValue());
            Preferences.setTrackColorRandom(trackColorRandom.isSelected());
            Preferences.setTrackColorDefault(trackColorSelector.getColor());
            Preferences.setPreviewEnabled(enablePreview.isSelected());
            setVisible(false);
            dispose();
        }));

        setContentPane(FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("right:p, 5dlu, fill:[200dlu, pref]") //NON-NLS
                .rows("p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 5dlu, p, 10dlu, p") //NON-NLS

                .addSeparator(resourceBundle.getString("ui.dialog.preferences.cache.separator")).xyw(1, 1, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.cachepath.label")).xy(1, 3)
                .add(tileCachePathSelector).xy(3, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.cachetimelimit.label")).xy(1, 5)
                .add(tileCacheTimeLimitSpinner).xy(3, 5)

                .addSeparator(resourceBundle.getString("ui.dialog.preferences.track")).xyw(1, 7, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.track.color")).xy(1, 9)
                .add(trackColorPanel).xy(3, 9)

                .addSeparator(resourceBundle.getString("ui.dialog.preferences.rendering")).xyw(1, 11, 3)
                .add(resourceBundle.getString("ui.dialog.preferences.rendering.enablepreview")).xy(1, 13)
                .add(enablePreview).xy(3, 13)

                .addSeparator("").xyw(1, 15, 3)
                .addBar(cancelButton, saveButton).xyw(1, 17, 3, CellConstraints.RIGHT, CellConstraints.FILL)
                .build());

        tileCachePathSelector.setFilename(Preferences.getTileCacheDir());
        tileCacheTimeLimitSpinner.setValue(Preferences.getTileCacheTimeLimit());

        pack();
        setLocationRelativeTo(owner);
    }

}
