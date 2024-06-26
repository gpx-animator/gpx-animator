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
import org.jetbrains.annotations.NonNls;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.io.File;
import java.io.Serial;

public abstract class FileSelector extends JPanel {

    @NonNls
    public static final String PROPERTY_FILENAME = "filename";

    @Serial
    private static final long serialVersionUID = 3157365691996396016L;

    private final JTextField fileTextField;

    private final JButton btnNewButton;

    private JFileChooser fileChooser;

    /**
     * Create the panel.
     */
    @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
    protected FileSelector(final int fileSelectionMode) {
        final var resourceBundle = Preferences.getResourceBundle();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        fileTextField = new JTextField();
        fileTextField.setMaximumSize(new Dimension(2147483647, 21));
        fileTextField.setPreferredSize(new Dimension(55, 21));
        add(fileTextField);
        fileTextField.setColumns(10);

        fileTextField.getDocument().addDocumentListener(new DocumentListener() {
            private String oldText = fileTextField.getText();

            @Override
            public void removeUpdate(final DocumentEvent e) {
                fire();
            }

            @Override
            public void insertUpdate(final DocumentEvent e) {
                fire();
            }

            @Override
            public void changedUpdate(final DocumentEvent e) {
                fire();
            }

            private void fire() {
                firePropertyChange(PROPERTY_FILENAME, oldText, fileTextField.getText()); //NON-NLS
                oldText = fileTextField.getText();
            }
        });

        final var rigidArea = Box.createRigidArea(new Dimension(5, 0));
        add(rigidArea);

        btnNewButton = new JButton(resourceBundle.getString("ui.dialog.fileselector.button.browse"));
        btnNewButton.addActionListener(e -> {
            if (fileChooser == null) {
                fileChooser = new JFileChooser();
            }

            fileChooser.setFileSelectionMode(fileSelectionMode);

            final var text = fileTextField.getText();
            if (text.isEmpty()) {
                fileChooser.setCurrentDirectory(new File(Preferences.getLastWorkingDir()));
                fileChooser.setSelectedFile(new File(""));
            } else {
                final var file = new File(text);
                fileChooser.setCurrentDirectory(file.getParentFile() == null ? new File(System.getProperty("user.dir")) : file.getParentFile());
                fileChooser.setSelectedFile(file);
            }

            final var type = configure(fileChooser);
            if ((type == Type.OPEN ? fileChooser.showOpenDialog(FileSelector.this)
                    : fileChooser.showSaveDialog(FileSelector.this)) == JFileChooser.APPROVE_OPTION) {
                Preferences.setLastWorkingDir(fileChooser.getSelectedFile().getParent());
                setFilename(transformFilename(fileChooser.getSelectedFile().toString()));
            }
        });
        add(btnNewButton);

    }

    protected abstract Type configure(JFileChooser gpxFileChooser);

    /**
     * Overwrite this method to transform the filename.
     * Defaults to no transformation and behaves like a getter.
     *
     * @param filename the filename to be transformed
     * @return the transformed filename
     */
    protected String transformFilename(final String filename) {
        return filename;
    }

    public final String getFilename() {
        return fileTextField.getText();
    }

    public final void setFilename(final String filename) {
        fileTextField.setText(filename);
    }

    @Override
    public final String getToolTipText() {
        return fileTextField.getToolTipText();
    }

    @Override
    public final void setToolTipText(final String text) {
        fileTextField.setToolTipText(text);
        btnNewButton.setToolTipText(text);
    }

    public enum Type {
        OPEN, SAVE
    }

    public final File getFile() {
        final var filename = getFilename();
        return filename == null || filename.isEmpty() ? null : new File(filename);
    }

}
