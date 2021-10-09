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
package app.gpx_animator.core.data;

import edu.umd.cs.findbugs.annotations.NonNull;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComboBox;
import java.util.Arrays;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H264;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_H265;

public enum VideoCodec {

    H264("H.264", AV_CODEC_ID_H264),
    H265("H.265", AV_CODEC_ID_H265);

    private final String codecName;
    private final int codecId;

    VideoCodec(@NonNull final String codecName, final int codecId) {
        this.codecName = codecName;
        this.codecId = codecId;
    }

    public String getCodecName() {
        return codecName;
    }

    public int getCodecId() {
        return codecId;
    }

    @Override
    public String toString() {
        return getCodecName();
    }

    public static void fillComboBox(@NotNull final JComboBox<VideoCodec> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

    public static VideoCodec parse(final String codecName, final VideoCodec defaultCodec) {
        return Arrays.stream(VideoCodec.values())
                .filter(videoCodec -> videoCodec.getCodecName().equalsIgnoreCase(codecName))
                .findAny()
                .orElse(defaultCodec);
    }

}
