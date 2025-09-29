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
package app.gpx_animator.core;

import org.slf4j.LoggerFactory;

import java.awt.Font;
import java.io.IOException;
import java.util.Properties;

@SuppressWarnings("checkstyle:NoWhitespaceBefore") // For the ";" after the class declaration which is needed to use an enum for constants only
public enum Constants {
    ;

    public static final String APPNAME = "GPX Animator"; //NON-NLS

    public static final String VERSION = loadVersionString();

    public static final String APPNAME_VERSION = APPNAME.concat(" ").concat(VERSION);

    public static final String COPYRIGHT = "Copyright Contributors to the GPX Animator project."; //NON-NLS

    public static final String OS_NAME = System.getProperty("os.name");

    public static final String OS_VERSION = System.getProperty("os.version");

    public static final String OS_ARCH = System.getProperty("os.arch");

    public static final Font DEFAULT_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);

    public static final String UPDATES_URL = "https://download.gpx-animator.app/updates.xml";

    public static final String USER_AGENT = String.format("%s %s on %s %s (%s)", //NON-NLS
            Constants.APPNAME, Constants.VERSION, Constants.OS_NAME, Constants.OS_VERSION, Constants.OS_ARCH);

    private static String loadVersionString() {

        final var properties = new Properties();
        try (var inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("build-info.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException e) {
            LoggerFactory.getLogger(Constants.class).warn("Could not load build-info.properties", e);
        }
        return properties.getProperty("version", "<UNKNOWN VERSION>");
    }
}
