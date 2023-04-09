package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.UserException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GpxContentHandlerTest {

    @Test
    void testCommentParsing() throws UserException {
        final var contentHandler = new GpxContentHandler();
        final var is = getClass().getResourceAsStream("/gpx/comment.gpx");
        GpxParser.parseGpx(is, contentHandler);

        final var track = contentHandler.getTrack();
        assertEquals("Comment for Track", track.getComment());

        final var trackSegments = track.getTrackSegments();
        assertEquals(1, trackSegments.size());

        final var trackSegment = trackSegments.get(0);
        final var trackPoints = trackSegment.getTrackPoints();
        assertEquals(6, trackPoints.size());

        final var trackPoint1 = trackPoints.get(0);
        assertEquals("Comment for Track Point 1", trackPoint1.getComment());
        final var trackPoint2 = trackPoints.get(1);
        assertNull(trackPoint2.getComment());
        final var trackPoint3 = trackPoints.get(2);
        assertEquals("Comment for Track Point 3", trackPoint3.getComment());
        final var trackPoint4 = trackPoints.get(3);
        assertNull(trackPoint4.getComment());
        final var trackPoint5 = trackPoints.get(4);
        assertEquals("", trackPoint5.getComment());
        final var trackPoint6 = trackPoints.get(5);
        assertNull(trackPoint6.getComment());
    }

}
