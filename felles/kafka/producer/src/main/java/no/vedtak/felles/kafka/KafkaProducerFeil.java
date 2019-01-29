package no.vedtak.felles.kafka;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.errors.RetriableException;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;

public interface KafkaProducerFeil extends DeklarerteFeil {

    KafkaProducerFeil FACTORY = FeilFactory.create(KafkaProducerFeil.class);

    @ManglerTilgangFeil(feilkode = "VLKAFKA-821005", feilmelding = "Feil i pålogging mot Kafka", logLevel = LogLevel.ERROR)
    Feil feilIPålogging(Exception e);

    @IntegrasjonFeil(feilkode = "VLKAFKA-925469", feilmelding = "Uventet feil ved sending til Kafka", logLevel = LogLevel.WARN)
    Feil uventetFeil(Exception e);

    @IntegrasjonFeil(feilkode = "VLKAFKA-127608", feilmelding = "Fikk transient feil mot Kafka, kan prøve igjen", logLevel = LogLevel.WARN)
    Feil retriableExceptionMotKaka(RetriableException e);

    @IntegrasjonFeil(feilkode = "VLKAFKA-811208", feilmelding = "Fikk feil mot Kafka", logLevel = LogLevel.WARN)
    Feil annenExceptionMotKafka(KafkaException e);


}
