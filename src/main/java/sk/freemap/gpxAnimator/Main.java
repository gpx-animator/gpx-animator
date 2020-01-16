/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
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
package sk.freemap.gpxAnimator;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sk.freemap.gpxAnimator.ui.MainFrame;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;
import java.util.ResourceBundle;


public final class Main {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() throws InstantiationException {
        throw new InstantiationException("This is the main class and can't be instantiated!");
    }

    public static void main(final String[] args) {
        final ResourceBundle resourceBundle = Preferences.getResourceBundle();

        try {
            final CommandLineConfigurationFactory cf = new CommandLineConfigurationFactory(args);
            final Configuration configuration = cf.getConfiguration();

            new Thread(TileCache::ageCache).start();

            if (cf.isGui() && !GraphicsEnvironment.isHeadless()) {
                EventQueue.invokeLater(() -> {
                    try {
                        final MainFrame frame = new MainFrame();
                        frame.setVisible(true);
                        frame.setConfiguration(configuration);
                    } catch (final Exception e) {
                        e.printStackTrace();
                    }
                });
            } else {
                new Renderer(configuration).render(new RenderingContext() {
                    @Override
                    public void setProgress1(final int pct, final String message) {
                        LOGGER.info("{}% {}", pct, message);
                    }

                    @Override
                    public boolean isCancelled1() {
                        return false;
                    }
                });
            }
        } catch (final UserException e) {
            LOGGER.error("Very bad, exception caught in main method!", e);
            System.exit(1); // NOPMD -- We can't recover here
        }
    }

}
