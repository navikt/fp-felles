package no.nav.vedtak.sikkerhet.abac;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public class PdpRequest {

    private List<String> fnr;
    private List<String> aktørId;
    private List<String> saksnummer;
    private AbacIdToken token;
    private String ansvarligSaksbehandler;
    private List<String> oppgavestyringEnhet;

    private String annenPartAktørId;
    private Boolean opgittAleneomsorg;
    
    private String xacmlAction;
    private String xacmlResourceType;
    private String xacmlSakstatus;
    private String xacmlBehandlingStatus;
    private List<String> xacmlAksjonspunktType;

    public List<String> getFnr() {
        return fnr;
    }

    public void setFnr(Set<String> fnr) {
        this.fnr = new ArrayList<>(fnr);
    }

    public void setToken(AbacIdToken token) {
        this.token = token;
    }

    public AbacIdToken getToken() {
        return token;
    }

    public Optional<String> getAnsvarligSaksbehandler() {
        return Optional.ofNullable(ansvarligSaksbehandler);
    }

    public void setAnsvarligSaksbehandler(String ansvarligSaksbehandler) {
        this.ansvarligSaksbehandler = ansvarligSaksbehandler;
    }

    public String getXacmlAction() {
        return xacmlAction;
    }

    public String getXacmlResourceType() {
        return xacmlResourceType;
    }

    public Optional<String> getXacmlSakstatus() {
        return Optional.ofNullable(xacmlSakstatus);
    }

    public void setSakstatus(AbacFagsakStatus sakstatus) {
        xacmlSakstatus = sakstatus.getEksternKode();
    }

    public Optional<String> getXacmlBehandlingStatus() {
        return Optional.ofNullable(xacmlBehandlingStatus);
    }

    public void setBehandlingStatus(AbacBehandlingStatus behandlingStatus) {
        xacmlBehandlingStatus = behandlingStatus.getEksternKode();
    }

    public List<String> getXacmlAksjonspunktType() {
        return xacmlAksjonspunktType;
    }

    public void setAksjonspunktType(Collection<String> aksjonspunktType) {
        xacmlAksjonspunktType = new ArrayList<>(aksjonspunktType);
    }

    public void setAction(BeskyttetRessursActionAttributt action) {
        this.xacmlAction = action.getEksternKode();
    }

    public void setResource(BeskyttetRessursResourceAttributt resource) {
        this.xacmlResourceType = resource.getEksternKode();
    }

    public int antallResources() {
        return Math.max(1, antallIdenter()) * Math.max(1, antallAksjonspunktTyper());
    }

    public Optional<String> getFnrForIndex(int index) {
        int antallFnr = antallFnr();
        return antallFnr == 0
                ? Optional.empty()
                : Optional.of(fnr.get(index % antallFnr));
    }

    public Optional<String> getAksjonspunktTypeForIndex(int index) {
        return antallAksjonspunktTyper() == 0
                ? Optional.empty()
                : Optional.of(xacmlAksjonspunktType.get(index / Math.max(antallIdenter(), 1)));
    }

    private int antallIdenter() {
        return antallFnr() + antallAktørId();
    }

    public int getAntallFnr() {
        return antallFnr();
    }

    private int antallAksjonspunktTyper() {
        return xacmlAksjonspunktType != null ? xacmlAksjonspunktType.size() : 0;
    }

    private int antallFnr() {
        return fnr != null ? fnr.size() : 0;
    }

    public List<String> getAktørId() {
        return aktørId;
    }

    public PdpRequest setAktørId(Set<String> aktørId) {
        this.aktørId = new ArrayList<>(aktørId);
        return this;
    }

    public Optional<String> getAktørIdForIndex(int index) {
        int antallAktørId = antallAktørId();
        return antallAktørId == 0
                ? Optional.empty()
                : Optional.of(aktørId.get(index % antallAktørId));
    }

    public int antallAktørId() {
        return aktørId != null ? aktørId.size() : 0;
    }

    public List<String> getOppgavestyringEnhet() {
        return oppgavestyringEnhet;
    }

    public Optional<String> getOppgavestyringEnhetForIndex(int index) {
        int antallEnheter = antallOppgavestyringEnhet();
        return antallEnheter == 0
                ? Optional.empty()
                : Optional.of(oppgavestyringEnhet.get(index % antallEnheter));
    }

    private int antallOppgavestyringEnhet() {
        return oppgavestyringEnhet != null ? oppgavestyringEnhet.size() : 0;
    }

    public void setOppgavestyringEnhet(List<String> avdelingEnhet) {
        this.oppgavestyringEnhet = new ArrayList<>(avdelingEnhet);
    }
    
    public void setSaksummer(List<String> saksnumer){
        this.saksnummer = saksnumer;
    }

    public List<String> getSaksnummer(){ return saksnummer; }

    public Optional<String> getAnnenPartAktørId() {
        return Optional.ofNullable(annenPartAktørId);
    }

    public void setAnnenPartAktørId(String annenPartAktørId) {
        this.annenPartAktørId = annenPartAktørId;
    }

    public Optional<Boolean> getOpgittAleneomsorg() {
        return Optional.ofNullable(opgittAleneomsorg);
    }

    public void setOpgittAleneomsorg(Boolean opgittAleneomsorg) {
        this.opgittAleneomsorg = opgittAleneomsorg;
    }

    private int antallSaksnummer(){
        return saksnummer != null ? saksnummer.size() : 0;
    }

    public Optional<String> getSaksummerForIndex(int index) {
        int antallSaksummer = antallSaksnummer();
        return antallSaksummer == 0
                ? Optional.empty()
                : Optional.of(saksnummer.get(index));
    }
}
