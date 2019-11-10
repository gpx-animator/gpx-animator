package sk.freemap.gpxAnimator.ui;

import javax.swing.*;
import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class DurationFormatter extends JFormattedTextField.AbstractFormatter {

    private static final long serialVersionUID = -6817456936657252534L;

    @Override
    public String valueToString(final Object value) throws ParseException {

        if(isValid(value)) {
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

        final Pattern pattern = Pattern.compile("\\s*" +
                "-?(?:(\\d+)\\s*d\\s*)?" +
                "-?(?:(\\d+)\\s*h\\s*)?" +
                "-?(?:(\\d+)\\s*m\\s*)?" +
                "-?(?:(\\d+)\\s*s\\s*)?" +
                "-?(?:(\\d+)\\s*ms\\s*)?");

        final Matcher matcher = pattern.matcher(text);

        if (matcher.matches()) {
            long result = 0;
            long mul = 1;

            final long[] muls = new long[] { 1, 1000, 60, 60, 24 };

            for (int i = 5; i > 0; i--) {
                mul *= muls[5 - i];
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

    private boolean isValid(Object value) {
        boolean isValid=true;
        if (value == null) {
            isValid=false;
        }else if ((Long)value == 0) {
            isValid=false;
        }
        return isValid;
    }

    private String getDurationAsText(long l) {
        final StringBuilder sb = new StringBuilder();

        sb.insert(0, "ms");
        sb.insert(0, l % 1000);
        l /= 1000;

        sb.insert(0, "s ");
        sb.insert(0, l % 60);
        l /= 60;

        sb.insert(0, "m ");
        sb.insert(0, l % 60);
        l /= 60;

        sb.insert(0, "h ");
        sb.insert(0, l % 24);
        l /= 24;

        sb.insert(0, "d ");
        sb.insert(0, l);

        return sb.toString();
    }
}