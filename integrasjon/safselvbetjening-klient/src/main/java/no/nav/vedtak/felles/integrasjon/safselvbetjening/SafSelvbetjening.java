package no.nav.vedtak.felles.integrasjon.safselvbetjening;

import java.net.http.HttpResponse;

import no.nav.foreldrepenger.graphql.GraphQLOperationRequest;
import no.nav.foreldrepenger.graphql.GraphQLResponseProjection;
import no.nav.foreldrepenger.graphql.GraphQLResult;
import no.nav.safselvbetjening.Dokumentoversikt;
import no.nav.safselvbetjening.DokumentoversiktResponseProjection;
import no.nav.safselvbetjening.DokumentoversiktSelvbetjeningQueryRequest;


public interface SafSelvbetjening {

    Dokumentoversikt dokumentoversiktSelvbetjening(DokumentoversiktSelvbetjeningQueryRequest query, DokumentoversiktResponseProjection projection);

    byte[] hentDokument(HentDokumentQuery q);

    HttpResponse<byte[]> hentDokumentResponse(HentDokumentQuery q);

    <T extends GraphQLResult<?>> T query(GraphQLOperationRequest q, GraphQLResponseProjection p, Class<T> clazz);

}
