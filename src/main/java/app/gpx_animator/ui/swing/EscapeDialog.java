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
