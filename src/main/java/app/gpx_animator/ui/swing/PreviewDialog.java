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

import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.plugins.PreviewPlugin;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.io.Serial;
import java.util.ResourceBundle;
import javax.swing.WindowConstants;

public final class PreviewDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 3102552837955393556L;

    private final transient PreviewPlugin plugin;
    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private final transient ImageIcon imageIcon = new ImageIcon();

    public PreviewDialog(@NonNull final PreviewPlugin plugin, @NonNull final BufferedImage image) {
        super();
        this.plugin = plugin;
        setTitle(resourceBundle.getString("ui.dialog.preview.title"));
        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        updatePreview(image);
        setLayout(new BorderLayout());
        getContentPane().add(new JLabel(imageIcon), BorderLayout.CENTER);
        pack();
        setResizable(false);
        setLocationRelativeTo(null);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(@Nullable final WindowEvent e) {
                plugin.dialogClosed();
                closeDialog();
            }
        });
        setVisible(true);
    }

    public void updatePreview(@NonNull final BufferedImage image) {
        SwingUtilities.invokeLater(() -> {
            imageIcon.setImage(image);
            this.repaint(1_000);
        });
    }

    public void closeDialog() {
        plugin.dialogClosed();
        setVisible(false);
        dispose();
    }

}
