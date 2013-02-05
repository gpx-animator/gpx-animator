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

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.SAXException;

class GpxParser {

	static List<List<LatLon>> parseGpx(final String inputGpx) throws UserException {
		final SAXParser saxParser;
		try {
			saxParser = SAXParserFactory.newInstance().newSAXParser();
		} catch (final ParserConfigurationException e) {
			throw new RuntimeException("can't create XML parser", e);
		} catch (final SAXException e) {
			throw new RuntimeException("can't create XML parser", e);
		}
		
		final GpxContentHandler dh = new GpxContentHandler();
		try {
			saxParser.parse(new File(inputGpx), dh);
		} catch (final SAXException e) {
			throw new UserException("error parsing input GPX file", e);
		} catch (final IOException e) {
			throw new UserException("error reading input file", e);
		} catch (final RuntimeException e) {
			throw new RuntimeException("internal error when parsing GPX file", e);
		}
		
		return dh.getPointLists();
	}


}
