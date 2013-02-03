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

import java.text.ParseException;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class GpxContentHandler extends DefaultHandler {
	
	private final TreeMap<Long, LatLon> timePointMap = new TreeMap<Long, LatLon>();

	private final StringBuilder timeSb = new StringBuilder();
	private long time;
	private LatLon latLon;
	

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if ("trkpt".equals(qName)) {
			latLon = new LatLon(
					Double.parseDouble(attributes.getValue("lat")),
					Double.parseDouble(attributes.getValue("lon")));
		} else if ("time".equals(qName)) {
			timeSb.setLength(0);
		}
	}
	

	@Override
	public void characters(final char[] ch, final int start, final int length) throws SAXException {
		if (timeSb != null) {
			timeSb.append(ch, start, length);
		}
	}
	

	@Override
	public void endElement(final String uri, final String localName, final String qName) throws SAXException {
		if ("trkpt".equals(qName)) {
			timePointMap.put(time, latLon);
		} else if ("time".equals(qName)) {
			try {
				time = Utils.parseISO8601(timeSb.toString()).getTime();
			} catch (final ParseException e) {
				throw new SAXException(e);
			}
			timeSb.setLength(0);
		}
	}
	
	
	public TreeMap<Long, LatLon> getTimePointMap() {
		return timePointMap;
	}
	
}