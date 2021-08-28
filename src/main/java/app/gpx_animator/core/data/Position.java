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
package app.gpx_animator.core.data;

import app.gpx_animator.core.preferences.Preferences;

import javax.swing.JComboBox;
import java.util.Arrays;
import java.util.Locale;
import java.util.ResourceBundle;

import org.jetbrains.annotations.NotNull;

public enum Position {

    HIDDEN,
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

    public static Position parse(@NotNull final String p) {
        return Position.valueOf(p.replace(' ', '_').toUpperCase(Locale.getDefault()));
    }

    public static void fillComboBox(@NotNull final JComboBox<Position> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

}
