package no.nav.vedtak.felles.integrasjon.aktør.klient;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.xml.ws.soap.SOAPFaultException;

import no.nav.tjeneste.virksomhet.aktoer.v2.binding.AktoerV2;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentAktoerIdForIdentPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.binding.HentIdentForAktoerIdPersonIkkeFunnet;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.AktoerIder;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentListeRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentListeResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentAktoerIdForIdentResponse;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdRequest;
import no.nav.tjeneste.virksomhet.aktoer.v2.meldinger.HentIdentForAktoerIdResponse;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebServiceFeil;

class AktørConsumerImpl implements AktørConsumer {
    public static final String SERVICE_IDENTIFIER = "AktoerV2";

    private final AktoerV2 port;

    public AktørConsumerImpl(AktoerV2 port) {
        this.port = port;
    }

    @Override
    public Optional<String> hentAktørIdForPersonIdent(String personIdent) {
        HentAktoerIdForIdentRequest request = new HentAktoerIdForIdentRequest();
        request.setIdent(personIdent);
        try {
            HentAktoerIdForIdentResponse svar = port.hentAktoerIdForIdent(request);
            return Optional.of(svar.getAktoerId());
        } catch (HentAktoerIdForIdentPersonIkkeFunnet e) { // NOSONAR
            return Optional.empty(); //NOSONAR
        } catch (SOAPFaultException e) { // NOSONAR
            if (e.getMessage().contains("status: S511002F")) {
                //Merkelig, men dette er oppførsel fra Tps når det finnes to
                //som har samme personIdent. Det eneste tilfellet vi vet det skjer
                //er ved dødfødsler.
                throw FeilFactory.create(AktørConsumerFeil.class).flereAktørerMedSammeIdent().toException();
            } else {
                throw SoapWebServiceFeil.FACTORY.soapFaultIwebserviceKall(SERVICE_IDENTIFIER, e).toException();
            }
        }
    }

    @Override
    public Optional<String> hentPersonIdentForAktørId(String aktørId) {
        HentIdentForAktoerIdRequest request = new HentIdentForAktoerIdRequest();
        request.setAktoerId(aktørId);
        try {
            HentIdentForAktoerIdResponse svar = port.hentIdentForAktoerId(request);
            return Optional.of(svar.getIdent());
        } catch (HentIdentForAktoerIdPersonIkkeFunnet hentIdentForAktoerIdPersonIkkeFunnet) { // NOSONAR
            return Optional.empty(); //NOSONAR
        }
    }

    @Override
    public List<AktoerIder> hentAktørIdForPersonIdentSet(Set<String> personIdentSet) {
        HentAktoerIdForIdentListeRequest request = new HentAktoerIdForIdentListeRequest();
        request.getIdentListe().addAll(personIdentSet);

        HentAktoerIdForIdentListeResponse svar = port.hentAktoerIdForIdentListe(request);
        return svar.getAktoerListe();
    }
}
