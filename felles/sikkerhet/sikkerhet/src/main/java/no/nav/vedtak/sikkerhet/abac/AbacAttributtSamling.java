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

    /**
     * @deprecated bye-bye bruk verdier direkte, ikke hardkod metoder
     */
    @Deprecated
    public Set<String> getSaksnummre() {
        return dataAttributter.getSaksnummre();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<Long> getBehandlingsIder() {
        return dataAttributter.getBehandlingIder();
    }
    
    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getBehandlingsUUIDer() {
        return dataAttributter.getBehandlingsUUIDer();
    }
    
    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<Long> getSPBeregningsIder() {
        return dataAttributter.getSPBeregningsIder();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getFødselsnumre() {
        return dataAttributter.getFødselsnumre();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getFnrForSøkEtterSaker() {
        return dataAttributter.getFnrForSøkEtterSaker();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getJournalpostIder(boolean påkrevde) {
        return dataAttributter.getJournalpostIder(påkrevde);
    }
    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getOppgaveIder() {
        return dataAttributter.getOppgaveIder();
    }
    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getOppgavestyringEnhet(){
        return dataAttributter.getOppgavestyringEnhet();
    }
    
    public Set<String> getVerdier(AbacAttributtType type){
        return dataAttributter.getVerdier(type);
    }
    
    public Set<AbacAttributtType> keySet(){
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

    public AbacAttributtSamling setResource(BeskyttetRessursResourceAttributt resource) {
        this.resource = resource;
        return this;
    }

    public BeskyttetRessursResourceAttributt getResource() {
        return resource;
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getAksjonspunktKode() {
        return dataAttributter.getAksjonspunktKode();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getAktørIder() {
        return dataAttributter.getAktørIder();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<Long> getDokumentDataIDer() {
        return dataAttributter.getDokumentDataId();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<String> getDokumentIDer() {
        return dataAttributter.getDokumentId();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public Set<Long> getFagsakIder() {
        return dataAttributter.getFagsakIder();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
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
