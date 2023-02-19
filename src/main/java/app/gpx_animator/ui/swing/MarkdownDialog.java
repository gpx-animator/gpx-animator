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

import app.gpx_animator.core.preferences.Preferences;
import com.jgoodies.forms.builder.FormBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.ast.Node;
import com.vladsch.flexmark.util.data.MutableDataSet;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.HyperlinkEvent;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.io.Serial;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class MarkdownDialog extends EscapeDialog {

    @Serial
    private static final long serialVersionUID = 3880814150593572969L;

    private static final String TEMPLATE = """
            <html lang="en"><head>
                <meta charset="utf-8" />
                <title>%s</title>
                <style>
                    body {
                        font-family: sans-serif;
                        font-size: 12px;
                    }
                    h1 {
                        font-size: 24px;
                        font-weight: bold;
                    }
                    h2 {
                        font-size: 20px;
                        font-weight: bold;
                    }
                    h3 {
                        font-size: 16px;
                        font-weight: bold;
                        margin-top: 24px;
                        margin-bottom: 0;
                    }
                    h4 {
                        font-size: 12px;
                        font-weight: bold;
                        margin-top: 12px;
                        margin-bottom: 0;
                    }
                    p {
                        margin-top: 6px;
                        margin-bottom: 6px;
                    }
                </style>
            </head><body>
                %s
            </body>""";

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final String markdown;
    private final Map<String, String> variables;

    public MarkdownDialog(final JFrame owner, final String title,
                          @NonNls final String markdown,
                          final int width, final int height) {
        this(owner, title, markdown, new HashMap<>(), width, height);
    }

    public MarkdownDialog(final JFrame owner, final String title,
                          @NonNls final String markdown,
                          final Map<String, String> variables,
                          final int width, final int height) {
        super(owner);
        this.markdown = markdown;
        this.variables = Map.copyOf(variables);
        setTitle(title);
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        setContentPane(buildContent());
        setSize(width, height);
        setLocationRelativeTo(owner);
        setVisible(true);
    }

    private JComponent buildContent() {
        final var textPane = new JTextPane();
        textPane.setEditable(false);
        textPane.setContentType("text/html");
        textPane.setText(parseVariables());
        textPane.setCaretPosition(0);
        textPane.addHyperlinkListener(this::handleLinkClicked);

        final var scrollPane = new JScrollPane(textPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        final var closeButton = new JButton(resourceBundle.getString("ui.dialog.markdown.button.close"));
        closeButton.addActionListener(e -> SwingUtilities.invokeLater(this::closeDialog));

        return FormBuilder.create()
                .padding(new EmptyBorder(20, 20, 20, 20))
                .columns("fill:200dlu:grow") //NON-NLS
                .rows("fill:100dlu:grow, 10dlu, p") //NON-NLS
                .add(scrollPane).xy(1, 1)
                .addBar(closeButton).xy(1, 3, CellConstraints.RIGHT, CellConstraints.FILL)
                .build();
    }

    private void handleLinkClicked(@NotNull final HyperlinkEvent event) {
        if (event.getEventType().equals(HyperlinkEvent.EventType.ACTIVATED)
                && Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().browse(event.getURL().toURI());
            } catch (final IOException | URISyntaxException e) {
                new ErrorDialog(MainFrame.getInstance(), e.getLocalizedMessage(), e);
            }
        }
    }

    private void closeDialog() {
        setVisible(false);
        dispose();
    }

    private String parseVariables() {
        var html = readFileAsHTML();
        for (var variable : variables.entrySet()) {
            final var placeholder = "\\{\\{".concat(variable.getKey()).concat("\\s*\\}\\}"); //NON-NLS
            html = html.replaceAll(placeholder, variable.getValue());
        }
        return html;
    }

    private String readFileAsHTML() {
        final var html = convertMarkdownToHTML(markdown);
        return TEMPLATE.formatted(getTitle(), html);
    }

    private String convertMarkdownToHTML(final String md) {
        final var options = new MutableDataSet();
        final var parser = Parser.builder(options).build();
        final var renderer = HtmlRenderer.builder(options).build();
        final Node document = parser.parse(md);
        return renderer.render(document);
    }

}
