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

package app.gpx_animator.ui;

import javax.swing.JFormattedTextField;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurationFormatter extends JFormattedTextField.AbstractFormatter {

    private static final long serialVersionUID = -6817456936657252534L;

    @Override
    public String valueToString(final Object value) {

        if (isValid(value)) {
            long l = (Long) value;
            return getDurationAsText(l);
        }
        return "";
    }

    @Override
    public Object stringToValue(final String text) throws ParseException {
        if (text.trim().isEmpty()) {
            return null;
        }

        @SuppressWarnings("RegExpAnonymousGroup") // This regex is tested and I don't want to rewrite it which may potentionally break it.
        final Pattern pattern = Pattern.compile("\\s*"
                + "(?:(-?\\d+)\\s*d\\s*)?"
                + "(?:(-?\\d+)\\s*h\\s*)?"
                + "(?:(-?\\d+)\\s*m\\s*)?"
                + "(?:(-?\\d+)\\s*s\\s*)?"
                + "(?:(-?\\d+)\\s*ms\\s*)?");

        final Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            long result = 0;
            long mul = 1;

            final long[] multiplier = new long[]{1, 1000, 60, 60, 24};

            for (int i = 5; i > 0; i--) {
                mul *= multiplier[5 - i];
                if (matcher.group(i) != null) {
                    result += Long.parseLong(matcher.group(i)) * mul;
                }
            }
            return result;
        } else if (text.matches("\\s*\\d+\\s*")) {
            return Long.valueOf(text);
        } else {
            throw new ParseException("invalid format", 0);
        }
    }

    private boolean isValid(final Object value) {
        boolean isValid = true;
        if (value == null) {
            isValid = false;
        }
        return isValid;
    }

    private String getDurationAsText(final long duration) {
        final StringBuilder sb = new StringBuilder();

        long l = duration;

        sb.insert(0, "ms"); //NON-NLS
        sb.insert(0, l % 1000);
        l /= 1000;

        sb.insert(0, "s "); //NON-NLS
        sb.insert(0, l % 60);
        l /= 60;

        sb.insert(0, "m "); //NON-NLS
        sb.insert(0, l % 60);
        l /= 60;

        sb.insert(0, "h "); //NON-NLS
        sb.insert(0, l % 24);
        l /= 24;

        sb.insert(0, "d "); //NON-NLS
        sb.insert(0, l);

        return sb.toString();
    }
}
