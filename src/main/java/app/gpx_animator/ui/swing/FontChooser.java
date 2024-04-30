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

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.preferences.Preferences;
import edu.umd.cs.findbugs.annotations.NonNull;

import javax.swing.ScrollPaneConstants;

import javax.swing.SwingConstants;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;
import java.util.ResourceBundle;

public final class FontChooser extends JComponent {

    @Serial
    private static final long serialVersionUID = 6085184669738380002L;

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(FontChooser.class);

    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     */
    public static final int OK_OPTION = 0;
    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     */
    public static final int CANCEL_OPTION = 1;
    /**
     * Return value from <code>showDialog()</code>.
     *
     * @see #showDialog
     */
    public static final int ERROR_OPTION = -1;

    private static final int[] FONT_STYLE_CODES = {
            Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC
    };

    private static final String[] DEFAULT_FONT_SIZE_STRINGS = {
            "8", "9", "10", "11", "12", "14", "16", "18", "20",
            "22", "24", "26", "28", "36", "48", "72" };

    // instance variables
    private int dialogResultValue = ERROR_OPTION;

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final String[] fontSizeStrings;

    private String[] fontStyleNames = null;
    private String[] fontFamilyNames = null;
    private JTextField fontFamilyTextField = null;
    private JTextField fontStyleTextField = null;
    private JTextField fontSizeTextField = null;
    private JList<String> fontNameList = null;
    private JList<String> fontStyleList = null;
    private JList<String> fontSizeList = null;
    private JPanel fontNamePanel = null;
    private JPanel fontStylePanel = null;
    private JPanel fontSizePanel = null;
    private JPanel samplePanel = null;
    private JTextField sampleText = null;

    /**
     * Constructs a <code>FontChooser</code> object.
     **/
    public FontChooser() {
        this(DEFAULT_FONT_SIZE_STRINGS);
    }

    /**
     * Constructs a <code>FontChooser</code> object using the given font size array.
     *
     * @param fontSizeStrings the array of font size string.
     **/
    public FontChooser(final String... fontSizeStrings) {
        this.fontSizeStrings = fontSizeStrings != null ? fontSizeStrings : DEFAULT_FONT_SIZE_STRINGS;

        final var selectPanel = new JPanel();
        selectPanel.setLayout(new BoxLayout(selectPanel, BoxLayout.X_AXIS));
        selectPanel.add(getFontFamilyPanel());
        selectPanel.add(getFontStylePanel());
        selectPanel.add(getFontSizePanel());

        final var contentsPanel = new JPanel();
        contentsPanel.setLayout(new GridLayout(2, 1));
        contentsPanel.add(selectPanel, BorderLayout.NORTH);
        contentsPanel.add(getSamplePanel(), BorderLayout.CENTER);

        this.setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        this.add(contentsPanel);
        this.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        this.setSelectedFont(Constants.DEFAULT_FONT);
    }

    private JTextField getFontFamilyTextField() {
        if (fontFamilyTextField == null) {
            fontFamilyTextField = new JTextField();
            fontFamilyTextField.addFocusListener(
                    new TextFieldFocusHandlerForTextSelection(fontFamilyTextField));
            fontFamilyTextField.addKeyListener(
                    new TextFieldKeyHandlerForListSelectionUpDown(getFontFamilyList()));
            fontFamilyTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(getFontFamilyList()));

        }
        return fontFamilyTextField;
    }

    private JTextField getFontStyleTextField() {
        if (fontStyleTextField == null) {
            fontStyleTextField = new JTextField();
            fontStyleTextField.addFocusListener(
                    new TextFieldFocusHandlerForTextSelection(fontStyleTextField));
            fontStyleTextField.addKeyListener(
                    new TextFieldKeyHandlerForListSelectionUpDown(getFontStyleList()));
            fontStyleTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(getFontStyleList()));
        }
        return fontStyleTextField;
    }

    private JTextField getFontSizeTextField() {
        if (fontSizeTextField == null) {
            fontSizeTextField = new JTextField();
            fontSizeTextField.addFocusListener(
                    new TextFieldFocusHandlerForTextSelection(fontSizeTextField));
            fontSizeTextField.addKeyListener(
                    new TextFieldKeyHandlerForListSelectionUpDown(getFontSizeList()));
            fontSizeTextField.getDocument().addDocumentListener(
                    new ListSearchTextFieldDocumentHandler(getFontSizeList()));
        }
        return fontSizeTextField;
    }

    private JList<String> getFontFamilyList() {
        if (fontNameList == null) {
            fontNameList = new JList<>(getFontFamilies());
            fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontNameList.addListSelectionListener(
                    new ListSelectionHandler(getFontFamilyTextField()));
            fontNameList.setSelectedIndex(0);
            fontNameList.setFocusable(false);
        }
        return fontNameList;
    }

    private JList<String> getFontStyleList() {
        if (fontStyleList == null) {
            fontStyleList = new JList<>(getFontStyleNames());
            fontStyleList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontStyleList.addListSelectionListener(
                    new ListSelectionHandler(getFontStyleTextField()));
            fontStyleList.setSelectedIndex(0);
            fontStyleList.setFocusable(false);
        }
        return fontStyleList;
    }

    private JList<String> getFontSizeList() {
        if (fontSizeList == null) {
            fontSizeList = new JList<>(this.fontSizeStrings);
            fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            fontSizeList.addListSelectionListener(
                    new ListSelectionHandler(getFontSizeTextField()));
            fontSizeList.setSelectedIndex(0);
            fontSizeList.setFocusable(false);
        }
        return fontSizeList;
    }

    /**
     * Get the family name of the selected font.
     *
     * @return the font family of the selected font.
     * @see #setSelectedFontFamily
     **/
    public String getSelectedFontFamily() {
        return getFontFamilyList().getSelectedValue();
    }

    /**
     * Get the style of the selected font.
     *
     * @return the style of the selected font.
     * <code>Font.PLAIN</code>, <code>Font.BOLD</code>,
     * <code>Font.ITALIC</code>, <code>Font.BOLD|Font.ITALIC</code>
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #setSelectedFontStyle
     **/
    public int getSelectedFontStyle() {
        final var index = getFontStyleList().getSelectedIndex();
        return FONT_STYLE_CODES[index];
    }

    /**
     * Get the size of the selected font.
     *
     * @return the size of the selected font
     * @see #setSelectedFontSize
     **/
    public int getSelectedFontSize() {
        int fontSize;
        var fontSizeString = getFontSizeTextField().getText();
        while (true) {
            try {
                fontSize = Integer.parseInt(fontSizeString);
                break;
            } catch (final NumberFormatException e) {
                fontSizeString = getFontSizeList().getSelectedValue();
                getFontSizeTextField().setText(fontSizeString);
            }
        }

        return fontSize;
    }

    /**
     * Get the selected font.
     *
     * @return the selected font
     * @see #setSelectedFont
     * @see java.awt.Font
     **/
    public Font getSelectedFont() {
        //noinspection MagicConstant
        return new Font(getSelectedFontFamily(),
                getSelectedFontStyle(), getSelectedFontSize());
    }

    /**
     * Set the family name of the selected font.
     *
     * @param name the family name of the selected font.
     * @see #getSelectedFontFamily
     **/
    public void setSelectedFontFamily(@NonNull final String name) {
        final var names = getFontFamilies();
        for (var i = 0; i < names.length; i++) {
            if (names[i].equalsIgnoreCase(name)) {
                getFontFamilyList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    /**
     * Set the style of the selected font.
     *
     * @param style the size of the selected font.
     *              <code>Font.PLAIN</code>, <code>Font.BOLD</code>,
     *              <code>Font.ITALIC</code>, or
     *              <code>Font.BOLD|Font.ITALIC</code>.
     * @see java.awt.Font#PLAIN
     * @see java.awt.Font#BOLD
     * @see java.awt.Font#ITALIC
     * @see #getSelectedFontStyle
     **/
    public void setSelectedFontStyle(final int style) {
        for (var i = 0; i < FONT_STYLE_CODES.length; i++) {
            if (FONT_STYLE_CODES[i] == style) {
                getFontStyleList().setSelectedIndex(i);
                break;
            }
        }
        updateSampleFont();
    }

    /**
     * Set the size of the selected font.
     *
     * @param size the size of the selected font
     * @see #getSelectedFontSize
     **/
    public void setSelectedFontSize(final int size) {
        final var sizeString = String.valueOf(size);
        for (var i = 0; i < this.fontSizeStrings.length; i++) {
            if (this.fontSizeStrings[i].equals(sizeString)) {
                getFontSizeList().setSelectedIndex(i);
                break;
            }
        }
        getFontSizeTextField().setText(sizeString);
        updateSampleFont();
    }

    /**
     * Set the selected font.
     *
     * @param font the selected font
     * @see #getSelectedFont
     * @see java.awt.Font
     **/
    public void setSelectedFont(final Font font) {
        setSelectedFontFamily(font.getFamily());
        setSelectedFontStyle(font.getStyle());
        setSelectedFontSize(font.getSize());
    }

    /**
     * Show font selection dialog.
     *
     * @param parent Dialog's Parent component.
     * @return OK_OPTION, CANCEL_OPTION or ERROR_OPTION
     * @see #OK_OPTION
     * @see #CANCEL_OPTION
     * @see #ERROR_OPTION
     **/
    public int showDialog(final Component parent) {
        dialogResultValue = ERROR_OPTION;
        final var dialog = createDialog(parent);
        dialog.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(final WindowEvent e) {
                dialogResultValue = CANCEL_OPTION;
            }
        });

        dialog.setVisible(true);
        dialog.dispose();

        return dialogResultValue;
    }

    class ListSelectionHandler implements ListSelectionListener {

        private final JTextComponent textComponent;

        ListSelectionHandler(final JTextComponent textComponent) {
            this.textComponent = textComponent;
        }

        @Override
        public void valueChanged(final ListSelectionEvent e) {
            if (!e.getValueIsAdjusting()) {
                @SuppressWarnings("unchecked")
                final var list = (JList<String>) e.getSource();
                final var selectedValue = list.getSelectedValue();
                final var oldValue = textComponent.getText();
                textComponent.setText(selectedValue);
                if (!oldValue.equalsIgnoreCase(selectedValue)) {
                    textComponent.selectAll();
                    textComponent.requestFocus();
                }

                updateSampleFont();
            }
        }
    }

    class TextFieldFocusHandlerForTextSelection extends FocusAdapter {

        private final JTextComponent textComponent;

        TextFieldFocusHandlerForTextSelection(final JTextComponent textComponent) {
            this.textComponent = textComponent;
        }

        @Override
        public void focusGained(final FocusEvent e) {
            textComponent.selectAll();
        }

        @Override
        public void focusLost(final FocusEvent e) {
            textComponent.select(0, 0);
            updateSampleFont();
        }
    }

    static class TextFieldKeyHandlerForListSelectionUpDown extends KeyAdapter {

        private final JList<String> targetList;

        TextFieldKeyHandlerForListSelectionUpDown(final JList<String> list) {
            this.targetList = list;
        }

        @Override
        public void keyPressed(final KeyEvent e) {
            switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                    var up = targetList.getSelectedIndex() - 1;
                    if (up < 0) {
                        up = 0;
                    }
                    targetList.setSelectedIndex(up);
                    break;
                case KeyEvent.VK_DOWN:
                    var listSize = targetList.getModel().getSize();
                    var down = targetList.getSelectedIndex() + 1;
                    if (down >= listSize) {
                        down = listSize - 1;
                    }
                    targetList.setSelectedIndex(down);
                    break;
                default:
                    break;
            }
        }
    }

    @SuppressWarnings("ClassCanBeRecord") // in my opinion this should not be a record
    static class ListSearchTextFieldDocumentHandler implements DocumentListener {

        private final JList<String> targetList;

        ListSearchTextFieldDocumentHandler(final JList<String> targetList) {
            this.targetList = targetList;
        }

        @Override
        public void insertUpdate(final DocumentEvent e) {
            update(e);
        }

        @Override
        public void removeUpdate(final DocumentEvent e) {
            update(e);
        }

        @Override
        public void changedUpdate(final DocumentEvent e) {
            update(e);
        }

        private void update(final DocumentEvent event) {
            var newValue = "";
            try {
                final var doc = event.getDocument();
                newValue = doc.getText(0, doc.getLength());
            } catch (final BadLocationException ex) {
                LOGGER.error("update(DocumentEvent) exception", ex);
            }

            if (!newValue.isEmpty()) {
                var index = targetList.getNextMatch(newValue, 0, Position.Bias.Forward);
                if (index < 0) {
                    index = 0;
                }
                targetList.ensureIndexIsVisible(index);

                final var matchedName = targetList.getModel().getElementAt(index);
                if (newValue.equalsIgnoreCase(matchedName) && index != targetList.getSelectedIndex()) {
                    SwingUtilities.invokeLater(new ListSelector(index));
                }
            }
        }

        public class ListSelector implements Runnable {

            private final int index;

            ListSelector(final int index) {
                this.index = index;
            }

            @Override
            public void run() {
                targetList.setSelectedIndex(this.index);
            }
        }
    }

    class DialogOKAction extends AbstractAction {

        protected static final String ACTION_NAME = "OK";
        @Serial
        private static final long serialVersionUID = 2309978224812073950L;
        private final JDialog dialog;

        @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
        protected DialogOKAction(final JDialog dialog) {
            this.dialog = dialog;
            putValue(Action.DEFAULT, ACTION_NAME);
            putValue(Action.ACTION_COMMAND_KEY, ACTION_NAME);
            putValue(Action.NAME, resourceBundle.getString("ui.dialog.fontchooser.button.ok"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            dialogResultValue = OK_OPTION;
            dialog.setVisible(false);
        }
    }

    class DialogCancelAction extends AbstractAction {

        protected static final String ACTION_NAME = "Cancel";
        @Serial
        private static final long serialVersionUID = -5080686015935861376L;
        private final JDialog dialog;

        @SuppressWarnings("PMD.ConstructorCallsOverridableMethod")
        protected DialogCancelAction(final JDialog dialog) {
            this.dialog = dialog;
            putValue(Action.DEFAULT, ACTION_NAME);
            putValue(Action.ACTION_COMMAND_KEY, ACTION_NAME);
            putValue(Action.NAME, resourceBundle.getString("ui.dialog.fontchooser.button.cancel"));
        }

        @Override
        public void actionPerformed(final ActionEvent e) {
            dialogResultValue = CANCEL_OPTION;
            dialog.setVisible(false);
        }
    }

    private JDialog createDialog(final Component parent) {
        final var frame = parent instanceof Frame ? (Frame) parent
                : (Frame) SwingUtilities.getAncestorOfClass(Frame.class, parent);
        final var dialog = new JDialog(frame, resourceBundle.getString("ui.dialog.fontchooser.title"), true);

        final Action okAction = new DialogOKAction(dialog);
        final Action cancelAction = new DialogCancelAction(dialog);

        final var okButton = new JButton(okAction);
        final var cancelButton = new JButton(cancelAction);

        final var buttonsPanel = new JPanel();
        buttonsPanel.setLayout(new GridLayout(2, 1));
        buttonsPanel.add(okButton);
        buttonsPanel.add(cancelButton);
        buttonsPanel.setBorder(BorderFactory.createEmptyBorder(25, 0, 10, 10));

        final var actionMap = buttonsPanel.getActionMap();
        actionMap.put(cancelAction.getValue(Action.DEFAULT), cancelAction);
        actionMap.put(okAction.getValue(Action.DEFAULT), okAction);
        final var inputMap = buttonsPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        inputMap.put(KeyStroke.getKeyStroke("ESCAPE"), cancelAction.getValue(Action.DEFAULT));
        inputMap.put(KeyStroke.getKeyStroke("ENTER"), okAction.getValue(Action.DEFAULT));

        final var dialogEastPanel = new JPanel();
        dialogEastPanel.setLayout(new BorderLayout());
        dialogEastPanel.add(buttonsPanel, BorderLayout.NORTH);

        dialog.getContentPane().add(this, BorderLayout.CENTER);
        dialog.getContentPane().add(dialogEastPanel, BorderLayout.EAST);
        dialog.pack();
        dialog.setLocationRelativeTo(frame);
        return dialog;
    }

    private void updateSampleFont() {
        final var font = getSelectedFont();
        getSampleTextField().setFont(font);
    }

    private JPanel getFontFamilyPanel() {
        if (fontNamePanel == null) {
            fontNamePanel = new JPanel();
            fontNamePanel.setLayout(new BorderLayout());
            fontNamePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            fontNamePanel.setPreferredSize(new Dimension(180, 130));

            final var scrollPane = new JScrollPane(getFontFamilyList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final var p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontFamilyTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final var label = new JLabel(resourceBundle.getString("ui.dialog.fontchooser.label.fontname"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontFamilyTextField());
            label.setDisplayedMnemonic('F');

            fontNamePanel.add(label, BorderLayout.NORTH);
            fontNamePanel.add(p, BorderLayout.CENTER);

        }
        return fontNamePanel;
    }

    private JPanel getFontStylePanel() {
        if (fontStylePanel == null) {
            fontStylePanel = new JPanel();
            fontStylePanel.setLayout(new BorderLayout());
            fontStylePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
            fontStylePanel.setPreferredSize(new Dimension(140, 130));

            final var scrollPane = new JScrollPane(getFontStyleList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final var p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontStyleTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final var label = new JLabel(resourceBundle.getString("ui.dialog.fontchooser.label.fontstyle"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontStyleTextField());
            label.setDisplayedMnemonic('Y');

            fontStylePanel.add(label, BorderLayout.NORTH);
            fontStylePanel.add(p, BorderLayout.CENTER);
        }
        return fontStylePanel;
    }

    private JPanel getFontSizePanel() {
        if (fontSizePanel == null) {
            fontSizePanel = new JPanel();
            fontSizePanel.setLayout(new BorderLayout());
            fontSizePanel.setPreferredSize(new Dimension(70, 130));
            fontSizePanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

            final var scrollPane = new JScrollPane(getFontSizeList());
            scrollPane.getVerticalScrollBar().setFocusable(false);
            scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);

            final var p = new JPanel();
            p.setLayout(new BorderLayout());
            p.add(getFontSizeTextField(), BorderLayout.NORTH);
            p.add(scrollPane, BorderLayout.CENTER);

            final var label = new JLabel(resourceBundle.getString("ui.dialog.fontchooser.label.fontsize"));
            label.setHorizontalAlignment(SwingConstants.LEFT);
            label.setHorizontalTextPosition(SwingConstants.LEFT);
            label.setLabelFor(getFontSizeTextField());
            label.setDisplayedMnemonic('S');

            fontSizePanel.add(label, BorderLayout.NORTH);
            fontSizePanel.add(p, BorderLayout.CENTER);
        }
        return fontSizePanel;
    }

    private JPanel getSamplePanel() {
        if (samplePanel == null) {
            final Border titledBorder = BorderFactory.createTitledBorder(
                    BorderFactory.createEtchedBorder(), resourceBundle.getString("ui.dialog.fontchooser.label.sample"));
            final var empty = BorderFactory.createEmptyBorder(5, 10, 10, 10);
            final Border border = BorderFactory.createCompoundBorder(titledBorder, empty);

            samplePanel = new JPanel();
            samplePanel.setLayout(new BorderLayout());
            samplePanel.setBorder(border);

            samplePanel.add(getSampleTextField(), BorderLayout.CENTER);
        }
        return samplePanel;
    }

    private JTextField getSampleTextField() {
        if (sampleText == null) {
            final var lowered = BorderFactory.createLoweredBevelBorder();

            sampleText = new JTextField(Constants.APPNAME_VERSION);
            sampleText.setBorder(lowered);
            sampleText.setPreferredSize(new Dimension(300, 100));
        }
        return sampleText;
    }

    private String[] getFontFamilies() {
        if (fontFamilyNames == null) {
            final var env = GraphicsEnvironment.getLocalGraphicsEnvironment();
            fontFamilyNames = env.getAvailableFontFamilyNames();
        }
        return fontFamilyNames;
    }

    private String[] getFontStyleNames() {
        if (fontStyleNames == null) {
            var i = 0;
            fontStyleNames = new String[4];
            fontStyleNames[i++] = resourceBundle.getString("ui.dialog.fontchooser.fontstyle.plain");
            fontStyleNames[i++] = resourceBundle.getString("ui.dialog.fontchooser.fontstyle.bold");
            fontStyleNames[i++] = resourceBundle.getString("ui.dialog.fontchooser.fontstyle.italic");
            fontStyleNames[i] = resourceBundle.getString("ui.dialog.fontchooser.fontstyle.bolditalic");
        }
        return fontStyleNames;
    }
}
