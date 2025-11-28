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
package app.gpx_animator.core.util;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.util.Locale;

public enum Sound {

    ERROR,
    SUCCESS;

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Sound.class);

    private static final String SOUND_RESOURCES = "/sounds/%s.wav";

    public void play() {
        new Thread(() -> {
            final var filename = String.format(SOUND_RESOURCES, name().toLowerCase(Locale.getDefault()));
            final var url = getClass().getResource(filename);
            if (url != null) {
                try (var audioInputStream = AudioSystem.getAudioInputStream(url)) {
                    final var clip = AudioSystem.getClip();
                    clip.open(audioInputStream);
                    clip.start();
                } catch (final IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                    LOGGER.error("Unable to play sound: {}", filename, e);
                }
            } else {
                LOGGER.error("Sound file not found: {}", filename);
            }
        }).start();
    }

}
