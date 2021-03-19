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
package app.gpx_animator.core.renderer;

import app.gpx_animator.core.data.Position;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.awt.image.BufferedImage;

import static app.gpx_animator.core.util.RenderUtil.getGraphics;

public abstract class ImageRenderer {

    /**
     * Render image on the video frame.
     *
     * @param image       the image to render on the video frame
     * @param position    the position of the text on the video frame
     * @param margin      the margin from the text to the border of the video frame
     * @param targetImage the target image representing the video frame
     */
    public void renderImage(@NonNull final BufferedImage image, @NonNull final Position position, final int margin,
                            @NonNull final BufferedImage targetImage) {
        if (Position.HIDDEN.equals(position)) {
            return;
        }

        final var imageWidth = image.getWidth();
        final var imageHeight = image.getHeight();
        final var targetImageWidth = targetImage.getWidth();
        final var targetImageHeight = targetImage.getHeight();
        final var graphics = getGraphics(targetImage);

        int xPosition;
        int yPosition;
        switch (position) {
            case TOP_LEFT -> {
                xPosition = margin;
                yPosition = margin;
            }
            case TOP_CENTER -> {
                xPosition = (targetImageWidth - imageWidth) / 2;
                yPosition = margin;
            }
            case TOP_RIGHT -> {
                xPosition = targetImageWidth - imageWidth - margin;
                yPosition = margin;
            }
            case BOTTOM_LEFT -> {
                xPosition = margin;
                yPosition = targetImageHeight - imageHeight - margin;
            }
            case BOTTOM_CENTER -> {
                xPosition = (targetImageWidth - imageWidth) / 2;
                yPosition = targetImageHeight - imageHeight - margin;
            }
            case BOTTOM_RIGHT -> {
                xPosition = targetImageWidth - imageWidth - margin;
                yPosition = targetImageHeight - imageHeight - margin;
            }
            default -> throw new IllegalStateException("Unexpected position: " + position);
        }
        graphics.drawImage(image, xPosition, yPosition, imageWidth, imageHeight, null);
    }

}
