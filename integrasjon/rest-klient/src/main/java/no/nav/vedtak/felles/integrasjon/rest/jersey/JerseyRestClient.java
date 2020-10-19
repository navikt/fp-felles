package no.nav.vedtak.felles.integrasjon.rest.jersey;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

public class JerseyRestClient {

    private static final String REST_URI = "http://localhost:8082/spring-jersey/resources/employees";

    private Client client = ClientBuilder.newClient();

    public String getJsonEmployee(int id) {
        return client
                
                .target(REST_URI)
                .path(String.valueOf(id))
                .request(MediaType.APPLICATION_JSON)
                .get(String.class);
    }
}
