package sk.freemap.gpxAnimator.ui;

import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;

public class EmptyZeroNumberEditor extends DefaultEditor {

	private static final long serialVersionUID = -3860212824757198990L;

	
	public EmptyZeroNumberEditor(final JSpinner spinner) {
		super(spinner);

		final JFormattedTextField ftf = getTextField();
		ftf.setEditable(true);
		ftf.setFormatterFactory(new DefaultFormatterFactory(new AbstractFormatter() {
			private static final long serialVersionUID = -6817456936657252534L;

			@Override
			public String valueToString(final Object value) throws ParseException {
				return value == null ? "" : value.toString();
			}
			
			@Override
			public Object stringToValue(final String text) throws ParseException {
				try {
					return text.isEmpty() ? null : Integer.parseInt(text);
				} catch (final NumberFormatException e) {
					throw new ParseException(text, 0);
				}
			}
		}));
		ftf.setHorizontalAlignment(JTextField.RIGHT);
	}

}
