package no.nav.vedtak.felles.integrasjon.rest;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ResponseHandler;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.ObjectReader;

public abstract class OidcRestClientResponseHandler<T> implements ResponseHandler<T> {

    private URI endpoint;

    public OidcRestClientResponseHandler(URI endpoint) {
        this.endpoint = endpoint;
    }

    @Override
    public T handleResponse(final HttpResponse response) throws IOException {
        int status = response.getStatusLine().getStatusCode();
        if (status == HttpStatus.SC_NO_CONTENT) {
            return null;
        } else if (status >= HttpStatus.SC_OK && status < HttpStatus.SC_MULTIPLE_CHOICES) {
            HttpEntity entity = response.getEntity();
            return entity != null ? readEntity(entity) : null;
        } else if (status == HttpStatus.SC_FORBIDDEN) {
            throw OidcRestClientFeil.FACTORY.manglerTilgang(OidcRestClientFeil.formatterURI(endpoint)).toException();
        } else {
            throw OidcRestClientFeil.FACTORY.serverSvarteMedFeilkode(
                OidcRestClientFeil.formatterURI(endpoint),
                status,
                response.getStatusLine().getReasonPhrase()).toException();
        }
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

        @SuppressWarnings("resource")
        @Override
        protected T readEntity(HttpEntity entity) throws IOException {
            return reader.readValue(entity.getContent());
        }

    }
}
