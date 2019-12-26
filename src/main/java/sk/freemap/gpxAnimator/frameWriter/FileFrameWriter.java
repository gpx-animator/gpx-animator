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
package sk.freemap.gpxAnimator.frameWriter;

import sk.freemap.gpxAnimator.UserException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class FileFrameWriter implements FrameWriter {

    private final String frameFilePattern;
    private final String imageType;
    private final double fps;
    private int frame;

    public FileFrameWriter(final String frameFilePattern, final String imageType, final double fps) throws UserException {
        if (String.format(frameFilePattern, 100).equals(String.format(frameFilePattern, 200))) {
            throw new UserException("output must be pattern, for example frame%08d.png");
        }

        this.frameFilePattern = frameFilePattern;
        this.imageType = imageType;
        this.fps = fps;
    }

    @Override
    public void addFrame(final BufferedImage bi) throws UserException {
        final File outputfile = new File(String.format(frameFilePattern, ++frame));
        try {
            ImageIO.write(bi, imageType, outputfile);
        } catch (final IOException e) {
            throw new UserException("error writing frame to " + outputfile, e);
        }
    }

    @Override
    public void close() {
        System.out.println("To encode generated frames you may run this command:");
        System.out.println("ffmpeg -i " + frameFilePattern + " -vcodec mpeg4 -b 3000k -r " + fps + " video.avi");
    }
}
