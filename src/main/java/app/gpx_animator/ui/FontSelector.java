package app.gpx_animator.ui;

import app.gpx_animator.Preferences;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.util.ResourceBundle;

public class FontSelector extends JPanel {

    @NonNls
    public static final String PROPERTY_FONT = "font";

    private static final long serialVersionUID = 4157235691776396086L;

    private transient final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private transient final JTextField fontTextField;

    private transient Font font;

    public FontSelector() {

        setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));

        fontTextField = new JTextField();
        fontTextField.setMaximumSize(new Dimension(2147483647, 21));
        fontTextField.setPreferredSize(new Dimension(55, 21));
        add(fontTextField);
        fontTextField.setColumns(10);
        fontTextField.setEditable(false);

        final Component rigidArea = Box.createRigidArea(new Dimension(5, 0));
        add(rigidArea);

        final JButton btnSelectFontButton = new JButton(resourceBundle.getString("ui.dialog.fontselector.button.selectfont"));
        btnSelectFontButton.addActionListener(e -> {
            final FontChooser fontChooser = new FontChooser();
            if (font != null) {
                fontChooser.setSelectedFont(getSelectedFont());
            }
            if (fontChooser.showDialog(FontSelector.this) == FontChooser.OK_OPTION) {
                final Font oldFont = getSelectedFont();
                setSelectedFont(fontChooser.getSelectedFont());
                firePropertyChange(PROPERTY_FONT, oldFont, getSelectedFont());
            }
        });
        add(btnSelectFontButton);
    }

    public Font getSelectedFont() {
        return font;
    }

    public void setSelectedFont(@Nullable final Font font) {
        this.font = font;
        if (font == null) {
            fontTextField.setText("");
        } else {
            final String name = font.getName();
            final String style = getStyleText(font, resourceBundle);
            final int size = font.getSize();
            final String text = "%s, %s, %d".formatted(name, style, size);
            fontTextField.setText(text);
        }
    }

    private static String getStyleText(@NotNull final Font font, @NotNull ResourceBundle resourceBundle) {
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
