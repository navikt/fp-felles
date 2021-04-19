package no.nav.foreldrepenger.sikkerhet.abac.pdp2.xacml;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;

import no.nav.foreldrepenger.felles.integrasjon.rest.DefaultJsonMapper;

public class XacmlRequestTest {

    @Test
    void serializeTest() throws JsonProcessingException {
        var actionSet = new XacmlRequest.AttributeSet(List.of(new XacmlRequest.Pair("urn:oasis:names:tc:xacml:1.0:action:action-id", "read")));
        var envSet = new XacmlRequest.AttributeSet(
            List.of(
                new XacmlRequest.Pair("no.nav.abac.attributter.environment.felles.pep_id", "local-app"),
                new XacmlRequest.Pair("no.nav.abac.attributter.environment.felles.oidc_token_body", "blatoken")
            ));
        var resourceSet = List.of(
            new XacmlRequest.AttributeSet(
                List.of(
                    new XacmlRequest.Pair("no.nav.abac.attributter.environment.felles.domene", "foreldrepenger"),
                    new XacmlRequest.Pair("no.nav.abac.attributter.resource.felles.resource_type", "no.nav.abac.attributter.foreldrepenger.fagsak"),
                    new XacmlRequest.Pair("no.nav.abac.attributter.resource.felles.person.fnr", "12345678900")
                ))
        );

        XacmlRequest.Request value = new XacmlRequest.Request(actionSet, envSet, resourceSet, null);
        XacmlRequest request = new XacmlRequest(value);

        var mapper = DefaultJsonMapper.MAPPER;
        String answer = mapper.writeValueAsString(request);

        assertThat(answer).contains("no.nav.abac.attributter.environment.felles.oidc_token_body");
        assertThat(answer).doesNotContain("AccessSubject");
    }
}
