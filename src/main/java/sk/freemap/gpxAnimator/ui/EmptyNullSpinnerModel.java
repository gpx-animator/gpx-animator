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
package sk.freemap.gpxAnimator.ui;

import javax.swing.AbstractSpinnerModel;

public class EmptyNullSpinnerModel extends AbstractSpinnerModel {

    private static final long serialVersionUID = -8064362052986633347L;

    private final transient boolean zeroEmpty;
    private final transient Number stepSize;
    private final transient Comparable minimum, maximum;
    private transient Number value;
    private transient Object object = new Object();


    public EmptyNullSpinnerModel(final Number value, final Comparable minimum, final Comparable maximum, final Number stepSize) {
        this(value, minimum, maximum, stepSize, true);
    }


    public EmptyNullSpinnerModel(final Number value, final Comparable minimum, final Comparable maximum, final Number stepSize, final boolean zeroEmpty) {
        if (stepSize == null) {
            throw new IllegalArgumentException("value and stepSize must be non-null");
        }
        if (value != null && !((minimum == null || minimum.compareTo(value) <= 0) && (maximum == null || maximum.compareTo(value) >= 0))) {
            throw new IllegalArgumentException("(minimum <= value <= maximum) is false");
        }
        this.zeroEmpty = zeroEmpty;
        this.value = value;
        this.minimum = minimum;
        this.maximum = maximum;
        this.stepSize = stepSize;
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
            final double v = value.doubleValue() + (stepSize.doubleValue() * dir);
            if (value instanceof Double) {
                newValue = Double.valueOf(v);
            } else {
                newValue = Float.valueOf((float) v);
            }
        } else {
            final long v = value.longValue() + (stepSize.longValue() * dir);

            if (value instanceof Long) {
                newValue = Long.valueOf(v);
            } else if (value instanceof Integer) {
                newValue = Integer.valueOf((int) v);
            } else if (value instanceof Short) {
                newValue = Short.valueOf((short) v);
            } else {
                newValue = Byte.valueOf((byte) v);
            }
        }

        if ((maximum != null) && (maximum.compareTo(newValue) < 0)) {
            return (Number) maximum;
        }
        if ((minimum != null) && (minimum.compareTo(newValue) > 0)) {
            return (Number) minimum;
        } else {
            return newValue;
        }
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
    public void setValue(final Object o) {
        if (object == null && o == null || object != null && object.equals(o)) {
            // no change
            return;
        }

        if (o == null && object != null || o != null && !o.equals(object)) {
            object = o;
            Object newValue = o == null || ((Number) o).doubleValue() == 0.0 && zeroEmpty ? null : o;
            if (newValue != null) {
                if ((maximum != null) && (maximum.compareTo(newValue) < 0)) {
                    newValue = maximum;
                }
                if ((minimum != null) && (minimum.compareTo(newValue) > 0)) {
                    newValue = minimum;
                }
            }
            value = (Number) newValue;
            fireStateChanged();
        }
    }

}
