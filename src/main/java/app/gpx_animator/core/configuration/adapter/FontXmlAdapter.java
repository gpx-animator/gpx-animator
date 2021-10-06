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
package app.gpx_animator.core.configuration.adapter;

import edu.umd.cs.findbugs.annotations.NonNull;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import org.jetbrains.annotations.Nullable;

import java.awt.Font;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class FontXmlAdapter extends XmlAdapter<String, Font> {

    @Override
    public Font unmarshal(@Nullable final String string) {
        return Font.decode(string);
    }

    @Override
    public String marshal(@Nullable final Font font) {
        return font != null ? encode(font) : null;
    }

    private String encode(@NonNull final Font font) {
        final var name = font.getName();
        final var style = getStyle(font);
        final var size = font.getSize();
        return "%s-%s-%d".formatted(name, style, size);
    }

    private String getStyle(@NonNull final Font font) {
        if (font.isBold() && font.isItalic()) {
            return "BOLDITALIC";
        } else if (font.isBold()) {
            return "BOLD";
        } else if (font.isItalic()) {
            return "ITALIC";
        } else {
            return "PLAIN";
        }
    }

}
