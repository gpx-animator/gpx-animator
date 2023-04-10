package app.gpx_animator.core.data.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.With;

import java.util.List;

@With
@Getter
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@SuppressFBWarnings() // lombok
public final class Track {
    String name;
    String comment;
    TrackType type = TrackType.UNKNOWN;
    List<TrackSegment> trackSegments = List.of();
}
