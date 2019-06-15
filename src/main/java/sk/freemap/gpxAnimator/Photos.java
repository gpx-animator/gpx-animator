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
package sk.freemap.gpxAnimator;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.lang.annotations.NotNull;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import org.imgscalr.Scalr;
import sk.freemap.gpxAnimator.frameWriter.FrameWriter;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.*;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

public class Photos {

    private final Map<Long, List<Photo>> photos;

    public Photos(@NotNull final File directory) {
        if (directory.isDirectory()) {
            final File[] files = directory.listFiles((dir, name) -> {
                final String lowerCaseName = name.toLowerCase();
                return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png");
            });
            if (files != null) {
                photos = Arrays.stream(files).map(Photos::toPhoto).filter(photo -> photo.getEpochSeconds() > 0)
                        .collect(groupingBy(Photo::getEpochSeconds));
            } else {
                photos = new HashMap<>();
            }
        } else {
            if (!directory.getAbsolutePath().isEmpty()) {
                System.err.println(String.format("'%s' is not a directory!", directory));
            }
            photos = new HashMap<>();
        }
    }

    private static Photo toPhoto(final File file) {
        return new Photo(timeOfPhotoInMilliSeconds(file), file);
    }

    private static Long timeOfPhotoInMilliSeconds(final File file) {
        try {
            final Metadata metadata = ImageMetadataReader.readMetadata(file);
            final ExifSubIFDDirectory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            final String dateTimeString = directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                    + " " + directory.getString(36881).replace(":", "");
            final ZonedDateTime zonedDateTime = ZonedDateTime.parse(dateTimeString,
                    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss x"));
            return zonedDateTime.toEpochSecond() * 1_000;
        } catch (ImageProcessingException | IOException | NullPointerException e) {
            System.err.println(String.format("Error processing file '%s': %s",
                    file.getAbsolutePath(), e.getMessage()));
            return 0L;
        }
    }

    public void render(final Long gpsTime, final Configuration cfg, final BufferedImage bi,
                       final FrameWriter frameWriter, final RenderingContext rc, final int pct) {
        System.out.println("GPS time: " + gpsTime);
        final List<Long> keys = photos.keySet().stream()
                .filter(photoTime -> gpsTime >= photoTime)
                .collect(Collectors.toList());
        if (!keys.isEmpty()) {
            keys.stream()
                    .map(photos::get)
                    .flatMap(List::stream).collect(Collectors.toList())
                    .forEach(photo -> Photos.renderPhoto(photo, cfg, bi, frameWriter, rc, pct));
            keys.forEach(photos::remove);
        }
    }

    private static void renderPhoto(final Photo photo, final Configuration cfg,
                                    final BufferedImage bi, final FrameWriter frameWriter,
                                    final RenderingContext rc, final int pct) {
        rc.setProgress1(pct, String.format("Rendering photo '%s'", photo.getFile().getName()));

        final BufferedImage image = readPhoto(photo, bi.getWidth(), bi.getHeight());
        if (image != null) {
            final BufferedImage bi2 = Utils.deepCopy(bi);
            final Graphics2D g2d = bi2.createGraphics();
            final int posX = (bi.getWidth() - image.getWidth()) / 2;
            final int posY = (bi.getHeight() - image.getHeight()) / 2;
            g2d.drawImage(image, posX, posY, null);
            g2d.dispose();

            final long ms = cfg.getPhotoTime().longValue();
            final long fps = Double.valueOf(cfg.getFps()).longValue();
            final long frames = ms * fps / 1_000;

            try {
                for (long frame = 0; frame < frames; frame++) {
                    frameWriter.addFrame(bi2);
                }
            } catch (final UserException e) {
                System.err.println(String.format("Problem rendering photo '%s': %s", photo, e.getMessage() ));
            }
        }
    }

    private static BufferedImage readPhoto(final Photo photo, final int width, final int height) {
        try {
            final byte[] rawData = getRawBytesFromFile(photo.getFile());
            final ImageInputStream input = ImageIO.createImageInputStream(new ByteArrayInputStream(rawData));
            final BufferedImage image = ImageIO.read(input);
            final int scaledWidth = Math.round(width * 0.7f);
            final int scaledHeight = Math.round(height * 0.7f);
            final BufferedImage scaledImage = scaleImage(image, scaledWidth, scaledHeight);
            return addBorder(scaledImage);
        } catch (final IOException e) {
            System.err.println(String.format("Problem reading photo '%s': %s", photo, e.getMessage() ));
        }
        return null;
    }

    private static BufferedImage addBorder(final BufferedImage image) {
        int borderWidth = Math.round(image.getWidth() / 15);
        int borderHeight = Math.round(image.getHeight() / 15);
        int borderSize = borderWidth < borderHeight ? borderWidth : borderHeight;
        int outerBorderSize = Math.round(borderSize / 5);
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

    private static byte[] getRawBytesFromFile(final File file) throws IOException {
        final byte[] image = new byte[(int)file.length()];
        try (final FileInputStream fileInputStream = new FileInputStream(file)) {
            //noinspection ResultOfMethodCallIgnored
            fileInputStream.read(image);
        }
        return image;
    }
}
