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
