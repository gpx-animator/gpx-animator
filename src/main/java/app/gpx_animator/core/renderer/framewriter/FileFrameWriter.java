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
package app.gpx_animator.core.renderer.framewriter;

import app.gpx_animator.Preferences;
import app.gpx_animator.UserException;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import static app.gpx_animator.Utils.isEqual;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class FileFrameWriter implements FrameWriter {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(FileFrameWriter.class);

    private final String frameFilePattern;
    private final String imageType;
    private final double fps;
    private int frame;

    public FileFrameWriter(final String frameFilePattern, final String imageType, final double fps) throws UserException {
        if (isEqual(String.format(frameFilePattern, 100), String.format(frameFilePattern, 200))) {
            final var resourceBundle = Preferences.getResourceBundle();
            throw new UserException(resourceBundle.getString("framewriter.error.outputpattern"));
        }

        this.frameFilePattern = frameFilePattern;
        this.imageType = imageType;
        this.fps = fps;
    }

    @Override
    public void addFrame(final BufferedImage bi) throws UserException {
        final var outputfile = new File(String.format(frameFilePattern, ++frame));
        try {
            ImageIO.write(bi, imageType, outputfile);
        } catch (final IOException e) {
            throw new UserException(String.format("error writing frame to '%s'", outputfile), e);
        }
    }

    @Override
    public void close() {
        LOGGER.info("To encode generated frames you may run this command:");
        LOGGER.info("ffmpeg -i {} -vcodec mpeg4 -b 3000k -r {} video.avi", frameFilePattern, fps); //NON-NLS
    }
}
