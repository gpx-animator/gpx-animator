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
import edu.umd.cs.findbugs.annotations.Nullable;
import org.jetbrains.annotations.NonNls;
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

import static app.gpx_animator.core.util.Utils.isEqual;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class GpxContentHandler extends DefaultHandler {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(GpxContentHandler.class);

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final List<TrackSegment> trackSegments = new ArrayList<>();
    private final List<TrackPoint> trackPoints = new ArrayList<>();
    private final List<WayPoint> wayPoints = new ArrayList<>();

    private final ArrayDeque<StringBuilder> characterStack = new ArrayDeque<>();

    private Track track = null;
    private TrackType trackType = null;
    private Long time = null;
    private Double speed = null;
    private Double latitude = null;
    private Double longitude = null;
    private String name = null;
    private String comment = null;

    public GpxContentHandler() {
        characterStack.addLast(new StringBuilder());
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        characterStack.addLast(new StringBuilder());
        if (isEqual(GPX.TRACK_POINT.getName(), qName) || isEqual(GPX.WAY_POINT.getName(), qName)) {
            latitude = Double.parseDouble(attributes.getValue(GPX.LATITUDE.getName()));
            longitude = Double.parseDouble(attributes.getValue(GPX.LONGITUDE.getName()));
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
    public void endElement(final String uri, final String localName, final String qName) {
        final var sb = characterStack.removeLast();

        if (isEqual(GPX.TRACK.getName(), qName)) {
            track = new Track(name, comment, trackType, List.copyOf(trackSegments));
            name = null;
            comment = null;
            trackType = null;
            trackSegments.clear();
        } else if (isEqual(GPX.TYPE.getName(), qName)) {
            trackType = TrackType.getTrackType(sb.toString());
        } else if (isEqual(GPX.TRACK_SEGMENT.getName(), qName)) {
            trackSegments.add(new TrackSegment(List.copyOf(trackPoints)));
            trackPoints.clear();
        } else if (isEqual(GPX.TRACK_POINT.getName(), qName)) {
            trackPoints.add(new TrackPoint(latitude, longitude, time, speed, comment));
            latitude = null;
            longitude = null;
            time = null;
            speed = null;
            comment = null;
        } else if (isEqual(GPX.WAY_POINT.getName(), qName)) {
            wayPoints.add(new WayPoint(latitude, longitude, time, name, comment));
            latitude = null;
            longitude = null;
            time = null;
            name = null;
            comment = null;
        } else if (isEqual(GPX.TIME.getName(), qName)) {
            final var dateTime = parseDateTime(sb.toString());
            time = dateTime != null ? dateTime.toInstant().toEpochMilli() : 0;
        } else if (isEqual(GPX.SPEED.getName(), qName)) {
            if (!sb.isEmpty()) {
                speed = Double.parseDouble(sb.toString());
            }
        } else if (isEqual(GPX.NAME.getName(), qName)) {
            name = sb.toString();
        } else if (isEqual(GPX.COMMENT.getName(), qName)) {
            comment = sb.toString();
        }
    }

    private ZonedDateTime parseDateTime(@Nullable final String dateTimeString) {
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

    public Track getTrack() {
        return track;
    }

    public List<WayPoint> getWayPoints() {
        return Collections.unmodifiableList(wayPoints);
    }

}
