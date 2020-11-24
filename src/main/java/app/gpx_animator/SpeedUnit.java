package app.gpx_animator;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

interface Calculation {
    double calc(final double kmh);
}

public enum SpeedUnit {
    KMH(kmh -> kmh),
    MPH(kmh -> kmh * 0.62137119223733),
    MIN_KM(kmh -> 3600 / kmh / 60),
    MIN_MI(kmh -> 3600 / (kmh * 0.62137119223733) / 60),
    KNOTS(kmh -> kmh * 0.53995680346039),
    MACH(kmh -> kmh * 0.00081699346405229),
    LIGHT(kmh -> kmh * 9.2656693110598E-10);

    private final Calculation calculation;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    public static void fillComboBox(final JComboBox<SpeedUnit> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

    /**
     * Define a speed unit.
     *
     * @param calculation how to calculate the speed, based on KMH
     */
    SpeedUnit(final Calculation calculation) {
        this.calculation = calculation;
    }

    @Override
    public String toString() {
        return resourceBundle.getString("speedunit.".concat(name().toLowerCase(Locale.getDefault())));
    }

    public double convertSpeed(final double speed) {
        return calculation.calc(speed);
    }
}
