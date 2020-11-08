package app.gpx_animator;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

public enum SpeedUnit {
    KMH(1.0),
    MPH(0.62137119223733),
    MIN_KM(60),
    MIN_MI(96.56064),
    KNOTS(0.53995680346039),
    MACH(0.00081699346405229),
    LIGHT(9.2656693110598E-10);

    private final double factor;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    public static void fillComboBox(final JComboBox<SpeedUnit> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

    /**
     * Define a speed unit.
     *
     * @param factor the conversion factor, based on KMH
     */
    SpeedUnit(final double factor) {
        this.factor = factor;
    }

    @Override
    public String toString() {
        return resourceBundle.getString("speedunit.".concat(name().toLowerCase(Locale.getDefault())));
    }

    public double convertSpeed(final double speed) {
        return speed * factor;
    }
}
