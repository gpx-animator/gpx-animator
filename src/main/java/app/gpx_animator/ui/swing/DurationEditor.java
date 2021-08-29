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

import app.gpx_animator.ui.swing.DurationSpinnerModel.Field;

import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.text.DefaultFormatterFactory;
import java.io.Serial;
import java.util.regex.Pattern;

public class DurationEditor extends DefaultEditor {

    @Serial
    private static final long serialVersionUID = -3860212824757198990L;


    public DurationEditor(final JSpinner spinner) {
        super(spinner);

        final var ftf = getTextField();

        ftf.addCaretListener(e -> {
            final var model = spinner.getModel();
            if (!(model instanceof final DurationSpinnerModel dsm)) {
                return;
            }

            // special hack to select number
            if (!ftf.isValid()) {
                final var field = dsm.getField();
                @SuppressWarnings("RegExpAnonymousGroup") // This regex is tested and I don't want to rewrite it which may potentionally break it.
                final var matcher = Pattern.compile("(\\d+)\\s*" + field.getUnit() + "\\b").matcher(ftf.getText());
                if (matcher.find()) {
                    SwingUtilities.invokeLater(() -> {
                        ftf.setSelectionStart(matcher.start(1));
                        ftf.setSelectionEnd(matcher.end(1));
                    });
                }

                return;
            }

            final var text = ftf.getText();
            final var n = text.length();

            var i = e.getDot();

            while (i < n && !Character.isLowerCase(text.charAt(i))) {
                i++;
            }

            while (i > 1 && Character.isLowerCase(text.charAt(i - 1))) {
                i--;
            }

            final var sb = new StringBuilder();
            for (; i < n && Character.isLowerCase(text.charAt(i)); i++) {
                sb.append(text.charAt(i));
            }

            final var field = Field.fromUnit(sb.toString());

            if (field != null) {
                dsm.setField(field);
            }
        });

        ftf.setEditable(true);
        ftf.setFormatterFactory(new DefaultFormatterFactory(new DurationFormatter()));

        ftf.setHorizontalAlignment(SwingConstants.RIGHT);
    }

}
