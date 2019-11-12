package no.nav.vedtak.felles.integrasjon.sigrun;

import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.AUTH_HEADER;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.CALL_ID;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.CONSUMER_ID;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.FILTER;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.FILTER_SSG;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.INNTEKTSAAR;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.INNTEKTSFILTER;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.NAV_PERSONIDENT;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.NYE_HEADER_CALL_ID;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.NYE_HEADER_CONSUMER_ID;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.OIDC_AUTH_HEADER_PREFIX;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.PATH_SSG;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.X_AKTØRID;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.X_FILTER;
import static no.nav.vedtak.felles.integrasjon.sigrun.SigrunRestConfig.X_INNTEKTSÅR;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;

import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.IntegrasjonFeil;
import no.nav.vedtak.feil.deklarasjon.ManglerTilgangFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.isso.OpenAMHelper;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;

public class SigrunRestClient {
    private static final Logger LOG = LoggerFactory.getLogger(SigrunRestClient.class);
    private final ResponseHandler<String> defaultResponseHandler;
    private CloseableHttpClient client;
    private URI endpoint;

    SigrunRestClient(CloseableHttpClient closeableHttpClient) {
        super();
        client = closeableHttpClient;
        defaultResponseHandler = createDefaultResponseHandler();
    }

    //api/beregnetskatt
    String hentBeregnetSkattForAktørOgÅr(long aktørId, String år) {
        HttpRequestBase request = lagRequestBS(år, aktørId);
        String response;
        try {
            response = client.execute(request, defaultResponseHandler);
        } catch (IOException e) {
            throw SigrunRestClient.SigrunRestClientFeil.FACTORY.ioException(e).toException();
        } finally {
            request.reset();
        }
        return response;
    }

    //api/v1/summertskattegrunnlag
    String hentSummertskattegrunnlag(long aktørId, String år) {
        HttpRequestBase request = lagRequestSSG(aktørId, år);
        String response;
        try {
            response = client.execute(request, defaultResponseHandler);
        } catch (IOException e) {
            throw SigrunRestClient.SigrunRestClientFeil.FACTORY.ioException(e).toException();
        } finally {
            request.reset();
        }
        return response;
    }

    private HttpRequestBase lagRequestSSG(long aktørId, String år) {
        URIBuilder builder = new URIBuilder();
        builder.setParameter(INNTEKTSAAR, år);
        builder.setParameter(INNTEKTSFILTER, FILTER_SSG);
        builder.setPath(endpoint.getPath() + PATH_SSG);
        URI build = null;
        try {
            build = builder.build();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        HttpRequestBase request = new HttpGet(build);
        request.addHeader(NAV_PERSONIDENT, String.valueOf(aktørId));
        String authHeaderValue = OIDC_AUTH_HEADER_PREFIX + getOIDCToken();
        request.setHeader(AUTH_HEADER, authHeaderValue);
        request.setHeader(CALL_ID, MDCOperations.getCallId());
        request.setHeader(NYE_HEADER_CALL_ID, MDCOperations.getCallId());
        request.setHeader("Accept", "application/json");
        request.setHeader(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());

        return request;
    }

    private HttpRequestBase lagRequestBS(String år, long aktørId) {
        HttpRequestBase request = new HttpGet(endpoint.resolve(endpoint.getPath() + SigrunRestConfig.PATH));
        String authHeaderValue = OIDC_AUTH_HEADER_PREFIX + getOIDCToken();
        request.setHeader(AUTH_HEADER, authHeaderValue);
        request.addHeader(X_FILTER, FILTER);
        request.addHeader(X_AKTØRID, String.valueOf(aktørId));
        request.addHeader(X_INNTEKTSÅR, år);
        request.setHeader(CALL_ID, MDCOperations.getCallId());
        request.setHeader(NYE_HEADER_CALL_ID, MDCOperations.getCallId());
        request.setHeader("Accept", "application/json");
        request.setHeader(CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        request.setHeader(NYE_HEADER_CONSUMER_ID, SubjectHandler.getSubjectHandler().getConsumerId());
        return request;
    }

    private String getOIDCToken() {
        String oidcToken = SubjectHandler.getSubjectHandler().getInternSsoToken();
        if (oidcToken != null) {
            return oidcToken;
        }

        Element samlToken = SubjectHandler.getSubjectHandler().getSamlToken();
        if (samlToken != null) {
            return veksleSamlTokenTilOIDCToken(samlToken);
        }
        throw SigrunRestClient.SigrunRestClientFeil.FACTORY.klarteIkkeSkaffeOIDCToken().toException();
    }

    private ResponseHandler<String> createDefaultResponseHandler() {
        return response -> {
            int status = response.getStatusLine().getStatusCode();
            if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
                HttpEntity entity = response.getEntity();
                return entity != null ? EntityUtils.toString(entity) : null;
            } else if (status == HttpStatus.SC_FORBIDDEN) {
                throw SigrunRestClientFeil.FACTORY.manglerTilgang().toException();
            } else if (status == HttpStatus.SC_NOT_FOUND) {
                LOG.info("Sigrun: " + response.getStatusLine().getReasonPhrase());
                return null;
            } else {
                throw SigrunRestClientFeil.FACTORY.serverSvarteMedFeilkode(status, response.getStatusLine().getReasonPhrase()).toException();
            }
        };
    }

    //FIXME (u139158): PK-50281 STS for SAML til OIDC
    // I mellomtiden bruker vi systemets OIDC-token, dvs vi propagerer ikke sikkerhetskonteksten
    private String veksleSamlTokenTilOIDCToken(@SuppressWarnings("unused") Element samlToken) {
        try {
            return new OpenAMHelper().getToken().getIdToken().getToken();
        } catch (IOException e) {
            throw SigrunRestClient.SigrunRestClientFeil.FACTORY.feilVedHentingAvSystemToken(e).toException();
        }
    }

    void setEndpoint(URI endpoint) {
        this.endpoint = endpoint;
    }

    interface SigrunRestClientFeil extends DeklarerteFeil {

        SigrunRestClient.SigrunRestClientFeil FACTORY = FeilFactory.create(SigrunRestClient.SigrunRestClientFeil.class);

        @ManglerTilgangFeil(feilkode = "F-018815", feilmelding = "Mangler tilgang. Fikk http-kode 403 fra server", logLevel = LogLevel.ERROR)
        Feil manglerTilgang();

        @IntegrasjonFeil(feilkode = "F-016912", feilmelding = "Server svarte med feilkode http-kode '%s' og response var '%s'", logLevel = LogLevel.WARN)
        Feil serverSvarteMedFeilkode(int feilkode, String feilmelding);

        @TekniskFeil(feilkode = "F-012937", feilmelding = "IOException ved kommunikasjon med server", logLevel = LogLevel.WARN)
        Feil ioException(IOException cause);

        @TekniskFeil(feilkode = "F-011590", feilmelding = "IOException ved henting av systemets OIDC-token", logLevel = LogLevel.ERROR)
        Feil feilVedHentingAvSystemToken(IOException cause);

        @TekniskFeil(feilkode = "F-017072", feilmelding = "Klarte ikke å fremskaffe et OIDC token", logLevel = LogLevel.ERROR)
        Feil klarteIkkeSkaffeOIDCToken();

    }
}
