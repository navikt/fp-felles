package no.nav.vedtak.felles.integrasjon.dokument.produksjon.v2;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

@ApplicationScoped
public class DokumentproduksjonConsumerProducerDelegator {
    private DokumentproduksjonConsumerProducer producer;

    @Inject
    public DokumentproduksjonConsumerProducerDelegator(DokumentproduksjonConsumerProducer producer) {
        this.producer = producer;
    }

    @Produces
    public DokumentproduksjonConsumer dokumentproduksjonConsumerForEndUser() {
        return producer.dokumentproduksjonConsumer();
    }

    @Produces
    public DokumentproduksjonSelftestConsumer dokumentproduksjonSelftestConsumerForSystemUser() {
        return producer.dokumentproduksjonSelftestConsumer();
    }
}
