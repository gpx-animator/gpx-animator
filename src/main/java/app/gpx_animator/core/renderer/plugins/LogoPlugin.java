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
package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.renderer.ImageRenderer;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.IOException;

// Plugins are loaded using reflection
public final class LogoPlugin extends ImageRenderer implements RendererPlugin {

    private final BufferedImage logo;
    private final Position position;
    private final int margin;

    public LogoPlugin(@NonNull final Configuration configuration) throws UserException {
        position = configuration.getLogoPosition();
        margin = configuration.getLogoMargin();

        final var file = configuration.getLogo();
        if (file != null && file.exists()) {
            try {
                logo = ImageIO.read(file);
            } catch (final IOException e) {
                throw new UserException("Can't read logo: ".concat(e.getMessage())); // TODO translate
            }
        } else {
            logo = null;
        }
    }

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage image) {
        if (logo == null || position.equals(Position.HIDDEN)) {
            // no logo defined or logo should not be visible
            return;
        }

        renderImage(logo, position, margin, image);
    }

}
