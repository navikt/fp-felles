package no.nav.vedtak.felles.integrasjon.felles.ws;

import no.nav.vedtak.exception.VLException;
import no.nav.vedtak.log.util.LoggerUtils;
import org.apache.cxf.interceptor.Fault;
import org.apache.cxf.logging.FaultListener;
import org.apache.cxf.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

public class VLFaultListener implements FaultListener {

    private static final Logger LOG = LoggerFactory.getLogger(VLFaultListener.class);

    private final Set<Class<? extends Exception>> unntak = new HashSet<>();

    @Override
    public boolean faultOccurred(Exception exception, String description, Message message) {
        Throwable rootCause = (exception instanceof Fault && exception.getCause() != null) ? exception.getCause() : exception;

        if (unntak.contains(rootCause.getClass())) {
            // skal ikke logge
            return false;
        }

        if (rootCause instanceof VLException) {
            LOG.warn("VL exception", rootCause);
        } else {
            LOG.error("Uventet exception: {}", LoggerUtils.removeLineBreaks(description), rootCause); // NOSONAR
        }
        return false;
    }

    public void leggTilUnntak(VLFaultListenerUnntakKonfigurasjon unntakKonfigurasjon) {
        for (Class<? extends Exception> uk : unntakKonfigurasjon.getUnntak()) {
            if (VLException.class.isAssignableFrom(uk)) {
                throw new IllegalArgumentException(
                    "Det gir ikke mening å unnta " + VLException.class.getName() + " fra logging. Juster heller logLevel i deklarajonen");
            }
        }
        unntak.addAll(unntakKonfigurasjon.getUnntak());
    }
}
