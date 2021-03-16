package app.gpx_animator.core.renderer.plugins;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.configuration.Configuration;
import app.gpx_animator.core.data.Photo;
import app.gpx_animator.core.preferences.Preferences;
import app.gpx_animator.core.renderer.Metadata;
import app.gpx_animator.core.renderer.RenderingContext;
import app.gpx_animator.core.renderer.framewriter.FrameWriter;
import app.gpx_animator.core.util.RenderUtil;
import app.gpx_animator.core.util.Utils;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.exif.ExifSubIFDDirectory;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.geom.Point2D;
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

public final class PhotoPlugin implements RendererPlugin {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(PhotoPlugin.class);
    private static final String PHOTOS_PROGRESS_RENDERING = "photos.progress.rendering";
    private static final String SYSTEM_ZONE_OFFSET;

    static {
        final var dateTime = ZonedDateTime.now();
        final var formatter = DateTimeFormatter.ofPattern("x"); //NON-NLS
        SYSTEM_ZONE_OFFSET = dateTime.format(formatter);
    }

    private final transient ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final transient double fps;
    private final transient long photoTime;
    private final transient long photoAnimationDuration;

    private final transient Map<Long, List<Photo>> remainingPhotos;

    private transient Metadata metadata;
    private transient FrameWriter frameWriter;
    private transient RenderingContext context;

    public PhotoPlugin(@NonNull final Configuration configuration) {
        this.fps = configuration.getFps();
        this.photoTime = configuration.getPhotoTime() == null ? 0 : configuration.getPhotoTime();
        this.photoAnimationDuration = configuration.getPhotoAnimationDuration() == null ? 0 : configuration.getPhotoAnimationDuration();
        this.remainingPhotos = loadPhotos(configuration.getPhotoDirectory());
    }

    @Override
    public void setMetadata(@NonNull final Metadata metadata) {
        this.metadata = metadata;
    }

    @Override
    public void setFrameWriter(@NotNull final FrameWriter frameWriter) {
        this.frameWriter = frameWriter;
    }

    @Override
    public void setRenderingContext(@NotNull final RenderingContext renderingContext) {
        this.context = renderingContext;
    }

    private Map<Long, List<Photo>> loadPhotos(@Nullable final String dirname) {
        if (dirname == null || dirname.isBlank()) {
            return new HashMap<>();
        } else {
            final var directory = new File(dirname);
            if (directory.isDirectory()) {
                final var files = directory.listFiles((dir, name) -> {
                    final var lowerCaseName = name.toLowerCase(Locale.getDefault());
                    return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") || lowerCaseName.endsWith(".png"); //NON-NLS
                });
                if (files != null) {
                    return Arrays.stream(files)
                            .map(this::toPhoto)
                            .filter(this::validatePhotoTime)
                            .collect(groupingBy(Photo::getEpochSeconds));
                } else {
                    return new HashMap<>();
                }
            } else {
                LOGGER.error("'{}' is not a directory!", directory);
                return new HashMap<>();
            }
        }
    }

    private Photo toPhoto(@NonNull final File file) {
        return new Photo(timeOfPhotoInMilliSeconds(file), file);
    }

    private Long timeOfPhotoInMilliSeconds(@NonNull final File file) {
        try {
            final var imageMetadata = ImageMetadataReader.readMetadata(file);
            final var directory = imageMetadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
            final var zoneOffset = directory.getString(36881) != null ? directory.getString(36881) : SYSTEM_ZONE_OFFSET;
            final var dateTimeString = directory.getString(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL)
                    .concat(" ").concat(zoneOffset.replace(":", ""));
            final var zonedDateTime = ZonedDateTime.parse(dateTimeString,
                    DateTimeFormatter.ofPattern("yyyy:MM:dd HH:mm:ss x")); //NON-NLS
            return zonedDateTime.toEpochSecond() * 1_000;
        } catch (ImageProcessingException | IOException | NullPointerException e) { // NOPMD -- NPEs can happen quite often in image metadata handling
            LOGGER.error("Error processing file '{}'!", file.getAbsoluteFile(), e);
            return 0L;
        }
    }

    private boolean validatePhotoTime(@NonNull final Photo photo) {
        if (photo.getEpochSeconds() > 0) {
            return true;
        }
        LOGGER.warn("Ignoring photo '{}', because it has no timestamp.", photo);
        return false;
    }

    /**
     * The photos will always be rendered on top of all other layers.
     *
     * @return highest layer number
     */
    @Override
    public int getOrder() {
        return 1_000_000;
    }

    @Override
    public int getAdditionalFrameCount() {
        final var numberOfPhotos = remainingPhotos.size();
        final var displayPhotoTime = photoTime * numberOfPhotos;
        final var photoAnimationTime = photoAnimationDuration * numberOfPhotos * 2;
        final var milliseconds = displayPhotoTime + photoAnimationTime;
        return (int) Math.round(fps * milliseconds / 1_000);
    }

    @Override
    public void renderFrame(final int frame, @Nullable final Point2D marker, @NonNull final BufferedImage bi) {
        final var time = RenderUtil.getTime(frame, metadata.minTime(), fps, metadata.speedup());
        final var keys = remainingPhotos.keySet().stream()
                .filter(timeOfPhoto -> time >= timeOfPhoto)
                .collect(Collectors.toList());
        if (!keys.isEmpty()) {
            keys.stream()
                    .map(remainingPhotos::get)
                    .flatMap(List::stream).collect(Collectors.toList())
                    .forEach(photo -> renderPhoto(photo, bi));
            keys.forEach(remainingPhotos::remove);
        }
    }

    private void renderPhoto(@NonNull final Photo photo, @NonNull final BufferedImage frameImage) {
        final var filename = photo.getFile().getName();
        context.setProgress1(0, String.format(resourceBundle.getString(PHOTOS_PROGRESS_RENDERING), filename));

        final var photoImage = readPhoto(photo, frameImage.getWidth() - 20, frameImage.getHeight() - 20);
        if (photoImage != null) {
            final var bi2 = Utils.deepCopy(frameImage);
            final var g2d = bi2.createGraphics();
            final var posX = (frameImage.getWidth() - photoImage.getWidth()) / 2;
            final var posY = (frameImage.getHeight() - photoImage.getHeight()) / 2;
            g2d.drawImage(photoImage, posX, posY, null);
            g2d.dispose();

            final var frames = (int) Math.round(photoTime * fps / 1_000);
            final var inOutFrames = (int) Math.round(photoAnimationDuration * fps / 1_000);
            final var allFrames = frames + (2 * inOutFrames);

            try {
                renderAnimationIn(frameImage, photoImage, inOutFrames, allFrames, filename);
                for (long frame = 0; frame < frames; frame++) {
                    final var pct = (int) (100.0 * (inOutFrames + frame) / allFrames);
                    context.setProgress1(pct, String.format(resourceBundle.getString(PHOTOS_PROGRESS_RENDERING), filename));
                    frameWriter.addFrame(bi2);
                }
                renderAnimationOut(frameImage, photoImage, inOutFrames, allFrames, filename);
            } catch (final UserException e) {
                LOGGER.error("Problems rendering photo '{}'!", photo, e);
            }
        }
    }

    private BufferedImage readPhoto(@NonNull final Photo photo, final int width, final int height) {
        try {
            final var image = ImageIO.read(photo.getFile());
            final var scaledWidth = Math.round(width * 0.8f);
            final var scaledHeight = Math.round(height * 0.8f);
            final var scaledImage = RenderUtil.scaleImage(image, scaledWidth, scaledHeight);
            final var borderedImage = addBorder(scaledImage);
            borderedImage.flush();
            return borderedImage;
        } catch (final IOException e) {
            LOGGER.error("Problems reading photo '{}'!", photo, e);
        }

        return null;
    }

    private void renderAnimationIn(@NonNull final BufferedImage frameImage, @NonNull final BufferedImage photoImage, final int frames,
                                   final int allFrames, @NonNull final String filename) throws UserException {
        for (long frame = 1; frame <= frames; frame++) {
            final var pct = (int) (100.0 * frame / allFrames);
            context.setProgress1(pct, String.format(resourceBundle.getString(PHOTOS_PROGRESS_RENDERING), filename));
            renderAnimation(frameImage, photoImage, frames, frame);
        }
    }

    private void renderAnimationOut(@NonNull final BufferedImage frameImage, @NonNull final BufferedImage photoImage, final long frames,
                                    final int allFrames, @NonNull final String filename) throws UserException {
        for (var frame = frames; frame >= 1; frame--) {
            final var pct = (int) (100.0 * (allFrames - frame) / allFrames);
            context.setProgress1(pct, String.format(resourceBundle.getString(PHOTOS_PROGRESS_RENDERING), filename));
            renderAnimation(frameImage, photoImage, frames, frame);
        }
        context.setProgress1(100, String.format(resourceBundle.getString(PHOTOS_PROGRESS_RENDERING), filename));
    }

    private void renderAnimation(@NonNull final BufferedImage frameImage, @NonNull final BufferedImage photoImage, final long frames,
                                 final long frame) throws UserException {
        final var width = (int) (photoImage.getWidth() * frame / frames);
        final var height = (int) (photoImage.getHeight() * frame / frames);
        final var scaledImage = RenderUtil.scaleImage(photoImage, width, height);

        final var posX = (frameImage.getWidth() - scaledImage.getWidth()) / 2;
        final var posY = (frameImage.getHeight() - scaledImage.getHeight()) / 2;

        final var bi2 = Utils.deepCopy(frameImage);
        final var g2d = bi2.createGraphics();

        g2d.drawImage(scaledImage, posX, posY, null);
        g2d.dispose();

        frameWriter.addFrame(bi2);
    }

    private static BufferedImage addBorder(@NonNull final BufferedImage photoImage) {
        var borderWidth = photoImage.getWidth() / 15;
        var borderHeight = photoImage.getHeight() / 15;
        var borderSize = Math.min(borderWidth, borderHeight);
        var outerBorderSize = borderSize / 5;
        final var border = new BufferedImage(
                photoImage.getWidth() + 2 * borderSize,
                photoImage.getHeight() + 2 * borderSize,
                photoImage.getType());

        final var g2d = border.createGraphics();
        g2d.setColor(Color.DARK_GRAY);
        g2d.fillRect(0, 0, border.getWidth(), border.getHeight());
        g2d.setColor(Color.WHITE);
        g2d.fillRect(outerBorderSize, outerBorderSize,
                border.getWidth() - (2 * outerBorderSize), border.getHeight() - (2 * outerBorderSize));
        g2d.drawImage(photoImage, borderSize, borderSize, null);
        g2d.dispose();

        return border;
    }

}
