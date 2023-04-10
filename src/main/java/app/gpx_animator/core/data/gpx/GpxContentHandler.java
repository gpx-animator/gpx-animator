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
package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.data.entity.Track;
import app.gpx_animator.core.data.entity.TrackPoint;
import app.gpx_animator.core.data.entity.TrackSegment;
import app.gpx_animator.core.data.entity.TrackType;
import app.gpx_animator.core.data.entity.WayPoint;
import app.gpx_animator.core.preferences.Preferences;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ResourceBundle;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class GpxContentHandler extends DefaultHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GpxContentHandler.class);

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();
    private final List<TrackPoint> trackPoints = new ArrayList<>();
    private final List<WayPoint> wayPoints = new ArrayList<>();
    private final ArrayDeque<StringBuilder> characterStack = new ArrayDeque<>();

    private Track track = null;
    private TrackPoint trackPoint = null;
    private WayPoint wayPoint = null;

    public GpxContentHandler() {
        characterStack.addLast(new StringBuilder());
    }

    @Override
    public void startElement(@Nullable final String uri, @Nullable final String localName, @NotNull final String qName,
                             @NotNull final Attributes attributes) {
        characterStack.addLast(new StringBuilder());
        try {
            final GPX gpxElement = GPX.getElement(qName);
            switch (gpxElement) {
                case TRACK -> track = new Track();
                case TRACK_POINT -> {
                    trackPoint = new TrackPoint(
                            Double.parseDouble(attributes.getValue(GPX.LATITUDE.getName())),
                            Double.parseDouble(attributes.getValue(GPX.LONGITUDE.getName())));
                }
                case WAY_POINT -> {
                    wayPoint = new WayPoint(
                            Double.parseDouble(attributes.getValue(GPX.LATITUDE.getName())),
                            Double.parseDouble(attributes.getValue(GPX.LONGITUDE.getName())));
                }
                default -> LOGGER.debug("Ignoring supported XML start element \"{}\"", qName);
            }
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("Ignoring unsupported XML start element \"{}\"", qName);
        }
    }

    @Override
    public void characters(final char[] ch, final int start, final int length) {
        final var sb = characterStack.peekLast();
        if (sb != null) {
            sb.append(ch, start, length);
        }
    }

    @Override
    @SuppressWarnings("PMD.NullAssignment") // XML parsing ending elements, it's okay here
    public void endElement(@Nullable final String uri, @Nullable final String localName, @NotNull final String qName) {
        final var sb = characterStack.removeLast();
        try {
            final GPX gpxElement = GPX.getElement(qName);
            switch (gpxElement) {
                case TYPE -> track = track.withType(TrackType.getTrackType(sb.toString()));
                case TRACK_SEGMENT -> {
                    final var trackSegments = new ArrayList<>(track.getTrackSegments());
                    trackSegments.add(new TrackSegment(List.copyOf(trackPoints)));
                    track = track.withTrackSegments(trackSegments);
                    trackPoints.clear();
                }
                case TRACK_POINT -> {
                    trackPoints.add(trackPoint);
                    trackPoint = null;
                }
                case WAY_POINT -> {
                    wayPoints.add(wayPoint);
                    wayPoint = null;
                }
                case TIME -> {
                    final var dateTime = parseDateTime(sb.toString());
                    if (dateTime != null) {
                        final var time = dateTime.toInstant().toEpochMilli();
                        if (trackPoint != null) {
                            trackPoint = trackPoint.withTime(time);
                        } else if (wayPoint != null) {
                            wayPoint = wayPoint.withTime(time);
                        }
                    }
                }
                case COMMENT -> {
                    if (trackPoint != null) {
                        trackPoint = trackPoint.withComment(sb.toString());
                    } else if (wayPoint != null) {
                        wayPoint = wayPoint.withComment(sb.toString());
                    } else if (track != null) {
                        track = track.withComment(sb.toString());
                    }
                }
                case SPEED -> {
                    if (trackPoint != null && !sb.isEmpty()) {
                        final var speed = Double.parseDouble(sb.toString());
                        trackPoint = trackPoint.withSpeed(speed);
                    }
                }
                case NAME -> {
                    if (wayPoint != null) {
                        wayPoint = wayPoint.withName(sb.toString());
                    }
                }
                default -> LOGGER.debug("Ignoring supported XML end element \"{}\"", qName);
            }
        } catch (final IllegalArgumentException e) {
            LOGGER.debug("Ignoring unsupported XML end element \"{}\"", qName);
        }
    }

    private @Nullable ZonedDateTime parseDateTime(@Nullable final String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(dateTimeString);
        } catch (final DateTimeParseException ignored) { }

        try {
            return LocalDateTime.parse(dateTimeString).atZone(ZoneId.systemDefault());
        } catch (final DateTimeParseException ignored) { }

        LOGGER.error("Unable to parse date and time from string '{}'", dateTimeString);
        throw new RuntimeException(
                new UserException(resourceBundle.getString("gpxparser.error.datetimeformat").formatted(dateTimeString)));
    }

    public @Nullable Track getTrack() {
        return track;
    }

    public @NotNull List<WayPoint> getWayPoints() {
        return Collections.unmodifiableList(wayPoints);
    }

}
