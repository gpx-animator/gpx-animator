/*
 *  Copyright Contributors to the GPX Animator project.
 *
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
package app.gpx_animator.core.util;

import app.gpx_animator.core.Constants;
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
