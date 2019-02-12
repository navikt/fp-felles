package no.nav.vedtak.felles.integrasjon.felles.ws;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Exception-klasser som listes fra en slik konfigurasjon, vil ikke bli logget
 */
public abstract class VLFaultListenerUnntakKonfigurasjon {

    private final Set<Class<? extends Exception>> unntak;

    protected VLFaultListenerUnntakKonfigurasjon(Class<? extends Exception> exception) {
        unntak = Collections.singleton(exception);
    }

    protected VLFaultListenerUnntakKonfigurasjon(Collection<Class<? extends Exception>> exceptions) {
        unntak = new HashSet<>(exceptions);
    }

    public Set<Class<? extends Exception>> getUnntak() {
        return unntak;
    }
}
