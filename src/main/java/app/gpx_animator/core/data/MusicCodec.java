/*
Copyright 2023 Contributors to the Sports-club

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package app.gpx_animator.core.data;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComboBox;
import java.util.Arrays;

import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_AAC;
import static org.bytedeco.ffmpeg.global.avcodec.AV_CODEC_ID_PCM_S16LE;

/**
 * Description: List audio codec.
 *
 * @author <a href="mailto:onixbed@gmail.com">amaksimov</a>
 * crested on 24.12.2024
 */
@Getter
@RequiredArgsConstructor
public enum MusicCodec {
    AAC("AAC", AV_CODEC_ID_AAC),
    PCM_S16LE("PCM_S16LE", AV_CODEC_ID_PCM_S16LE);

    private final String codecName;
    private final int codecId;

    @Override
    public String toString() {
        return getCodecName();
    }

    public static void fillComboBox(@NotNull final JComboBox<MusicCodec> comboBox) {
        Arrays.stream(values()).forEach(comboBox::addItem);
    }

    public static MusicCodec parse(final String codecName, final MusicCodec defaultCodec) {
        return Arrays.stream(MusicCodec.values())
                .filter(codec -> codec.getCodecName().equalsIgnoreCase(codecName))
                .findAny()
                .orElse(defaultCodec);
    }
}
