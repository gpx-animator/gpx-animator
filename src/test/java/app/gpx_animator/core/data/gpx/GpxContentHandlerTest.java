package app.gpx_animator.core.data.gpx;

import app.gpx_animator.core.UserException;
import app.gpx_animator.core.data.LatLon;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class GpxContentHandlerTest {

    @Test
    void test() throws UserException {
        final var contentHandler = new GpxContentHandler();
        final var is = getClass().getResourceAsStream("/gpx/comment.gpx");
        GpxParser.parseGpx(is, contentHandler);

        final var trackSegmentList = contentHandler.getPointLists();
        assertEquals(1, trackSegmentList.size());
        final var trackSegment = trackSegmentList.get(0);
        assertEquals(6, trackSegment.size());
        assertEquals("Track Point Comment 1", trackSegment.get(0).getCmt());
        assertNull(trackSegment.get(1).getCmt());
        assertEquals("Track Point Comment 2", trackSegment.get(2).getCmt());
        assertNull(trackSegment.get(3).getCmt());
        assertEquals("", trackSegment.get(4).getCmt());
        assertNull(trackSegment.get(5).getCmt());
    }

}
