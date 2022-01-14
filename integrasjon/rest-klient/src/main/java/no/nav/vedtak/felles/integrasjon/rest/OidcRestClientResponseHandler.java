package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectReader;

import no.nav.vedtak.exception.IntegrasjonException;
import no.nav.vedtak.exception.ManglerTilgangException;

public abstract class OidcRestClientResponseHandler<T> implements ResponseHandler<T> {

    private final URI endpoint;
    private final Set<Integer> permits;

    public OidcRestClientResponseHandler(URI endpoint) {
        this(endpoint, Set.of());
    }

    public OidcRestClientResponseHandler(URI endpoint, Set<Integer> permits) {
        this.endpoint = endpoint;
        this.permits = permits;
    }

    @Override
    public T handleResponse(final HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_NO_CONTENT) {
            return null;
        }
        if ((status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) || permits.contains(status)) {
            HttpEntity entity = response.getEntity();
            return entity != null ? readEntity(entity) : null;
        }
        if (status == HttpStatus.SC_FORBIDDEN) {
            throw new ManglerTilgangException("F-468815", "Feilet mot " + endpoint);
        }
        throw new IntegrasjonException("F-468815", String.format("Uventet respons %s fra %s", status, endpoint));
    }

    protected abstract T readEntity(HttpEntity entity) throws IOException;

    public static class StringResponseHandler extends OidcRestClientResponseHandler<String> {
        public StringResponseHandler(URI endpoint) {
            super(endpoint);
        }

        /**
         * default håndteres alt som string.
         */
        @Override
        protected String readEntity(HttpEntity entity) throws IOException {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
    }

    public static class StringResponseHandlerPermitConflict extends OidcRestClientResponseHandler<String> {
        public StringResponseHandlerPermitConflict(URI endpoint) {
            super(endpoint, Set.of(HttpStatus.SC_CONFLICT));
        }

        /**
         * default håndteres alt som string.
         */
        @Override
        protected String readEntity(HttpEntity entity) throws IOException {
            return EntityUtils.toString(entity, StandardCharsets.UTF_8);
        }
    }

    public static class ByteArrayResponseHandler extends OidcRestClientResponseHandler<byte[]> {
        public ByteArrayResponseHandler(URI endpoint) {
            super(endpoint);
        }

        @Override
        protected byte[] readEntity(HttpEntity entity) throws IOException {
            return EntityUtils.toByteArray(entity);
        }
    }

    public static class ObjectReaderResponseHandler<T> extends OidcRestClientResponseHandler<T> {

        private ObjectReader reader;

        public ObjectReaderResponseHandler(URI endpoint, ObjectReader reader) {
            super(endpoint);
            this.reader = reader;
        }

        @Override
        protected T readEntity(HttpEntity entity) throws IOException {
            return reader.readValue(entity.getContent());
        }

    }
}
