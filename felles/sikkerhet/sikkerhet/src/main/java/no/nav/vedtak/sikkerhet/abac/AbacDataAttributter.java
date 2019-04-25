package no.nav.vedtak.sikkerhet.abac;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class AbacDataAttributter {

    private Map<AbacAttributtType, Set<Object>> attributter = new EnumMap<>(AbacAttributtType.class);

    public static AbacDataAttributter opprett() {
        return new AbacDataAttributter();
    }

    public AbacDataAttributter leggTil(AbacDataAttributter annen) {
        for (Map.Entry<AbacAttributtType, Set<Object>> entry : annen.attributter.entrySet()) {
            if (entry.getValue() != null) {
                leggTil(entry.getKey(), entry.getValue());
            }
        }
        return this;
    }

    public AbacDataAttributter leggTilOppgavestyringEnhet(String enhet) {
        return leggTil(AbacAttributtType.OPPGAVESTYRING_ENHET, enhet);
    }

    Set<String> getOppgavestyringEnhet() {
        return getVerdier(AbacAttributtType.OPPGAVESTYRING_ENHET);
    }

    public AbacDataAttributter leggTilFødselsnummer(String fnr) {
        return leggTil(AbacAttributtType.FNR, fnr);
    }

    Set<String> getFødselsnumre() {
        return getVerdier(AbacAttributtType.FNR);
    }

    public AbacDataAttributter leggTilAktørId(String aktørId) {
        return leggTil(AbacAttributtType.AKTØR_ID, aktørId);
    }

    Set<String> getAktørIder() {
        return getVerdier(AbacAttributtType.AKTØR_ID);
    }

    public AbacDataAttributter leggTilSaksnummer(String saksnummer) {
        return leggTil(AbacAttributtType.SAKSNUMMER, saksnummer);
    }

    Set<String> getSaksnummre() {
        return getVerdier(AbacAttributtType.SAKSNUMMER);
    }

    public AbacDataAttributter leggTilBehandlingsId(Long behandlingsId) {
        return leggTil(AbacAttributtType.BEHANDLING_ID, behandlingsId);
    }
    
    public AbacDataAttributter leggTilBehandlingsUUID(UUID behandlingsUuid) {
        return leggTil(AbacAttributtType.BEHANDLING_UUID, behandlingsUuid.toString());
    }
    
    public AbacDataAttributter leggTilBehandlingsUUID(String behandlingsUuid) {
        return leggTil(AbacAttributtType.BEHANDLING_UUID, behandlingsUuid);
    }

    Set<Long> getBehandlingIder() {
        return getVerdier(AbacAttributtType.BEHANDLING_ID);
    }
    
    Set<String> getBehandlingsUUIDer() {
        return getVerdier(AbacAttributtType.BEHANDLING_UUID);
    }

    public AbacDataAttributter leggTilFagsakId(Long fagsakId) {
        return leggTil(AbacAttributtType.FAGSAK_ID, fagsakId);
    }

    Set<Long> getFagsakIder() {
        return getVerdier(AbacAttributtType.FAGSAK_ID);
    }

    public AbacDataAttributter leggTilDokumentDataId(Long dokumentDataId) {
        return leggTil(AbacAttributtType.DOKUMENT_DATA_ID, dokumentDataId);
    }

    Set<Long> getDokumentDataId() {
        return getVerdier(AbacAttributtType.DOKUMENT_DATA_ID);
    }

    public AbacDataAttributter leggTilDokumentId(String dokumentId) {
        return leggTil(AbacAttributtType.DOKUMENT_ID, dokumentId);
    }

    Set<String> getDokumentId() {
        return getVerdier(AbacAttributtType.DOKUMENT_ID);
    }

    public AbacDataAttributter leggTilJournalPostId(String journalpostId, boolean krevAtFinnes) {
        return leggTil(krevAtFinnes ? AbacAttributtType.EKSISTERENDE_JOURNALPOST_ID : AbacAttributtType.JOURNALPOST_ID, journalpostId);
    }

    Set<String> getJournalpostIder(boolean påkrevde) {
        return getVerdier(påkrevde ? AbacAttributtType.EKSISTERENDE_JOURNALPOST_ID : AbacAttributtType.JOURNALPOST_ID);
    }

    public AbacDataAttributter leggTilFnrForSøkeEtterSaker(String fnr) {
        return leggTil(AbacAttributtType.SAKER_MED_FNR, fnr);
    }

    Set<String> getFnrForSøkEtterSaker() {
        return getVerdier(AbacAttributtType.SAKER_MED_FNR);
    }

    public AbacDataAttributter leggTilOppgaveId(String oppgaveId) {
        return leggTil(AbacAttributtType.OPPGAVE_ID, oppgaveId);
    }

    Set<String> getOppgaveIder() {
        return getVerdier(AbacAttributtType.OPPGAVE_ID);
    }

    public AbacDataAttributter leggTilAksjonspunktKode(String aksjonspunktId) {
        return leggTil(AbacAttributtType.AKSJONSPUNKT_KODE, aksjonspunktId);
    }

    Set<String> getAksjonspunktKode() {
        return getVerdier(AbacAttributtType.AKSJONSPUNKT_KODE);
    }

    public AbacDataAttributter leggTilSPBeregningId(Long beregningId) {
        return leggTil(AbacAttributtType.SPBEREGNING_ID, beregningId);
    }

    Set<Long> getSPBeregningsIder() {
        return getVerdier(AbacAttributtType.SPBEREGNING_ID);
    }

    public AbacDataAttributter leggTilDokumentforsendelseId(UUID forsendelseId) {
        return leggTil(AbacAttributtType.DOKUMENTFORSENDELSE_ID, forsendelseId);
    }

    Set<UUID> getDokumentforsendelseIder() {
        return getVerdier(AbacAttributtType.DOKUMENTFORSENDELSE_ID);
    }

    private AbacDataAttributter leggTil(AbacAttributtType type, Collection<Object> samling) {
        Set<Object> a = attributter.get(type);
        if (a == null) {
            attributter.put(type, new HashSet<>(samling));
        } else {
            a.addAll(samling);
        }
        return this;
    }

    private AbacDataAttributter leggTil(AbacAttributtType type, Object verdi) {
        requireNonNull(verdi, "Attributt av type " + type + " kan ikke være null"); //$NON-NLS-1$ //$NON-NLS-2$
        Set<Object> a = attributter.get(type);
        if (a == null) {
            a = new HashSet<>(4); // det er vanligvis bare 1 attributt i settet
            attributter.put(type, a);
        }
        a.add(verdi);
        return this;
    }

    @SuppressWarnings("unchecked")
    private <T> Set<T> getVerdier(AbacAttributtType type) {
        return attributter.containsKey(type)
            ? (Set<T>) attributter.get(type) // NOSONAR cast fungerer når settere/gettere er symmetriske slik de skal være her
            : Collections.emptySet();
    }

    @Override
    public String toString() {
        return AbacDataAttributter.class.getSimpleName() +
            "{saksnummre='" + getSaksnummre() + '\'' +
            ", fnr='" + maskertEllerTom(getFødselsnumre()) + "'" +
            ", aktørId='" + maskertEllerTom(getAktørIder()) + "'" +
            ", oppgaveId='" + getOppgaveIder() + "'" +
            ", fagsakId='" + getFagsakIder() + "'" +
            ", behandlingId='" + getBehandlingIder() + "'" +
            ", aksjonspunktKoder='" + getAksjonspunktKode() + "'" +
            ", journalpostId='" + getJournalpostIder(false) + "'" +
            ", påkrevdeJournalpostId='" + getJournalpostIder(true) + "'" +
            ", beregningId='" + getSPBeregningsIder() + "'" +
            ", dokumentforsendelseId='" + getDokumentforsendelseIder() + "'" +
            ", oppgavestyringEnhet='" + getOppgavestyringEnhet() + "'" +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof AbacDataAttributter)) {
            return false;
        }
        AbacDataAttributter annen = (AbacDataAttributter) o;
        return Objects.equals(attributter, annen.attributter);
    }

    @Override
    public int hashCode() {
        return Objects.hash(attributter);
    }

    private static String maskertEllerTom(Collection<?> input) {
        return input.isEmpty() ? "[]" : "[MASKERT#" + input.size() + "]";
    }
}
