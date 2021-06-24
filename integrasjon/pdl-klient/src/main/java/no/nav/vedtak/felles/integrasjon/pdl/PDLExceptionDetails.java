package no.nav.vedtak.felles.integrasjon.pdl;

public record PDLExceptionDetails(String type, String cause,String policy) {
     
    @Deprecated(since = "2.3", forRemoval = true)
    public String getType() {
        return type();
    }

    @Deprecated(since = "2.3", forRemoval = true)
    public String getCause() {
        return cause();
    }

    @Deprecated(since = "2.3", forRemoval = true)
    public String getPolicy() {
        return policy();
    }

    
}
