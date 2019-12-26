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

import javax.swing.JFormattedTextField;
import javax.swing.JFormattedTextField.AbstractFormatter;
import javax.swing.JSpinner;
import javax.swing.JSpinner.DefaultEditor;
import javax.swing.JTextField;
import javax.swing.text.DefaultFormatterFactory;
import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

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
                    } catch (final Throwable e1) { // NOPMD -- The cause of InvocationTargetException is a Throwable
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
