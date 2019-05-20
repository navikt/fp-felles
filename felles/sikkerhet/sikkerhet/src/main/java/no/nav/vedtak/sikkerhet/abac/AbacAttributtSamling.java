package no.nav.vedtak.sikkerhet.abac;

import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class AbacAttributtSamling {
    private final AbacIdToken idToken;
    private final AbacDataAttributter dataAttributter = AbacDataAttributter.opprett();
    private BeskyttetRessursActionAttributt actionType;
    private BeskyttetRessursResourceAttributt resource;
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

    public Set<String> getSaksnummre() {
        return dataAttributter.getSaksnummre();
    }

    public Set<Long> getBehandlingsIder() {
        return dataAttributter.getBehandlingIder();
    }
    
    public Set<String> getBehandlingsUUIDer() {
        return dataAttributter.getBehandlingsUUIDer();
    }
    
    public Set<Long> getSPBeregningsIder() {
        return dataAttributter.getSPBeregningsIder();
    }

    public Set<String> getFødselsnumre() {
        return dataAttributter.getFødselsnumre();
    }

    public Set<String> getFnrForSøkEtterSaker() {
        return dataAttributter.getFnrForSøkEtterSaker();
    }

    public Set<String> getJournalpostIder(boolean påkrevde) {
        return dataAttributter.getJournalpostIder(påkrevde);
    }

    public Set<String> getOppgaveIder() {
        return dataAttributter.getOppgaveIder();
    }

    public Set<String> getOppgavestyringEnhet(){
        return dataAttributter.getOppgavestyringEnhet();
    }

    public Set<String> getAnnenPart(){
        return dataAttributter.getAnnenPart();
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

    public AbacAttributtSamling setResource(BeskyttetRessursResourceAttributt resource) {
        this.resource = resource;
        return this;
    }

    public BeskyttetRessursResourceAttributt getResource() {
        return resource;
    }

    public Set<String> getAksjonspunktKode() {
        return dataAttributter.getAksjonspunktKode();
    }

    public Set<String> getAktørIder() {
        return dataAttributter.getAktørIder();
    }

    public Set<Long> getDokumentDataIDer() {
        return dataAttributter.getDokumentDataId();
    }

    public Set<String> getDokumentIDer() {
        return dataAttributter.getDokumentId();
    }

    public Set<Long> getFagsakIder() {
        return dataAttributter.getFagsakIder();
    }

    public Set<UUID> getDokumentforsendelseIder() {
        return dataAttributter.getDokumentforsendelseIder();
    }

    AbacAttributtSamling setAction(String action) {
        this.action = action;
        return this;
    }

    public String getAction() {
        return action;
    }
}
