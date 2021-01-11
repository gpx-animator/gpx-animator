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
package app.gpx_animator;

import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.ui.cli.CommandLineConfigurationFactory;
import app.gpx_animator.ui.swing.MainFrame;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.EventQueue;
import java.awt.GraphicsEnvironment;


public final class Main {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);

    private Main() throws InstantiationException {
        throw new InstantiationException("This is the main class and can't be instantiated!");
    }

    public static void main(final String... args) {
        try {
            start(args);
        } catch (final UserException e) {
            LOGGER.error(e.getMessage());
            System.exit(1); // NOPMD -- We can't recover here
        } catch (final Exception e) {
            LOGGER.error("Very bad, exception caught in main method!", e);
            System.exit(2); // NOPMD -- We can't recover here
        }
    }

    public static void start(final String... args) throws Exception {
        final var cf = new CommandLineConfigurationFactory(args);
        final var configuration = cf.getConfiguration().validate();

        new Thread(TileCache::ageCache).start();

        if (cf.isGui() && !GraphicsEnvironment.isHeadless()) {
            EventQueue.invokeLater(() -> {
                try {
                    final var frame = new MainFrame();
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
    }

}
