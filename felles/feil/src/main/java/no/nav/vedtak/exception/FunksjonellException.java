package no.nav.vedtak.exception;


import no.nav.vedtak.feil.FunksjonellFeil;

public class FunksjonellException extends VLException {

    public FunksjonellException(FunksjonellFeil feil) {
        super(feil);
    }

    @Override
    public FunksjonellFeil getFeil() {
        return (FunksjonellFeil) super.getFeil(); //NOSONAR Uproblematisk cast, finnes bare constructorer som tar inn denne typen
    }
}
