/*
 *  Copyright 2019 Marcus Fihlon, Switzerland
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
package app.gpx_animator;

import app.gpx_animator.frameWriter.FrameWriter;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.imgscalr.Scalr;
import org.jetbrains.annotations.NonNls;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class Photos {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(Photos.class);

    private static final String SYSTEM_ZONE_OFFSET;

    static {
        final ZonedDateTime dateTime = ZonedDateTime.now();
        final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("x"); //NON-NLS
        SYSTEM_ZONE_OFFSET = dateTime.format(formatter);
    }

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final Map<Long, List<Photo>> allPhotos;

    public Photos(final String dirname) {
        if (dirname == null || dirname.isBlank()) {
            allPhotos = new HashMap<>();
        } else {
            final File directory = new File(dirname);
            if (directory.isDirectory()) {
                final File[] files = directory.listFiles((dir, name) -> {
                    final String lowerCaseName = name.toLowerCase(Locale.getDefault());
                    return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png"); //NON-NLS
                });
                if (files != null) {
                    allPhotos = Arrays.stream(files)
                            .map(this::toPhoto)
                            .filter(this::validatePhotoTime)
                            .collect(groupingBy(Photo::getEpochSeconds));
                } else {
                    allPhotos = new HashMap<>();
                }
            } else {
                LOGGER.error("'{}' is not a directory!", directory);
                allPhotos = new HashMap<>();
            }
        }
    }

    private Photo toPhoto(final File file) {
        return new Photo(timeOfPhotoInMilliSeconds(file), file);
    }

    private boolean validatePhotoTime(final Photo photo) {
        if (photo.getEpochSeconds() > 0) {
            LOGGER.info("Photo '{}' has a timestamp, perfect.", photo);
            return true;
        }
        LOGGER.warn("Ignoring photo '{}', because it has no timestamp.", photo);
        return false;
    }

    private Long timeOfPhotoInMilliSeconds(final File file) {
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(file);
            final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            final String zoneOffset = directory.getString(36881) != null ? directory.getString(36881) : SYSTEM_ZONE_OFFSET;
            final String dateTimeString = directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                    .concat(" ").concat(zoneOffset.replace(":", ""));
            final ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString,
                    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss x")); //NON-NLS
            return zonedDateTime.toEpochSecond() * 1_000;
        } catch (ImageProcessingException | IOException | NullPointerException e) { // NOPMD -- NPEs can happen quite often in image metadata handling
            LOGGER.error("Error processing file '{}'!", file.getAbsoluteFile(), e);
            return 0L;
        }
    }

    private void renderPhoto(final Photo photo, final Configuration cfg,
                                    final BufferedImage bi, final FrameWriter frameWriter,
                                    final RenderingContext rc, final int pct) {
        rc.setProgress1(pct, String.format(resourceBundle.getString("photos.progress.rendering"), photo.getFile().getName()));

        final BufferedImage image = readPhoto(photo, bi.getWidth(), bi.getHeight());
        if (image != null) {
            final BufferedImage bi2 = Utils.deepCopy(bi);
            final Graphics2D g2d = bi2.createGraphics();
            final int posX = (bi.getWidth() - image.getWidth()) / 2;
            final int posY = (bi.getHeight() - image.getHeight()) / 2;
            g2d.drawImage(image, posX, posY, null);
            g2d.dispose();

            final long ms = cfg.getPhotoTime();
            final long fps = Double.valueOf(cfg.getFps()).longValue();
            final long frames = ms * fps / 1_000;
            final long inOutFrames = cfg.getPhotoAnimationDuration() * fps / 1_000;

            try {
                renderAnimationIn(bi, frameWriter, image, inOutFrames);
                for (long frame = 0; frame < frames; frame++) {
                    frameWriter.addFrame(bi2);
                }
                renderAnimationOut(bi, frameWriter, image, inOutFrames);
            } catch (final UserException e) {
                LOGGER.error("Problems rendering photo '{}'!", photo, e);
            }
        }
    }

    private void renderAnimationIn(final BufferedImage bi, final FrameWriter frameWriter, final BufferedImage image, final long frames)
            throws UserException {
        for (long frame = 1; frame <= frames; frame++) {
            renderAnimation(bi, frameWriter, image, frames, frame);
        }
    }

    private void renderAnimationOut(final BufferedImage bi, final FrameWriter frameWriter, final BufferedImage image, final long frames)
            throws UserException {
        for (long frame = frames; frame >= 1; frame--) {
            renderAnimation(bi, frameWriter, image, frames, frame);
        }
    }

    private void renderAnimation(final BufferedImage bi, final FrameWriter frameWriter,
                                 final BufferedImage image, final long frames, final long frame)
            throws UserException {
        final int width = (int) (image.getWidth() * frame / frames);
        final int height = (int) (image.getHeight() * frame / frames);
        final BufferedImage scaledImage = scaleImage(image, width, height);

        final int posX = (bi.getWidth() - scaledImage.getWidth()) / 2;
        final int posY = (bi.getHeight() - scaledImage.getHeight()) / 2;

        final BufferedImage bi2 = Utils.deepCopy(bi);
        final Graphics2D g2d = bi2.createGraphics();

        g2d.drawImage(scaledImage, posX, posY, null);
        g2d.dispose();

        frameWriter.addFrame(bi2);
    }

    private BufferedImage readPhoto(final Photo photo, final int width, final int height) {
        try {
            final BufferedImage image = ImageIO.read(photo.getFile());
            final int scaledWidth = Math.round(width * 0.7f);
            final int scaledHeight = Math.round(height * 0.7f);
            final BufferedImage scaledImage = scaleImage(image, scaledWidth, scaledHeight);
            final BufferedImage borderedImage = addBorder(scaledImage);
            borderedImage.flush();
            return borderedImage;
        } catch (final IOException e) {
            LOGGER.error("Problems reading photo '{}'!", photo, e);
        }
        return null;
    }

    private static BufferedImage addBorder(final BufferedImage image) {
        int borderWidth = image.getWidth() / 15;
        int borderHeight = image.getHeight() / 15;
        int borderSize = Math.min(borderWidth, borderHeight);
        int outerBorderSize = borderSize / 5;
        final BufferedImage border = new BufferedImage(
                image.getWidth() + 2 * borderSize,
                image.getHeight() + 2 * borderSize,
                image.getType());

        final Graphics2D g2d = border.createGraphics();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, border.getWidth(), border.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(outerBorderSize, outerBorderSize,
                border.getWidth() - (2 * outerBorderSize), border.getHeight() - (2 * outerBorderSize));
        g2d.drawImage(image, borderSize, borderSize, null);
        g2d.dispose();

        return border;
    }

    private static BufferedImage scaleImage(final BufferedImage image, final int width, final int height) {
        return Scalr.resize(Scalr.resize(image,
                Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_WIDTH, width),
                Scalr.Method.ULTRA_QUALITY, Scalr.Mode.FIT_TO_HEIGHT, height);
    }

    public void render(final Long gpsTime, final Configuration cfg, final BufferedImage bi,
                       final FrameWriter frameWriter, final RenderingContext rc, final int pct) {
        final List<Long> keys = allPhotos.keySet().stream()
                .filter(photoTime -> gpsTime >= photoTime)
                .collect(Collectors.toList());
        if (!keys.isEmpty()) {
            keys.stream()
                    .map(allPhotos::get)
                    .flatMap(List::stream).collect(Collectors.toList())
                    .forEach(photo -> renderPhoto(photo, cfg, bi, frameWriter, rc, pct));
            keys.forEach(allPhotos::remove);
        }
    }
}
