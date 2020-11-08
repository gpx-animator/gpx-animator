package app.gpx_animator.ui;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.awt.Font;

public class FontComboBoxRenderer extends JLabel implements ListCellRenderer<Integer> {

    public static final long serialVersionUID = 1L;

    private final transient String[] fontFamilyNames;

    public FontComboBoxRenderer(final String[] fontFamilyNames) {
        this.fontFamilyNames = fontFamilyNames.clone();
    }

    /**
     * @see ListCellRenderer#getListCellRendererComponent(JList, Object, int, boolean, boolean)
     */
    @Override
    public Component getListCellRendererComponent(final JList list, final Integer value, final int index, final boolean isSelected,
                                                  final boolean cellHasFocus) {
           int offset = value;
           final String name = fontFamilyNames[offset];
           setText(name);
           setFont(new Font(name, Font.PLAIN, 20));
           return this;
        }
     }
