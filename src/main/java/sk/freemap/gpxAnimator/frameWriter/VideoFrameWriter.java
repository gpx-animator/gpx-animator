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

import com.xuggle.mediatool.IMediaWriter;
import com.xuggle.mediatool.ToolFactory;
import com.xuggle.xuggler.IRational;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.concurrent.TimeUnit;

public final class VideoFrameWriter implements FrameWriter {
    private final IMediaWriter writer;
    private final double interval;
    private int frame;

    public VideoFrameWriter(final File file, final double fps, final int width, final int height) {
        writer = ToolFactory.makeWriter(file.toString());
        writer.addVideoStream(0, 0, IRational.make(fps), width, height);
        interval = 1000d / fps;
    }

    @Override
    public void addFrame(final BufferedImage bi) {
        writer.encodeVideo(0, bi, (int) (frame++ * interval), TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        writer.close();
    }
}
