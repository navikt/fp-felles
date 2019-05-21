package no.nav.vedtak.sikkerhet.abac;

import static java.util.Objects.requireNonNull;

import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.Set;
import java.util.TreeMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class AbacDataAttributter {

    private NavigableMap<AbacAttributtType, Set<Object>> attributter = new TreeMap<>(Comparator.comparing(AbacAttributtType::getSporingsloggEksternKode));

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

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilOppgavestyringEnhet(String enhet) {
        return leggTil(StandardAbacAttributtType.OPPGAVESTYRING_ENHET, enhet);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getOppgavestyringEnhet() {
        return getVerdier(StandardAbacAttributtType.OPPGAVESTYRING_ENHET);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilFødselsnummer(String fnr) {
        return leggTil(StandardAbacAttributtType.FNR, fnr);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getFødselsnumre() {
        return getVerdier(StandardAbacAttributtType.FNR);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilAktørId(String aktørId) {
        return leggTil(StandardAbacAttributtType.AKTØR_ID, aktørId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getAktørIder() {
        return getVerdier(StandardAbacAttributtType.AKTØR_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilSaksnummer(String saksnummer) {
        return leggTil(StandardAbacAttributtType.SAKSNUMMER, saksnummer);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getSaksnummre() {
        return getVerdier(StandardAbacAttributtType.SAKSNUMMER);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated

    public AbacDataAttributter leggTilBehandlingsId(Long behandlingsId) {
        return leggTil(StandardAbacAttributtType.BEHANDLING_ID, behandlingsId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated

    public AbacDataAttributter leggTilBehandlingsUUID(UUID behandlingsUuid) {
        return leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingsUuid.toString());
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilBehandlingsUUID(String behandlingsUuid) {
        return leggTil(StandardAbacAttributtType.BEHANDLING_UUID, behandlingsUuid);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<Long> getBehandlingIder() {
        return getVerdier(StandardAbacAttributtType.BEHANDLING_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getBehandlingsUUIDer() {
        return getVerdier(StandardAbacAttributtType.BEHANDLING_UUID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilFagsakId(Long fagsakId) {
        return leggTil(StandardAbacAttributtType.FAGSAK_ID, fagsakId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<Long> getFagsakIder() {
        return getVerdier(StandardAbacAttributtType.FAGSAK_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilDokumentDataId(Long dokumentDataId) {
        return leggTil(StandardAbacAttributtType.DOKUMENT_DATA_ID, dokumentDataId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<Long> getDokumentDataId() {
        return getVerdier(StandardAbacAttributtType.DOKUMENT_DATA_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilDokumentId(String dokumentId) {
        return leggTil(StandardAbacAttributtType.DOKUMENT_ID, dokumentId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getDokumentId() {
        return getVerdier(StandardAbacAttributtType.DOKUMENT_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilJournalPostId(String journalpostId, boolean krevAtFinnes) {
        return leggTil(krevAtFinnes ? StandardAbacAttributtType.EKSISTERENDE_JOURNALPOST_ID : StandardAbacAttributtType.JOURNALPOST_ID, journalpostId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getJournalpostIder(boolean påkrevde) {
        return getVerdier(påkrevde ? StandardAbacAttributtType.EKSISTERENDE_JOURNALPOST_ID : StandardAbacAttributtType.JOURNALPOST_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilFnrForSøkeEtterSaker(String fnr) {
        return leggTil(StandardAbacAttributtType.SAKER_MED_FNR, fnr);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getFnrForSøkEtterSaker() {
        return getVerdier(StandardAbacAttributtType.SAKER_MED_FNR);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilOppgaveId(String oppgaveId) {
        return leggTil(StandardAbacAttributtType.OPPGAVE_ID, oppgaveId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getOppgaveIder() {
        return getVerdier(StandardAbacAttributtType.OPPGAVE_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilAksjonspunktKode(String aksjonspunktId) {
        return leggTil(StandardAbacAttributtType.AKSJONSPUNKT_KODE, aksjonspunktId);
    }

    public Set<AbacAttributtType> keySet() {
        return attributter.keySet();
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<String> getAksjonspunktKode() {
        return getVerdier(StandardAbacAttributtType.AKSJONSPUNKT_KODE);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilSPBeregningId(Long beregningId) {
        return leggTil(StandardAbacAttributtType.SPBEREGNING_ID, beregningId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<Long> getSPBeregningsIder() {
        return getVerdier(StandardAbacAttributtType.SPBEREGNING_ID);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    public AbacDataAttributter leggTilDokumentforsendelseId(UUID forsendelseId) {
        return leggTil(StandardAbacAttributtType.DOKUMENTFORSENDELSE_ID, forsendelseId);
    }

    /**
     * @deprecated bye-bye
     */
    @Deprecated
    Set<UUID> getDokumentforsendelseIder() {
        return getVerdier(StandardAbacAttributtType.DOKUMENTFORSENDELSE_ID);
    }

    public AbacDataAttributter leggTil(AbacAttributtType type, Collection<Object> samling) {
        Set<Object> a = attributter.get(type);
        if (a == null) {
            attributter.put(type, new LinkedHashSet<>(samling));
        } else {
            a.addAll(samling);
        }
        return this;
    }

    public AbacDataAttributter leggTil(AbacAttributtType type, Object verdi) {
        requireNonNull(verdi, "Attributt av type " + type + " kan ikke være null"); //$NON-NLS-1$ //$NON-NLS-2$
        Set<Object> a = attributter.get(type);
        if (a == null) {
            a = new LinkedHashSet<>(4); // det er vanligvis bare 1 attributt i settet
            attributter.put(type, a);
        }
        a.add(verdi);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> Set<T> getVerdier(AbacAttributtType type) {
        return attributter.containsKey(type)
            ? (Set<T>) attributter.get(type) // NOSONAR cast fungerer når settere/gettere er symmetriske slik de skal være her
            : Collections.emptySet();
    }

    @Override
    public String toString() {
        return AbacDataAttributter.class.getSimpleName() + "{" +
            attributter.entrySet().stream()
                .map(e -> e.getKey() + "=" + (e.getKey().getMaskerOutput() ? maskertEllerTom(e.getValue()) : e.getValue()))
                .collect(Collectors.joining(", ", "{", "}"));
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
