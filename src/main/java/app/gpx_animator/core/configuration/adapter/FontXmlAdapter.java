package app.gpx_animator.core.configuration.adapter;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.adapters.XmlAdapter;
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
