/*
 *  Copyright 2013 Martin Ždila, Freemap Slovakia
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

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class GpxContentHandler extends DefaultHandler {
	
	private static final String ATTR_LON = "lon";
	private static final String ATTR_LAT = "lat";
	private static final String ELEM_TRKSEG = "trkseg";
	private static final String ELEM_TRKPT = "trkpt";
	private static final String ELEM_WPT = "wpt";
	private static final String ELEM_TIME = "time";
	private static final String ELEM_NAME = "name";
	
	private final List<List<LatLon>> timePointListList = new ArrayList<List<LatLon>>();
	private List<LatLon> timePointList;
	private final List<LatLon> waypointList = new ArrayList<LatLon>();

	private StringBuilder sb;
	private long time = Long.MIN_VALUE;
	private double lat;
	private double lon;
	private String name;
	

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if (ELEM_TRKSEG.equals(qName)) {
			timePointList = new ArrayList<LatLon>();
		} else if (ELEM_TRKPT.equals(qName) || ELEM_WPT.equals(qName)) {
			lat = Double.parseDouble(attributes.getValue(ATTR_LAT));
			lon = Double.parseDouble(attributes.getValue(ATTR_LON));
		} else if (ELEM_TIME.equals(qName) || ELEM_NAME.equals(qName) ) {
			sb = new StringBuilder();
		}
	}
	

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (sb != null) {
			sb.append(ch, start, length);
		}
	}
	

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if (ELEM_TRKSEG.equals(qName)) {
			timePointListList.add(timePointList);
			timePointList = null;
		} else if (ELEM_TRKPT.equals(qName)) {
			timePointList.add(new LatLon(lat, lon, time));
			time = Long.MIN_VALUE;
		} else if (ELEM_WPT.equals(qName)) {
			waypointList.add(new Waypoint(lat, lon, time, name));
		} else if (ELEM_TIME.equals(qName)) {
			final ZonedDateTime dateTime = ZonedDateTime.parse(sb.toString());
			time = dateTime.toInstant().toEpochMilli();
			sb = null;
		} else if (ELEM_NAME.equals(qName)) {
			name = sb.toString();
			sb = null;
		}
	}
	
	
	public List<List<LatLon>> getPointLists() {
		return timePointListList;
	}
	
	
	public List<LatLon> getWaypointList() {
		return waypointList;
	}
	
}
