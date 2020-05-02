package no.nav.vedtak.log.audit;

import java.util.Objects;

public final class CefField {
    
    private String key;
    private String value;
 
    
    public CefField(String key, String value) {
        this.key = cefKeyWhitelist(key);
        this.value = value;
    }
    
    public CefField(String key, long value) {
        this.key = cefKeyWhitelist(key);
        this.value = Long.toString(value);
    }
    
    
    private static final String cefKeyWhitelist(String s) {
        Objects.requireNonNull(s);
        if (s.matches("[^a-zA-Z0-9]")) {
            throw new IllegalArgumentException("Ugyldig CEF-nøkkeltegn");
        }
        return s;
    }
    
    private static final String cefValueEscape(String s) {
        return s.replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r").replace("=", "\\=");
    }
    
    public String getKey() {
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
        
        return key + "=" + cefValueEscape(value);
    }
}
