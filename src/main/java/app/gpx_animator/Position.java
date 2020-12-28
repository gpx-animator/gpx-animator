package app.gpx_animator;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public enum Position {

    // TODO Position.HIDE -> don't show on the map

    TOP_LEFT,
    TOP_CENTER,
    TOP_RIGHT,
    BOTTOM_LEFT,
    BOTTOM_CENTER,
    BOTTOM_RIGHT;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    @Override
    public String toString() {
        return resourceBundle.getString("position.".concat(name().toLowerCase(Locale.getDefault())));
    }

    public static Position parse(final String p) {
        return Position.valueOf(p.replace(' ', '_').toUpperCase(Locale.getDefault()));
    }

    public static void fillComboBox(final JComboBox<Position> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

}
