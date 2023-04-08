package app.gpx_animator.core.data.entity;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@SuppressFBWarnings(value = {"EI_EXPOSE_REP", "EI_EXPOSE_REP2"}) // the complete hierarchy is immutable
public record Track(@Nullable String name, @Nullable String comment, @Nullable TrackType type, @NotNull List<TrackSegment> trackSegments) { }
