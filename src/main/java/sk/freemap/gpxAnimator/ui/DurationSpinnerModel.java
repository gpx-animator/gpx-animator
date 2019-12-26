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
import java.util.Objects;

public final class DurationSpinnerModel extends AbstractSpinnerModel {

    private static final long serialVersionUID = 7220186634453532297L;

    private transient Long duration;
    private Field field = Field.SECOND;

    @Override
    public Object getValue() {
        return duration;
    }

    @Override
    public void setValue(final Object value) {
        if (!Objects.equals(duration, value)) {
            duration = (Long) value;
            fireStateChanged();
        }
    }

    @Override
    public Object getNextValue() {
        return (duration == null ? 0 : duration) + getDiffMs();
    }

    @Override
    public Object getPreviousValue() {
        return (duration == null ? 0 : duration) - getDiffMs();
    }

    public Field getField() {
        return field;
    }

    public void setField(final Field field) {
        this.field = field;
    }

    @SuppressWarnings("PMD.MissingBreakInSwitch") // Calculations actions sum up from top to down
    private long getDiffMs() {
        long add = 1;
        switch (field) {
            case DAY:
                add *= 24;
            case HOUR:
                add *= 60;
            case MINUTE:
                add *= 60;
            case SECOND:
                add *= 1000;
            case MILLISECOND:
                break;
            default:
                throw new AssertionError();
        }
        return add;
    }


    public enum Field {
        MILLISECOND("ms"),
        SECOND("s"),
        MINUTE("m"),
        HOUR("h"),
        DAY("d");

        private final String unit;

        Field(final String unit) {
            this.unit = unit;
        }

        public static Field fromUnit(final String unit) {
            for (final Field field : Field.values()) {
                if (field.getUnit().equals(unit)) {
                    return field;
                }
            }
            return null;
        }

        public String getUnit() {
            return unit;
        }
    }

}
