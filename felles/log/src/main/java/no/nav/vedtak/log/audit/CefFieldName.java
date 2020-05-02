package no.nav.vedtak.log.audit;

/**
 * Felter skal ikke brukes til andre verdier enn det som er avtalt med ArcSight-gjengen.
 * 
 * Vi ønsker å bruke de samme feltnavnene på tvers av K9- og FP-områdene. Derfor er disse
 * nøklene definert ett felles sted. Det er et mål for ArcSight at det blir likt i hele NAV.
 */
public enum CefFieldName {

    /**
     * Tidspunkt for når hendelsen skjedde.
     */
    EVENT_TIME("end"),
    
    /**
     * Brukeren som startet hendelsen (saksbehandler/veileder/...).
     */
    USER_ID("suid"),
    
    /**
     * Bruker (søker/part/...) som har personopplysninger som blir berørt.
     */
    BERORT_BRUKER_ID("duid"),
    
    /**
     * Det som blir bedt om: OK med både URLer og hendelsesnavn.
     */
    REQUEST("request"),
    
    /**
     * Brukes til ABAC-ressurstype.
     */
    ABAC_RESOURCE_TYPE("requestContext"),
    
    /**
     * Ekstra felt for ABAC-action (fordi det samme blir definert gjennom EventClassId).
     */
    ABAC_ACTION("act"),
    
    /**
     * Reservert til bruk for "Saksnummer". Det er godkjent med både eksternt saksnummer
     * og FagsakId, men førstnevnte er foretrukket. Denne sakl unikt identifisere fagsaken.
     * 
     * @see CefFields#forSaksnummer(String)
     */
    SAKSNUMMER_VERDI("flexString1"),
    
    /**
     * Reservert til bruk for "Saksnummer".
     * 
     * @see CefFields#forSaksnummer(String)
     */
    SAKSNUMMER_LABEL("flexString1Label"),
    
    /**
     * Reservert til bruk for "Behandling". Det er godkjent med både behandlingsUuid
     * og behandlingsId, men førstnevnte er foretrukket. Denne sakl unikt identifisere
     * behandlingen.
     * 
     * @see CefFields#forBehandling(String)
     */
    BEHANDLING_VERDI("flexString2"),
    
    /**
     * Reservert til bruk for "Behandling".
     * 
     * @see CefFields#forBehandling(String)
     */
    BEHANDLING_LABEL("flexString2Label");

    
    private final String kode;
    
    
    private CefFieldName(String kode) {
        this.kode = kode;
    }
    
    public String getKode() {
        return kode;
    }
}
