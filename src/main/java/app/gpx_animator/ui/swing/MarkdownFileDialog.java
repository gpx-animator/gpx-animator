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

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;

import javax.swing.JFrame;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Serial;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class MarkdownFileDialog extends MarkdownDialog {

    @Serial
    private static final long serialVersionUID = 5068769330531211095L;

    public MarkdownFileDialog(final JFrame owner, final String title,
                              @NonNls final String filename,
                              final int width, final int height) {
        this(owner, title, filename, new HashMap<>(), width, height);
    }

    public MarkdownFileDialog(final JFrame owner, final String title,
                              @NonNls final String filename,
                              final Map<String, String> variables,
                              final int width, final int height) {
        super(owner, title, readFileAsMarkdown(filename), variables, width, height);
    }

    private static String readFileAsMarkdown(@NonNull final String filename) {
        final var classLoader = ClassLoader.getSystemClassLoader();
        try (var is = classLoader.getResourceAsStream(filename)) {
            if (is == null) {
                return "";
            }
            try (var isr = new InputStreamReader(is, StandardCharsets.UTF_8);
                var reader = new BufferedReader(isr)) {
                return reader.lines().collect(Collectors.joining(System.lineSeparator()));
            }
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }

}
