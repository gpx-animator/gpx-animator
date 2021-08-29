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
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Dimension;
import java.awt.Font;
import java.io.Serial;
import java.util.ResourceBundle;

public final class FontSelector extends JPanel {

    @NonNls
    public static final String PROPERTY_FONT = "font";

    @Serial
    private static final long serialVersionUID = 4157235691776396086L;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final JTextField fontTextField;

    private Font selectedFont;

    public FontSelector() {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        fontTextField = new JTextField();
        fontTextField.setMaximumSize(new Dimension(2147483647, 21));
        fontTextField.setPreferredSize(new Dimension(55, 21));
        add(fontTextField);
        fontTextField.setColumns(10);
        fontTextField.setEditable(false);

        final var rigidArea = Box.createRigidArea(new Dimension(5, 0));
        add(rigidArea);

        final var btnSelectFontButton = new JButton(resourceBundle.getString("ui.dialog.fontselector.button.selectfont"));
        btnSelectFontButton.addActionListener(e -> {
            final var fontChooser = new FontChooser();
            if (selectedFont != null) {
                fontChooser.setSelectedFont(getSelectedFont());
            }
            if (fontChooser.showDialog(FontSelector.this) == FontChooser.OK_OPTION) {
                final var oldFont = getSelectedFont();
                setSelectedFont(fontChooser.getSelectedFont());
                firePropertyChange(PROPERTY_FONT, oldFont, getSelectedFont());
            }
        });
        add(btnSelectFontButton);
    }

    public Font getSelectedFont() {
        return selectedFont;
    }

    public void setSelectedFont(@Nullable final Font selectedFont) {
        this.selectedFont = selectedFont;
        if (selectedFont == null) {
            fontTextField.setText("");
        } else {
            final var name = selectedFont.getName();
            final var style = getStyleText(selectedFont, resourceBundle);
            final var size = selectedFont.getSize();
            final var text = "%s, %s, %d".formatted(name, style, size);
            fontTextField.setText(text);
        }
    }

    private static String getStyleText(@NonNull final Font font, @NonNull final ResourceBundle resourceBundle) {
        if (font.isBold() && font.isItalic()) {
            return resourceBundle.getString("ui.dialog.fontchooser.fontstyle.bolditalic");
        } else if (font.isBold()) {
            return resourceBundle.getString("ui.dialog.fontchooser.fontstyle.bold");
        } else if (font.isItalic()) {
            return resourceBundle.getString("ui.dialog.fontchooser.fontstyle.italic");
        } else {
            return resourceBundle.getString("ui.dialog.fontchooser.fontstyle.plain");
        }
    }

}
