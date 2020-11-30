package no.nav.vedtak.util.env;

import static ch.qos.logback.core.spi.FilterReply.DENY;
import static ch.qos.logback.core.spi.FilterReply.NEUTRAL;

import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.turbo.MarkerFilter;
import ch.qos.logback.core.spi.FilterReply;

public class ConfidentialMarkerFilter extends MarkerFilter {

    public static final Marker CONFIDENTIAL = MarkerFactory.getMarker("CONFIDENTIAL");

    public ConfidentialMarkerFilter() {
        super.setMarker(CONFIDENTIAL.getName());
    }

    @Override
    public FilterReply decide(Marker marker, Logger logger, Level level, String format, Object[] params, Throwable t) {
        if (!isStarted() || marker == null) {
            return NEUTRAL;
        }

        if (marker.equals(CONFIDENTIAL)) {
            return isProd() ? DENY : NEUTRAL;
        }
        return NEUTRAL;
    }

    private static boolean isProd() {
        return Environment.current().isProd();
    }
}
