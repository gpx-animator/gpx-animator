package sk.freemap.gpxAnimator.ui;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;

public class EmptyZeroNumberEditor extends DefaultEditor {

	private static final long serialVersionUID = -3860212824757198990L;

	
	public EmptyZeroNumberEditor(final JSpinner spinner, final Class<? extends Number> clazz) {
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
				if (text.isEmpty()) {
					return null;
				}
				
				try {
					return clazz.getConstructor(String.class).newInstance(text);
				} catch (final NumberFormatException e) {
					throw new ParseException(text, 0);
				} catch (final InstantiationException e) {
					throw new RuntimeException(e);
				} catch (final IllegalAccessException e) {
					throw new RuntimeException(e);
				} catch (final InvocationTargetException e) {
					try {
						throw e.getCause();
					} catch (final NumberFormatException e1) {
						throw new ParseException(text, 0);
					} catch (final Throwable e1) {
						throw new RuntimeException(e1);
					}
				} catch (final NoSuchMethodException e) {
					throw new RuntimeException(e);
				}
			}
		}));
		ftf.setHorizontalAlignment(JTextField.RIGHT);
	}

}
