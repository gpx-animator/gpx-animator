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
        assertEquals("Comment for Track", track.comment());

        final var trackSegments = track.trackSegments();
        assertEquals(1, trackSegments.size());

        final var trackSegment = trackSegments.get(0);
        assertEquals(6, trackSegment.size());

        final var trackPoint1 = trackSegment.getTrackPoint(0);
        assertEquals("Comment for Track Point 1", trackPoint1.comment());
        final var trackPoint2 = trackSegment.getTrackPoint(0);
        assertNull(trackPoint2.comment());
        final var trackPoint3 = trackSegment.getTrackPoint(0);
        assertEquals("Comment for Track Point 3", trackPoint3.comment());
        final var trackPoint4 = trackSegment.getTrackPoint(0);
        assertNull(trackPoint4.comment());
        final var trackPoint5 = trackSegment.getTrackPoint(0);
        assertEquals("", trackPoint5.comment());
        final var trackPoint6 = trackSegment.getTrackPoint(0);
        assertNull(trackPoint6.comment());
    }

}
