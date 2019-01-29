package no.nav.vedtak.felles.integrasjon.jms;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface QueueProducerFeil extends DeklarerteFeil { // NOSONAR

    QueueProducerFeil FACTORY = FeilFactory.create(QueueProducerFeil.class); // NOSONAR ok med konstant i interface her

    // For bruk i enhetstester:
    class FeilKoder {
        public static final String MANGLER_INTERN_KONFIG = "F-929294";
        public static final String UVENTET_FEIL_VED_SENDING_AV_MELDING = "F-848913";

        private FeilKoder() {
            // hidden
        }
    }

    @TekniskFeil(feilkode = FeilKoder.MANGLER_INTERN_KONFIG, feilmelding = "%s er null.", logLevel = LogLevel.ERROR)
    Feil manglerInternKonfig(String navn);

    @TekniskFeil(feilkode = FeilKoder.UVENTET_FEIL_VED_SENDING_AV_MELDING, feilmelding = "Uventet feil ved håndtering av melding: %s", logLevel = LogLevel.ERROR)
    Feil uventetFeilVedSendingAvMelding(CharSequence errorDetails, Exception e);

}