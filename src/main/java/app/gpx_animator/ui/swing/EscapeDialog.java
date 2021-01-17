package app.gpx_animator.ui.swing;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JRootPane;
import javax.swing.KeyStroke;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.io.Serial;

/**
 * Don't set the content pane directly, use
 * <code>getContentPane().add(...)</code>
 * instead!
 */
abstract class EscapeDialog extends JDialog {

    @Serial
    private static final long serialVersionUID = 126544226951191103L;

    EscapeDialog(@NonNull final JFrame owner, final boolean modal) {
        super(owner, modal);
    }

    @Override
    public void setContentPane(@NonNull final Container contentPane) {
        getContentPane().add(contentPane);
    }

    protected final JRootPane createRootPane() {
        final var rootPane = new JRootPane();
        final var stroke = KeyStroke.getKeyStroke("ESCAPE");
        final var actionListener = new AbstractAction() {
            public void actionPerformed(@Nullable final ActionEvent actionEvent) {
                setVisible(false);
            }
        };
        final var inputMap = rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(stroke, "ESCAPE");
        rootPane.getActionMap().put("ESCAPE", actionListener);

        return rootPane;
    }

}
