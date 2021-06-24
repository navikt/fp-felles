package no.nav.vedtak.felles.integrasjon.pdl;

public record PDLExceptionExtension(String code, PDLExceptionDetails details) {

    @Deprecated(since = "2.3", forRemoval = true)
    public PDLExceptionDetails getDetails() {
        return details;
    }

    @Deprecated(since = "2.3", forRemoval = true)
    public String getCode() {
        return code();
    }

}
