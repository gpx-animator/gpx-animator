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
package app.gpx_animator.ui.swing;

import javax.swing.AbstractSpinnerModel;
import java.io.Serial;
import java.math.BigDecimal;

public final class EmptyNullSpinnerModel extends AbstractSpinnerModel {

    @Serial
    private static final long serialVersionUID = -8064362052986633347L;

    private final transient boolean zeroEmpty;
    private final transient Number stepSize;
    private final transient Number minimum;
    private final transient Number maximum;
    private final transient int fractions;
    private final transient double fractionValue;
    private transient Number value;
    private transient Object object = new Object();


    public EmptyNullSpinnerModel(final Number value, final Number minimum, final Number maximum, final Number stepSize) {
        this(value, minimum, maximum, stepSize, true, -1);
    }

    public EmptyNullSpinnerModel(final Number value, final Number minimum, final Number maximum, final Number stepSize,
                                 final boolean zeroEmpty, final int fractions) {
        if (stepSize == null) {
            throw new IllegalArgumentException("value and stepSize must be non-null");
        }
        if (value != null && !((minimum == null || compareTo(minimum, value) <= 0) && (maximum == null || compareTo(maximum, value) >= 0))) {
            throw new IllegalArgumentException("(minimum <= value <= maximum) is false");
        }
        this.zeroEmpty = zeroEmpty;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepSize = stepSize;
        this.fractions = fractions;

        if (fractions < 0) {
            fractionValue = 0.0;
        } else {
            var fv = 1.0;
            for (var i = fractions; i > 0; i--) {
                fv *= 10.0;
            }
            fractionValue = fv;
        }
    }

    @Override
    public Object getNextValue() {
        return value == null ? /*minimum +*/ stepSize : incrValue(+1);
    }

    @Override
    public Object getPreviousValue() {
        return value == null ? 0 : incrValue(-1);
    }

    private Number incrValue(final int dir) {
        final Number newValue;
        if ((value instanceof Float) || (value instanceof Double)) {
            final var v = round(value.doubleValue() + (stepSize.doubleValue() * dir));
            if (value instanceof Double) {
                newValue = v;
            } else {
                newValue = (float) v;
            }
        } else {
            final var v = value.longValue() + (stepSize.longValue() * dir);

            if (value instanceof Long) {
                newValue = v;
            } else if (value instanceof Integer) {
                newValue = (int) v;
            } else if (value instanceof Short) {
                newValue = (short) v;
            } else {
                newValue = (byte) v;
            }
        }

        if ((maximum != null) && compareTo(maximum, newValue) < 0) {
            return maximum;
        }
        if ((minimum != null) && compareTo(minimum, newValue) > 0) {
            return minimum;
        } else {
            return newValue;
        }
    }

    private double round(final double doubleValue) {
        return (fractions < 0) ? doubleValue : Math.round(doubleValue * fractionValue) / fractionValue;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment")
    public void setValue(final Object o) {
        if (object == null && o == null || object != null && object.equals(o)) {
            // no change
            return;
        }

        if (o != null && !(o instanceof Number)) {
            throw new IllegalArgumentException(String.format("setValue was called with invalid Argument not of type Number. o: %s", o));
        }

        object = o;
        var newValue = (Number) o;
        if (newValue != null && zeroEmpty && newValue.doubleValue() == 0.0) {
            newValue = null;
        }

        if (newValue != null) {
            if ((maximum != null) && compareTo(maximum, newValue) < 0) {
                newValue = maximum;
            }
            if ((minimum != null) && compareTo(minimum, newValue) > 0) {
                newValue = minimum;
            }
        }
        value = newValue;
        fireStateChanged();
    }

    public int compareTo(final Number n1, final Number n2) {
        var b1 = BigDecimal.valueOf(n1.doubleValue());
        var b2 = BigDecimal.valueOf(n2.doubleValue());
        return b1.compareTo(b2);
    }
}
