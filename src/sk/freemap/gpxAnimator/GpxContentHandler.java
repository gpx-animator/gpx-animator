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

import java.awt.geom.Point2D;
import java.text.ParseException;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

final class GpxContentHandler extends DefaultHandler {
	
	private final TreeMap<Long, Point2D> timePointMap = new TreeMap<Long, Point2D>();

	private double x;
	private double y;
	private StringBuilder timeSb;
	private long time;
	

	@Override
	public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException {
		if ("trkpt".equals(qName)) {
			x = Math.toRadians(Double.parseDouble(attributes.getValue("lon"))) * 6378137.0;
			y = Math.log(Math.tan(Math.PI / 4 + Math.toRadians(Double.parseDouble(attributes.getValue("lat"))) / 2)) * 6378137.0;
		} else if ("time".equals(qName)) {
			timeSb = new StringBuilder();
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
			// System.out.println(String.format("%f %f %d", x, y, time));
			timePointMap.put(time, new Point2D.Double(x, y));
		} else if ("time".equals(qName)) {
			try {
				time = Utils.parseISO8601(timeSb.toString()).getTime();
			} catch (final ParseException e) {
				throw new SAXException(e);
			}
			timeSb = null;
		}
	}
	
	
	public TreeMap<Long, Point2D> getTimePointMap() {
		return timePointMap;
	}
	
}