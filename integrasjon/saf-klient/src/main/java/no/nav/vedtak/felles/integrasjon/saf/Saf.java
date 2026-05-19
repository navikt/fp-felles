package no.nav.vedtak.felles.integrasjon.saf;

import java.net.http.HttpResponse;
import java.util.List;

import no.nav.foreldrepenger.graphql.GraphQLOperationRequest;
import no.nav.foreldrepenger.graphql.GraphQLResponseProjection;
import no.nav.foreldrepenger.graphql.GraphQLResult;
import no.nav.saf.Dokumentoversikt;
import no.nav.saf.DokumentoversiktFagsakQueryRequest;
import no.nav.saf.DokumentoversiktResponseProjection;
import no.nav.saf.Journalpost;
import no.nav.saf.JournalpostQueryRequest;
import no.nav.saf.JournalpostResponseProjection;
import no.nav.saf.TilknyttedeJournalposterQueryRequest;

public interface Saf {

    Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest query, DokumentoversiktResponseProjection projection);

    Journalpost hentJournalpostInfo(JournalpostQueryRequest query, JournalpostResponseProjection projection);

    List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest query, JournalpostResponseProjection projection);

    byte[] hentDokument(HentDokumentQuery q);

    HttpResponse<byte[]> hentDokumentResponse(HentDokumentQuery q);

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

}
