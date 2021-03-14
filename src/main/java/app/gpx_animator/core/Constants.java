package app.gpx_animator.core;

import java.awt.Font;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

@SuppressWarnings("checkstyle:NoWhitespaceBefore") // For the ";" after the class declaration which is needed to use an enum for constants only
public enum Constants {
    ;

    public static final String APPNAME = "GPX Animator"; //NON-NLS

    public static final String VERSION = loadVersionString();

    public static final String APPNAME_VERSION = APPNAME.concat(" ").concat(VERSION);

    public static final String YEAR = "2021";

    public static final String COPYRIGHT = String.format("Copyright © %s Martin Ždila, Freemap Slovakia", Constants.YEAR); //NON-NLS

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String OS_VERSION = System.getProperty("os.version");

    public static final String OS_ARCH = System.getProperty("os.arch");

    public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    private static String loadVersionString() {
        final var stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("version.txt");
        assert stream != null : "Version file is missing!";
        try (Scanner scanner = new Scanner(stream, StandardCharsets.UTF_8)) {
            return scanner.nextLine();
        } catch (final Exception e) {
            return "<UNKNOWN VERSION>";
        }
    }
}
