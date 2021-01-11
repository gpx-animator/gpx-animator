/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
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
package app.gpx_animator.ui;

import app.gpx_animator.core.preferences.Preferences;
import org.jetbrains.annotations.NonNls;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionListener;
import java.io.Serial;
import java.util.Locale;

public final class ColorSelector extends JPanel {

    @NonNls
    @SuppressWarnings("DuplicateStringLiteralInspection")
    public static final String PROPERTY_COLOR = "color";

    @Serial
    private static final long serialVersionUID = 6506364764640471311L;

    private final transient JTextField colorTextField;

    private final transient JButton selectButton;


    /**
     * Create the panel.
     */
    public ColorSelector() {
        final var resourceBundle = Preferences.getResourceBundle();

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        colorTextField = new JTextField();
        colorTextField.setEditable(false);
        colorTextField.setMaximumSize(new Dimension(2147483647, 21));
        colorTextField.setPreferredSize(new Dimension(55, 21));
        add(colorTextField);
        colorTextField.setColumns(10);

        final var rigidArea = Box.createRigidArea(new Dimension(5, 0));
        add(rigidArea);

        selectButton = new JButton(resourceBundle.getString("ui.dialog.color.button.select"));
        selectButton.addActionListener(e -> {
            final var chooserPane = new JColorChooser();
            chooserPane.setColor(colorTextField.getBackground());
            final ActionListener okListener = e1 -> setColor(chooserPane.getColor());
            final var colorChooser = JColorChooser.createDialog(
                    ColorSelector.this, resourceBundle.getString("ui.dialog.color.title"), true, chooserPane, okListener, null);
            colorChooser.setVisible(true);
        });

        add(selectButton);
    }

    public Color getColor() {
        return colorTextField.getBackground();
    }

    public void setColor(final Color color) {
        final var oldColor = colorTextField.getBackground();
        colorTextField.setBackground(color);
        final var l = color.getRed() / 255.0 * 0.299 + color.getGreen() / 255.0 * 0.587 + color.getBlue() / 255.0 * 0.114;
        colorTextField.setForeground(l > 0.5 ? Color.BLACK : Color.WHITE);
        colorTextField.setText("#".concat(Integer.toHexString(color.getRGB()).toUpperCase(Locale.getDefault())));
        firePropertyChange(PROPERTY_COLOR, oldColor, color); //NON-NLS
    }

    @Override
    public String getToolTipText() {
        return colorTextField.getToolTipText();
    }

    @Override
    public void setToolTipText(final String text) {
        colorTextField.setToolTipText(text);
        selectButton.setToolTipText(text);
    }

    @Override
    public void setEnabled(final boolean enabled) {
        colorTextField.setEnabled(enabled);
        selectButton.setEnabled(enabled);
    }
}
