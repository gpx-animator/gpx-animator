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

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.DefaultFormatterFactory;

import sk.freemap.gpxAnimator.ui.DurationSpinnerModel.Field;

public class DurationEditor extends DefaultEditor {
	
	private static final long serialVersionUID = -3860212824757198990L;

	
	public DurationEditor(final JSpinner spinner) {
		super(spinner);

		final JFormattedTextField ftf = getTextField();
		
		ftf.addCaretListener(new CaretListener() {
			@Override
			public void caretUpdate(final CaretEvent e) {
				final SpinnerModel model = spinner.getModel();
				if (!(model instanceof DurationSpinnerModel)) {
					return;
				}
				
				final DurationSpinnerModel dsm = (DurationSpinnerModel) model;
				
				// special hack to select number
				if (!ftf.isValid()) {
					final Field field = dsm.getField();
					final Matcher matcher = Pattern.compile("(\\d+)\\s*" + field.getUnit() + "\\b").matcher(ftf.getText());
					if (matcher.find()) {
						SwingUtilities.invokeLater(new Runnable() {
							@Override
							public void run() {
								ftf.setSelectionStart(matcher.start(1));
								ftf.setSelectionEnd(matcher.end(1));
							}
						});
					}
					
					return;
				}
				
				final String text = ftf.getText();
				final int n = text.length();
						
				int i = e.getDot();
				
				while (i < n && !Character.isLowerCase(text.charAt(i))) {
					i++;
				}
				
				while (i > 1 && Character.isLowerCase(text.charAt(i - 1))) {
					i--;
				}
				
				final StringBuilder sb = new StringBuilder();
				for (; i < n && Character.isLowerCase(text.charAt(i)); i++) {
					sb.append(text.charAt(i));
				}
				
				final Field field = Field.fromUnit(sb.toString());
				
				if (field != null) {
					dsm.setField(field);
				}
			}
		});
		
		ftf.setEditable(true);
		ftf.setFormatterFactory(new DefaultFormatterFactory(new AbstractFormatter() {
			private static final long serialVersionUID = -6817456936657252534L;

			@Override
			public String valueToString(final Object value) throws ParseException {
				if (value == null) {
					return "";
				}
				
				long l = (Long) value;
				
				if (l == 0) {
					return "";
				}
				
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
			
			@Override
			public Object stringToValue(final String text) throws ParseException {
				if (text.trim().isEmpty()) {
					return null;
				}
				
				final Pattern pattern = Pattern.compile("\\s*" +
						"(?:(\\d+)\\s*d\\s*)?" +
						"(?:(\\d+)\\s*h\\s*)?" +
						"(?:(\\d+)\\s*m\\s*)?" +
						"(?:(\\d+)\\s*s\\s*)?" +
						"(?:(\\d+)\\s*ms\\s*)?");
				
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
		}));
		
		ftf.setHorizontalAlignment(JTextField.RIGHT);
	}

}
