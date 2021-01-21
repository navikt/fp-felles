package no.nav.vedtak.felles.integrasjon.saf;

import static no.nav.vedtak.felles.integrasjon.saf.AbstractSafJerseyTjeneste.F_240613;

import java.net.URI;
import java.util.List;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.ws.rs.WebApplicationException;

import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktFagsakQueryResponse;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostQueryResponse;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.saf.TilknyttedeJournalposterQueryRequest;
import no.nav.saf.TilknyttedeJournalposterQueryResponse;
import no.nav.vedtak.exception.TekniskException;
import no.nav.vedtak.felles.integrasjon.rest.jersey.Jersey;
import no.nav.vedtak.konfig.KonfigVerdi;

@Dependent
@Jersey
public class SafJerseyTjeneste extends AbstractSafJerseyTjeneste implements Saf {
    private static final String HENTDOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}";

    @Inject
    public SafJerseyTjeneste(@KonfigVerdi(value = "saf.base.url", defaultVerdi = DEFAULT_BASE) URI base) {
        super(base);
    }

    @Override
    public Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest q, DokumentoversiktResponseProjection p) {
        return query(q, p, DokumentoversiktFagsakQueryResponse.class).dokumentoversiktFagsak();
    }

    @Override
    public Journalpost hentJournalpostInfo(JournalpostQueryRequest q, JournalpostResponseProjection p) {
        return query(q, p, JournalpostQueryResponse.class).journalpost();
    }

    @Override
    public List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest q, JournalpostResponseProjection p) {
        return query(q, p, TilknyttedeJournalposterQueryResponse.class).tilknyttedeJournalposter();
    }

    @Override
    public byte[] hentDokument(HentDokumentQuery q) {
        try {
            return client.target(base)
                    .path(HENTDOKUMENT)
                    .resolveTemplate("journalpostId", q.getJournalpostId())
                    .resolveTemplate("dokumentInfoId", q.getDokumentInfoId())
                    .resolveTemplate("variantFormat", q.getVariantFormat())
                    .request()
                    .get(byte[].class);
        } catch (WebApplicationException e) {
            throw new TekniskException(F_240613, base, e);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [base=" + base + "]";
    }
}
