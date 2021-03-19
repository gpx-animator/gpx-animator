/*
 *  Copyright Contributors to the GPX Animator project.
 *
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
package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.data.LatLon;
import app.gpx_animator.core.data.Waypoint;
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
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import static app.gpx_animator.core.util.Utils.isEqual;

@SuppressWarnings("PMD.BeanMembersShouldSerialize") // This class is not serializable
public final class GpxContentHandler extends DefaultHandler {

    @NonNls
    private static final Logger LOGGER = LoggerFactory.getLogger(GpxContentHandler.class);

    private static final String ATTR_LON = "lon"; //NON-NLS
    private static final String ATTR_LAT = "lat"; //NON-NLS
    private static final String ELEM_TRKSEG = "trkseg"; //NON-NLS
    private static final String ELEM_TRKPT = "trkpt"; //NON-NLS
    private static final String ELEM_WPT = "wpt"; //NON-NLS
    private static final String ELEM_TIME = "time"; //NON-NLS
    private static final String ELEM_NAME = "name"; //NON-NLS
    private static final String ELEM_CMT = "cmt"; //NON-NLS

    private final ResourceBundle resourceBundle = Preferences.getResourceBundle();

    private final List<List<LatLon>> timePointListList = new ArrayList<>();
    private final List<LatLon> waypointList = new ArrayList<>();
    private List<LatLon> timePointList;
    private StringBuilder sb;
    private long time = Long.MIN_VALUE;
    private double lat;
    private double lon;
    private String name;
    private String cmt;


    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
        if (isEqual(ELEM_TRKSEG, qName)) {
            timePointList = new ArrayList<>();
        } else if (isEqual(ELEM_TRKPT, qName) || isEqual(ELEM_WPT, qName)) {
            lat = Double.parseDouble(attributes.getValue(ATTR_LAT));
            lon = Double.parseDouble(attributes.getValue(ATTR_LON));
        } else if (isEqual(ELEM_TIME, qName) || isEqual(ELEM_NAME, qName) || isEqual(ELEM_CMT, qName)) {
            sb = new StringBuilder();
        }
    }


    @Override
    public void characters(final char[] ch, final int start, final int length) {
        if (sb != null) {
            sb.append(ch, start, length);
        }
    }


    @Override
    @SuppressWarnings("PMD.NullAssignment") // XML parsing ending elements, it's okay here
    public void endElement(final String uri, final String localName, final String qName) {
        if (isEqual(ELEM_TRKSEG, qName)) {
            timePointListList.add(timePointList);
            timePointList = null;
        } else if (isEqual(ELEM_TRKPT, qName)) {
            timePointList.add(new LatLon(lat, lon, time, cmt));
            time = Long.MIN_VALUE;
            cmt = null;
        } else if (isEqual(ELEM_WPT, qName)) {
            waypointList.add(new Waypoint(lat, lon, time, name));
        } else if (isEqual(ELEM_TIME, qName)) {
            final var dateTime = parseDateTime(sb.toString());
            time = dateTime.toInstant().toEpochMilli();
            sb = null;
        } else if (isEqual(ELEM_NAME, qName)) {
            name = sb.toString();
            sb = null;
        } else if (isEqual(ELEM_CMT, qName)) {
            cmt = sb.toString();
            sb = null;
        }
    }


    private ZonedDateTime parseDateTime(@Nullable final String dateTimeString) {
        if (dateTimeString == null || dateTimeString.isBlank()) {
            return null;
        }

        try {
            return ZonedDateTime.parse(sb.toString());
        } catch (final DateTimeParseException ignored) { }

        try {
            return LocalDateTime.parse(sb.toString()).atZone(ZoneId.systemDefault());
        } catch (final DateTimeParseException ignored) { }

        LOGGER.error("Unable to parse date and time from string '{}'", sb.toString());
        throw new RuntimeException(
                new UserException(resourceBundle.getString("gpxparser.error.datetimeformat").formatted(sb.toString())));
    }


    public List<List<LatLon>> getPointLists() {
        return timePointListList;
    }


    public List<LatLon> getWaypointList() {
        return waypointList;
    }

}
