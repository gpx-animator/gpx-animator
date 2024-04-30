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
package app.gpx_animator.core.renderer.framewriter;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.data.VideoCodec;
import app.gpx_animator.core.preferences.Preferences;
import edu.umd.cs.findbugs.annotations.NonNull;
import org.bytedeco.javacv.FFmpegFrameRecorder;
import org.bytedeco.javacv.FrameRecorder;
import org.bytedeco.javacv.Java2DFrameConverter;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ResourceBundle;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_NONE;
import static org.bytedeco.ffmpeg.global.avutil.AV_PIX_FMT_YUV420P;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class VideoFrameWriter implements FrameWriter {

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private final Java2DFrameConverter frameConverter;
    private final FrameRecorder recorder;

    public VideoFrameWriter(@NonNull final File file, @NonNull final VideoCodec videoCodec,
                            final double fps, final int width, final int height) throws UserException {
        frameConverter = new Java2DFrameConverter();

        try {
            recorder = FFmpegFrameRecorder.createDefault(file, width, height);
        } catch (final FrameRecorder.Exception e) {
            throw new UserException(resourceBundle.getString("framewriter.error.createrecorder").formatted(e.getMessage()), e);
        }

        recorder.setFormat("matroska"); // mp4 doesn't support streaming
        recorder.setAudioCodec(AV_CODEC_ID_NONE);
        recorder.setVideoCodec(videoCodec.getCodecId());
        recorder.setPixelFormat(AV_PIX_FMT_YUV420P);
        recorder.setFormat("mp4");
        recorder.setVideoOption("crf", "24"); // recorder.setVideoQuality(24); -> crashes on systems with comma as decimal separator
        recorder.setFrameRate(fps);

        try {
            recorder.start();
        } catch (final FrameRecorder.Exception e) {
            throw new UserException(resourceBundle.getString("framewriter.error.startrecorder").formatted(e.getMessage()), e);
        }
    }

    @Override
    @SuppressWarnings("PMD.CloseResource") // frame will be closed later automatically
    public void addFrame(@NonNull final BufferedImage image) {
        final var frame = frameConverter.convert(image);
        try {
            recorder.record(frame);
        } catch (final FrameRecorder.Exception e) {
            throw new RuntimeException(new UserException(resourceBundle.getString("framewriter.error.record").formatted(e.getMessage()), e));
        }
    }

    @Override
    public void close() {
        try {
            recorder.close();
        } catch (final FrameRecorder.Exception e) {
            throw new RuntimeException(new UserException(resourceBundle.getString("framewriter.error.closerecorder").formatted(e.getMessage()), e));
        }
    }
}
