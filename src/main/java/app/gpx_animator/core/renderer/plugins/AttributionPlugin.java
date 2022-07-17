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

import app.gpx_animator.core.Constants;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Position;
import app.gpx_animator.core.renderer.TextRenderer;
import app.gpx_animator.core.util.MapUtil;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

@SuppressWarnings("unused") // Plugins are loaded using reflection
public final class AttributionPlugin extends TextRenderer implements RendererPlugin {

    private final String attribution;
    private final Position position;
    private final int margin;

    public AttributionPlugin(@NonNull final Configuration configuration) {
        super(configuration.getFont());

        final var mapTemplate = MapUtil.getMapTemplate(configuration.getTmsUrlTemplate());
        final var mapAttribution = mapTemplate == null || mapTemplate.attributionText() == null
                ? "" : mapTemplate.attributionText();

        attribution = configuration.getAttribution()
                .replace("%APPNAME_VERSION%", Constants.APPNAME_VERSION)
                .replace("%MAP_ATTRIBUTION%", mapAttribution);
        position = attribution.isBlank() ? Position.HIDDEN : configuration.getAttributionPosition();
        margin = configuration.getAttributionMargin();
    }

    @Override
    public int getOrder() {
        return 1_000;
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage image) {
        if (Position.HIDDEN.equals(position)) {
            // attribution should not be visible
            return;
        }

        renderText(attribution, position, margin, image);
    }

}
