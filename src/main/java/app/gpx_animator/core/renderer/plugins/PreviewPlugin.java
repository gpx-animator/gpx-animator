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
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.util.RenderUtil;
import app.gpx_animator.ui.UIMode;
import app.gpx_animator.ui.swing.PreviewDialog;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.Toolkit;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import static javax.management.timer.Timer.ONE_SECOND;

// Plugins are loaded using reflection
public final class PreviewPlugin implements RendererPlugin {

    private final Configuration configuration;

    private PreviewDialog preview;

    private int width;
    private int height;

    private boolean enabled = UIMode.getMode() != UIMode.CLI && Preferences.isPreviewEnabled();
    private long lastUpdate = 0;

    // all renderer plugins need this constructor
    public PreviewPlugin(@NonNull final Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public int getOrder() {
        return Integer.MAX_VALUE;
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NotNull final BufferedImage image) {
        if (!enabled && !configuration.isPreview()) {
            return;
        }

        final var now = System.currentTimeMillis();
        if (now - lastUpdate < ONE_SECOND) {
            return;
        }

        lastUpdate = now;

        if (width == 0) {
            calculateImageSize(image);
        }

        final var sameSize = image.getWidth() == width && image.getHeight() == height;
        final var previewImage = sameSize ? image : RenderUtil.scaleImage(image, width, height);

        if (preview == null) {
            preview = new PreviewDialog(this, previewImage);
        } else {
            preview.updatePreview(previewImage);
        }
    }

    @Override
    public void renderingFinished() {
        if (preview != null && !configuration.isPreview()) {
            preview.closeDialog();
        }
    }

    @Override
    public void renderingCanceled() {
        renderingFinished();
    }

    public void dialogClosed() {
        enabled = false;
    }

    private void calculateImageSize(@NotNull final BufferedImage image) {
        width = image.getWidth();
        height = image.getHeight();

        final var screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        if (width > screenSize.width - 50) {
            final var newWidth = screenSize.width - 50;
            height = newWidth * height / width;
            width = newWidth;
        }
        if (height > screenSize.height - 150) {
            final var newHeight = screenSize.height - 150;
            width = newHeight * width / height;
            height = newHeight;
        }
    }

}
