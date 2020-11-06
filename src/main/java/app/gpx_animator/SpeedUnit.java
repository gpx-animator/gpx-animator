package app.gpx_animator;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.ResourceBundle;

public enum SpeedUnit {
    KMH("km/h", 1.0),
    MPH("mph", 0.62137119223733),
    MIN_KM("min/km", 60),
    MIN_MI("min/mi", 96.56064),
    KNOTS("kt", 0.53995680346039),
    MACH("Ma", 0.00081699346405229),
    LIGHT("c", 9.2656693110598E-10);

    private final String unit;
    private final double factor;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    public static void fillComboBox(final JComboBox<SpeedUnit> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

    /**
     * Define a speed unit.
     *
     * @param unit the name of the speed unit
     * @param factor the conversion factor, based on km/h
     */
    SpeedUnit(final String unit, final double factor) {
        this.unit = unit;
        this.factor = factor;
    }

    public String getUnit() {
        return unit;
    }

    @Override
    public String toString() {
        return resourceBundle.getString("speedunit.".concat(name().toLowerCase()));
    }

    public double convertSpeed(final double speed) {
        return speed * factor;
    }
}
