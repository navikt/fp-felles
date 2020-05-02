package no.nav.vedtak.log.audit;

public final class CefField {
    
    private CefFieldName key;
    private String value;
 
    
    public CefField(CefFieldName key, String value) {
        this.key = key;
        this.value = value;
    }
    
    public CefField(CefFieldName key, long value) {
        this.key = key;
        this.value = Long.toString(value);
    }
    
    
    private static final String cefValueEscape(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("=", "\\=");
    }
    
    public CefFieldName getKey() {
        return key;
    }
    
    public String getValue() {
        return value;
    }
    
    /**
     * Nøkkel og verdi i "Commen Event Format (CEF)".
     */
    public String toString() {
        if (value == null) {
            return "";
        }
        
        return key.getKode() + "=" + cefValueEscape(value);
    }
}
