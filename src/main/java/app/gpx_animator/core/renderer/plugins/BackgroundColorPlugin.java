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

import app.gpx_animator.core.configuration.Configuration;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.awt.Color;
import java.awt.image.BufferedImage;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

// Plugins are loaded using reflection
public final class BackgroundColorPlugin implements RendererPlugin {

    private final Color backgroundColor;

    public BackgroundColorPlugin(@NonNull final Configuration configuration) {
        this.backgroundColor = configuration.getBackgroundColor();
    }

    @Override
    public int getOrder() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void renderBackground(@NonNull final BufferedImage image) {
        final var graphics = getGraphics(image);
        graphics.setColor(backgroundColor);
        graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    }

}
