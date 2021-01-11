package app.gpx_animator.core.util;

import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sound.sampled.AudioSystem;
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
            try (var audioInputStream = AudioSystem.getAudioInputStream(url)) {
                final var clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
            } catch (final Exception e) {
                LOGGER.error("Unable to play sound: %s".formatted(filename), e);
            }
        }).start();
    }

}
