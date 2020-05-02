package no.nav.vedtak.log.audit;

public enum EventClassId {

    /** Bruker har sett data. */
    AUDIT_ACCESS("audit:access"),
    
    /** Minimalt innsyn, f.eks. ved visning i liste. */
    AUDIT_SEARCH("audit:search"),
    
    /** Bruker har lagt inn nye data */
    AUDIT_CREATE("audit:create"),
    
    /** Bruker har endret data */
    AUDIT_UPDATE("audit:update");
    
    private final String cefKode;

    private EventClassId(String cefKode) {
        this.cefKode = cefKode;
    }
    
    public String getCefKode() {
        return cefKode;
    }
    
}
