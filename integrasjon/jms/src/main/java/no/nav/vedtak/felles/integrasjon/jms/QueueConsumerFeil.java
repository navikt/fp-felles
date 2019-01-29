package no.nav.vedtak.felles.integrasjon.jms;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;

public interface QueueConsumerFeil extends DeklarerteFeil {

    QueueConsumerFeil FACTORY = FeilFactory.create(QueueConsumerFeil.class);

    @TekniskFeil(feilkode = "F-076505", feilmelding = "receive loop allerede startet.", logLevel = LogLevel.WARN)
    Feil receiveLoopAlleredeStartet();

    @TekniskFeil(feilkode = "F-167157", feilmelding = "receive loop ikke startet.", logLevel = LogLevel.WARN)
    Feil receiveLoopIkkeStartet();

    @TekniskFeil(feilkode = "F-452849", feilmelding = "feil ved stopping av receive loop: awaitTermination avbrutt.", logLevel = LogLevel.WARN)
    Feil feilVedStoppingAvReceiveLoopAwaitTerminationAvbrutt(InterruptedException e);

    @TekniskFeil(feilkode = "F-048287", feilmelding = "feil ved stopping av receive loop: executorService ikke terminert - gjør shutdownNow()", logLevel = LogLevel.WARN)
    Feil feilVedStoppingAvReceiveLoopExecutorServiceIkkeTerminert();

    @TekniskFeil(feilkode = "F-310549", feilmelding = "Precondition ikke oppfyllt: %s.", logLevel = LogLevel.ERROR)
    Feil preconditionIkkeOppfyllt(String errorMessage);

    @TekniskFeil(feilkode = "F-266229", feilmelding = "Uventet feil ved mottak av melding: %s", logLevel = LogLevel.ERROR)
    Feil uventetFeilVedMottakAvMelding(CharSequence errorDetails, Exception e);

    @TekniskFeil(feilkode = "F-848912", feilmelding = "Uventet feil ved håndtering av melding: %s", logLevel = LogLevel.ERROR)
    Feil uventetFeilVedHåndteringAvMelding(CharSequence errorDetails, Exception e);

    @TekniskFeil(feilkode = "F-158357", feilmelding = "Klarte ikke å connecte til MQ server: %s", logLevel = LogLevel.ERROR)
    Feil klarteIkkeÅConnecteTilMQServer(CharSequence errorDetails, Exception e);

    @TekniskFeil(feilkode = "F-551390", feilmelding = "Pausing avbrutt.", logLevel = LogLevel.ERROR)
    Feil pausingAvbrutt(Exception e);

    @TekniskFeil(feilkode = "F-703683", feilmelding = "Klarte ikke å starte tråd etter %s s ", logLevel = LogLevel.WARN)
    Feil klarteIkkeÅStarteTråd(int sekunder);

    @TekniskFeil(feilkode = "F-020443", feilmelding = "rollback feilet: %s", logLevel = LogLevel.WARN)
    Feil rollbackFeilet(JmsKonfig konfig, Exception e);

}
