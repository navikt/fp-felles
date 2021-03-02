package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;

import no.nav.vedtak.exception.TekniskException;

class DefaultJsonMapperFeil {

    private DefaultJsonMapperFeil() {

    }

    static TekniskException ioExceptionVedLesing(IOException e) {
        return new TekniskException("F-919328", "Fikk IO exception ved parsing av JSON", e);
    }

    static TekniskException kunneIkkeSerialisereJson(IOException e) {
        return new TekniskException("F-208314", "Kunne ikke serialisere objekt til JSON", e);
    }
}