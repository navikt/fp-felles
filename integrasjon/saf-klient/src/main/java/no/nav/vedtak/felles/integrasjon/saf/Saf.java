package no.nav.vedtak.felles.integrasjon.saf;

import java.util.List;

import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLOperationRequest;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResponseProjection;
import com.kobylynskyi.graphql.codegen.model.graphql.GraphQLResult;

import no.nav.saf.*;

public interface Saf {

    Dokumentoversikt dokumentoversiktFagsak(DokumentoversiktFagsakQueryRequest query, DokumentoversiktResponseProjection projection);

    Journalpost hentJournalpostInfo(JournalpostQueryRequest query, JournalpostResponseProjection projection);

    List<Journalpost> hentTilknyttedeJournalposter(TilknyttedeJournalposterQueryRequest query, JournalpostResponseProjection projection);

    byte[] hentDokument(HentDokumentQuery q);

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

}
