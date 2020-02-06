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
package app.gpx_animator;

import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.zip.GZIPInputStream;

public final class GpxParser {

    private GpxParser() throws InstantiationException {
        throw new InstantiationException("GpxParser is a utility class and can't be instantiated!");
    }

    static void parseGpx(final File inputGpx, final GpxContentHandler dh) throws UserException {
        final SAXParser saxParser;
        try {
            saxParser = SAXParserFactory.newInstance().newSAXParser();
        } catch (final ParserConfigurationException | SAXException e) {
            throw new RuntimeException("can't create XML parser", e);
        }

        try {
            try (InputStream is = new FileInputStream(inputGpx)) {
                try (InputStream dis = decompressStream(is)) {
                    saxParser.parse(dis, dh);
                } catch (final SAXException e) {
                    throw new UserException("error parsing input GPX file", e);
                } catch (final RuntimeException e) {
                    throw new RuntimeException("internal error when parsing GPX file", e);
                }
            }
        } catch (final IOException e) {
            throw new UserException("error reading input file", e);
        }
    }

    @SuppressWarnings("PMD.CloseResource") // The returned stream will be used and closed in a try-with-resources block
    public static InputStream decompressStream(final InputStream input) throws IOException {
        final PushbackInputStream pb = new PushbackInputStream(input, 2);
        final byte[] signature = new byte[2];
        final int numBytesRead = pb.read(signature);
        pb.unread(signature, 0, numBytesRead);
        return signature[0] == (byte) 0x1f && signature[1] == (byte) 0x8b ? new GZIPInputStream(pb) : pb;
    }

}
