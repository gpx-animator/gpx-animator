package app.gpx_animator;

import java.awt.Font;

@SuppressWarnings("checkstyle:NoWhitespaceBefore") // For the ";" after the class declaration which is needed to use an enum for constants only
public enum Constants {
    ;

    public static final String APPNAME = "GPX Animator"; //NON-NLS

    public static final String VERSION = "1.6.0-SNAPSHOT"; //NON-NLS

    public static final String APPNAME_VERSION = APPNAME.concat(" ").concat(VERSION);

    public static final String YEAR = "2020";

    public static final String COPYRIGHT = String.format("Copyright © %s Martin Ždila, Freemap Slovakia", Constants.YEAR); //NON-NLS

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String OS_VERSION = System.getProperty("os.version");

    public static final String OS_ARCH = System.getProperty("os.arch");

    public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
}
