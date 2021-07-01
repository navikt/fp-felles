package no.nav.vedtak.sikkerhet.abac;

import java.net.URI;
import java.util.Objects;
import java.util.Set;

import com.nimbusds.jwt.SignedJWT;

import no.nav.vedtak.sikkerhet.abac.AbacIdToken.TokenType;

public class AbacAttributtSamling {
    private final AbacIdToken idToken;
    private final AbacDataAttributter dataAttributter = AbacDataAttributter.opprett();
    private BeskyttetRessursActionAttributt actionType;
    private String resource;
    private String action;

    private AbacAttributtSamling(AbacIdToken idToken) {
        this.idToken = idToken;
    }

    public static AbacAttributtSamling medJwtToken(String jwtToken) {
        return medJwtToken(jwtToken, oidcTokenType(jwtToken));
    }

    public static AbacAttributtSamling medJwtToken(String jwtToken, TokenType type) {
        Objects.requireNonNull(jwtToken);
        return new AbacAttributtSamling(AbacIdToken.withToken(jwtToken, type));
    }

    public static AbacAttributtSamling medSamlToken(String samlToken) {
        Objects.requireNonNull(samlToken);
        return new AbacAttributtSamling(AbacIdToken.withToken(samlToken, TokenType.SAML));
    }

    public AbacAttributtSamling leggTil(AbacDataAttributter dataAttributter) {
        this.dataAttributter.leggTil(dataAttributter);
        return this;
    }

    public <T> Set<T> getVerdier(AbacAttributtType type) {
        return dataAttributter.getVerdier(type);
    }

    public Set<AbacAttributtType> keySet() {
        return dataAttributter.keySet();
    }

    public AbacIdToken getIdToken() {
        return idToken;
    }

    public AbacAttributtSamling setActionType(BeskyttetRessursActionAttributt actionType) {
        this.actionType = actionType;
        return this;
    }

    public BeskyttetRessursActionAttributt getActionType() {
        return actionType;
    }

    public AbacAttributtSamling setResource(String resource) {
        this.resource = resource;
        return this;
    }

    public String getResource() {
        return resource;
    }

    public int getTotalAntallAttributter() {
        return dataAttributter.keySet().stream().mapToInt(k -> dataAttributter.getVerdier(k).size()).sum();
    }

    public int kryssProduktAntallAttributter() {
        return dataAttributter.keySet().stream()
                .mapToInt(k -> dataAttributter.getVerdier(k).size())
                .filter(s -> s > 0)
                .reduce(1, (a, b) -> a * b);
    }

    public AbacAttributtSamling setAction(String action) {
        this.action = action;
        return this;
    }

    public String getAction() {
        return action;
    }

    private static TokenType oidcTokenType(String token) {
        try {
            return URI.create(SignedJWT.parse(token)
                    .getJWTClaimsSet().getIssuer()).getHost().contains("tokendings") ? TokenType.TOKENX : TokenType.OIDC;

        } catch (Exception e) {
            throw new IllegalArgumentException("Ukjent token type");
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + " [idToken=" + idToken + ", dataAttributter=" + dataAttributter + ", actionType=" + actionType
                + ", resource=" + resource + ", action=" + action + "]";
    }
}
