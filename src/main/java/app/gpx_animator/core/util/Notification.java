package app.gpx_animator.core.util;

import app.gpx_animator.Constants;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.ImageIcon;
import java.awt.SystemTray;
import java.awt.TrayIcon;

public enum Notification {

    ERROR(TrayIcon.MessageType.ERROR),
    WARNING(TrayIcon.MessageType.WARNING),
    INFO(TrayIcon.MessageType.INFO),
    NONE(TrayIcon.MessageType.NONE);

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Notification.class);

    private static TrayIcon trayIcon;
    private static boolean isSupported;

    private final TrayIcon.MessageType type;

    Notification(@NonNull final TrayIcon.MessageType type) {
        this.type = type;
    }

    public void show(@NonNull final String title, @NonNull final String message) {
        trayIcon.displayMessage(title, message, type);
    }

    public static void init() {
        try {
            final var systemTray = SystemTray.getSystemTray();
            final var image = new ImageIcon(Notification.class.getResource("/icon_128.png")).getImage(); //NON-NLS
            trayIcon = new TrayIcon(image, Constants.APPNAME_VERSION);
            trayIcon.setImageAutoSize(true);
            systemTray.add(trayIcon);
            isSupported = true;
        } catch (final Exception e) {
            isSupported = false;
            LOGGER.warn("Notifications not supported on this platform!", e);
        }
    }

    public static boolean isSupported() {
        return isSupported;
    }

}
