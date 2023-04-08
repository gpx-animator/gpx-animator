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
        final var trackSegments = track.trackSegments();
        assertEquals(1, trackSegments.size());
        final var trackSegment = trackSegments.get(0);
        assertEquals(6, trackSegment.size());
        assertEquals("Track Point Comment 1", trackSegment.getTrackPoint(0).comment());
        assertNull(trackSegment.getTrackPoint(1).comment());
        assertEquals("Track Point Comment 2", trackSegment.getTrackPoint(2).comment());
        assertNull(trackSegment.getTrackPoint(3).comment());
        assertEquals("", trackSegment.getTrackPoint(4).comment());
        assertNull(trackSegment.getTrackPoint(5).comment());
    }

}
