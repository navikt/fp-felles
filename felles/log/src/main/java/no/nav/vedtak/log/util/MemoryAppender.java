package no.nav.vedtak.log.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;

@Deprecated(since = "3.1.x", forRemoval = true)
/* Bruk samme fra no.nav.foreldrepenger.felles:log. */
public class MemoryAppender extends ListAppender<ILoggingEvent> {
    public MemoryAppender(String name) {
        this.name = name;
    }

    public void reset() {
        this.list.clear();
    }

    public boolean contains(String string, Level level) {
        return this.list.stream()
                .anyMatch(event -> event.getMessage().toString().contains(string)
                        && event.getLevel().equals(level));
    }

    public int countEventsForLogger() {
        return countEventsForLogger(name);
    }

    public int countEventsForLogger(String loggerName) {
        return (int) this.list.stream()
                .filter(event -> event.getLoggerName().contains(loggerName))
                .count();
    }

    public List<ILoggingEvent> search(String string) {
        return this.list.stream()
                .filter(event -> event.getMessage().toString().contains(string))
                .collect(Collectors.toList());
    }

    public List<ILoggingEvent> searchInfo(String string) {
        return search(string, Level.INFO);
    }

    public List<ILoggingEvent> search(String string, Level level) {
        return this.list.stream()
                .filter(event -> event.getMessage().toString().contains(string)
                        && event.getLevel().equals(level))
                .collect(Collectors.toList());
    }

    public int getSize() {
        return this.list.size();
    }

    public List<ILoggingEvent> getLoggedEvents() {
        return Collections.unmodifiableList(this.list);
    }

    public long countEntries(String substring) {
        return list.stream()
                .map(ILoggingEvent.class::cast)
                .filter(e -> eventMatches(e, substring)).count();
    }

    private static boolean eventMatches(ILoggingEvent event, String substring) {
        return Optional.ofNullable(substring)
                .filter(s -> event.getFormattedMessage().contains(s))
                .isPresent();

    }

    public static MemoryAppender sniff(Class<?> clazz) {
        return sniff(LoggerFactory.getLogger(clazz));
    }

    public static MemoryAppender sniff(org.slf4j.Logger logger) {
        var log = Logger.class.cast(logger);
        log.setLevel(Level.INFO);
        var sniffer = new MemoryAppender(log.getName());
        log.addAppender(sniffer);
        sniffer.start();
        return sniffer;
    }

}
