package app.gpx_animator;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

import java.util.List;
import java.util.stream.Collectors;

public final class MemoryAppender extends ListAppender<ILoggingEvent> {

    public void reset() {
        this.list.clear();
    }

    public List<ILoggingEvent> search(final String string, final Level level) {
        return this.list.stream()
          .filter(event -> event.getMessage().contains(string)
            && event.getLevel().equals(level))
          .collect(Collectors.toList());
    }

    public List<ILoggingEvent> searchFormattedMessages(final String string, final Level level) {
        return this.list.stream()
          .filter(event -> event.getFormattedMessage().contains(string)
            && event.getLevel().equals(level))
          .collect(Collectors.toList());
    }

}
