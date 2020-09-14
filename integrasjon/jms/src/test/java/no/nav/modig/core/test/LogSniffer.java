//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package no.nav.modig.core.test;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.junit.rules.TestRule;
import org.junit.runner.Description;
import org.junit.runners.model.Statement;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.classic.turbo.TurboFilter;
import ch.qos.logback.core.spi.FilterReply;

/*
 * I serien grisete kode presenterer vi denne, som heldigvis snart skal dø
 */
@Deprecated(forRemoval = true, since = "2.1.x")
public class LogSniffer implements TestRule {
    private final Map<ILoggingEvent, Boolean> logbackAppender;
    private final Level minimumLevel;
    private final boolean denyOthersWhenMatched;

    static void initLogSystem() {
        LogManager.getLogManager().reset();
        Logger.getLogger("global").setLevel(java.util.logging.Level.FINEST);
    }

    public LogSniffer() {
        this(Level.INFO);
    }

    public LogSniffer(Level minimumLevel) {
        this(minimumLevel, true);
    }

    public LogSniffer(Level minimumLevel, boolean denyOthersWhenMatched) {
        this.logbackAppender = new LinkedHashMap<>();
        this.minimumLevel = minimumLevel;
        this.denyOthersWhenMatched = denyOthersWhenMatched;
        this.installTurboFilter(this.getLoggerContext());
    }

    private LoggerContext getLoggerContext() {
        return (LoggerContext) LoggerFactory.getILoggerFactory();
    }

    private void installTurboFilter(LoggerContext lc) {
        lc.addTurboFilter(new TurboFilter() {
            @Override
            public FilterReply decide(Marker marker, ch.qos.logback.classic.Logger logger, Level level, String format, Object[] argArray,
                    Throwable t) {
                if (format != null && level != null && level.isGreaterOrEqual(LogSniffer.this.minimumLevel)) {
                    LoggingEvent loggingEvent = new LoggingEvent(ch.qos.logback.classic.Logger.FQCN, logger, level, format, t, argArray);
                    LogSniffer.this.logbackAppender.put(loggingEvent, Boolean.FALSE);
                    return LogSniffer.this.denyOthersWhenMatched ? FilterReply.DENY : FilterReply.NEUTRAL;
                } else {
                    return FilterReply.NEUTRAL;
                }
            }
        });
    }

    public void assertHasWarnMessage(String substring) {
        if (!this.hasLogEntry(substring, (Class<? extends Throwable>) null, Level.WARN)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level WARN.  Has [%s]", substring, this));
        } else {
            this.markEntryAsserted(substring, (Class<? extends Throwable>) null, Level.WARN);
        }
    }

    public void assertHasErrorMessage(String substring) {
        if (!this.hasLogEntry(substring, (Class<? extends Throwable>) null, Level.ERROR)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level WARN.  Has [%s]", substring, this));
        } else {
            this.markEntryAsserted(substring, (Class<? extends Throwable>) null, Level.ERROR);
        }
    }

    public void assertHasInfoMessage(String substring) {
        if (!this.hasLogEntry(substring, (Class<? extends Throwable>) null, Level.INFO)) {
            throw new AssertionError(String.format("Could not find log message matching [%s], with level INFO.  Has [%s]", substring, this));
        } else {
            this.markEntryAsserted(substring, (Class<? extends Throwable>) null, Level.INFO);
        }
    }

    public void assertNoErrors() {
        if (this.countErrors() > 0) {
            throw new AssertionError("No errors expected, but has " + this);
        }
    }

    public void assertNoWarnings() {
        if (this.countWarnings() > 0) {
            throw new AssertionError("No warnings expected, but has " + this);
        }
    }

    public void assertNoErrorsOrWarnings() {
        this.assertNoErrors();
        this.assertNoWarnings();
    }

    public void assertNoLogEntries() {
        if (!this.logbackAppender.isEmpty()) {
            throw new AssertionError("No log entries expected, but has " + this);
        }
    }

    private void markEntryAsserted(String substring, Class<? extends Throwable> t, Level level) {
        var i$ = (Set.of(this.logbackAppender.keySet())).iterator();

        while (i$.hasNext()) {
            ILoggingEvent loggingEvent = (ILoggingEvent) i$.next();
            if (this.eventMatches(loggingEvent, substring, t, level)) {
                this.logbackAppender.put(loggingEvent, Boolean.TRUE);
            }
        }

    }

    private boolean eventMatches(ILoggingEvent loggingEvent, String substring, Class<? extends Throwable> t, Level level) {
        if (substring != null && !loggingEvent.getFormattedMessage().contains(substring)) {
            return false;
        } else if (t != null && (loggingEvent.getThrowableProxy() == null || loggingEvent.getThrowableProxy().getClassName() == null
                || !loggingEvent.getThrowableProxy().getClassName().equals(t.getName()))) {
            return false;
        } else {
            return level == null || level == loggingEvent.getLevel();
        }
    }

    private int countErrors() {
        int logErrors = 0;
        var i$ = this.logbackAppender.entrySet().iterator();

        while (i$.hasNext()) {
            Entry<ILoggingEvent, Boolean> entry = i$.next();
            if (entry.getKey().getLevel().equals(Level.ERROR) && entry.getValue() != Boolean.TRUE) {
                ++logErrors;
            }
        }

        return logErrors;
    }

    private int countWarnings() {
        int logWarnings = 0;
        var i$ = this.logbackAppender.entrySet().iterator();

        while (i$.hasNext()) {
            Entry<ILoggingEvent, Boolean> entry = i$.next();
            if (entry.getKey().getLevel().equals(Level.WARN) && entry.getValue() != Boolean.TRUE) {
                ++logWarnings;
            }
        }

        return logWarnings;
    }

    public int countEntries(String substring) {
        return this.countLogbackEntries(substring, (Class<? extends Throwable>) null, (Level) null);
    }

    private boolean hasLogEntry(String substring, Class<? extends Throwable> t, Level level) {
        return this.countLogbackEntries(substring, t, level) > 0;
    }

    private int countLogbackEntries(String substring, Class<? extends Throwable> t, Level level) {
        int count = 0;
        var i$ = this.logbackAppender.keySet().iterator();

        while (i$.hasNext()) {
            ILoggingEvent loggingEvent = i$.next();
            if (this.eventMatches(loggingEvent, substring, t, level)) {
                ++count;
            }
        }

        return count;
    }

    public void clearLog() {
        this.logbackAppender.clear();
    }

    @Override
    public Statement apply(final Statement base, Description description) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    base.evaluate();
                    LogSniffer.this.assertNoErrorsOrWarnings();
                } finally {
                    LogSniffer.this.getLoggerContext().resetTurboFilterList();
                }

            }
        };
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder();
        var i$ = this.logbackAppender.keySet().iterator();

        while (i$.hasNext()) {
            ILoggingEvent event = i$.next();
            buf.append(event.getLevel() + ":" + event.getFormattedMessage()).append('\n');
        }

        return buf.toString();
    }

    static {
        initLogSystem();
    }
}
