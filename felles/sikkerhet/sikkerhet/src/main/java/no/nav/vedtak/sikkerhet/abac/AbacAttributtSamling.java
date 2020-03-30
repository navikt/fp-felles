package no.nav.vedtak.sikkerhet.abac;

import java.util.Objects;
import java.util.Set;

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
        Objects.requireNonNull(jwtToken);
        return new AbacAttributtSamling(AbacIdToken.withOidcToken(jwtToken));
    }

    public static AbacAttributtSamling medSamlToken(String samlToken) {
        Objects.requireNonNull(samlToken);
        return new AbacAttributtSamling(AbacIdToken.withSamlToken(samlToken));
    }

    public AbacAttributtSamling leggTil(AbacDataAttributter dataAttributter) {
        this.dataAttributter.leggTil(dataAttributter);
        return this;
    }

    public<T> Set<T> getVerdier(AbacAttributtType type) {
        return dataAttributter.getVerdier(type);
    }

    public Set<AbacAttributtType> keySet() {
        return dataAttributter.keySet();
    }

    public AbacIdToken getIdToken() {
        return idToken;
    }

    @Override
    public String toString() {
        return AbacAttributtSamling.class.getSimpleName() + '{' + idToken +
            " action='" + action + "'" +
            " actionType='" + actionType + "'" +
            " resource='" + resource + "' " +
            dataAttributter +
            '}';
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

    AbacAttributtSamling setAction(String action) {
        this.action = action;
        return this;
    }

    public String getAction() {
        return action;
    }

}
