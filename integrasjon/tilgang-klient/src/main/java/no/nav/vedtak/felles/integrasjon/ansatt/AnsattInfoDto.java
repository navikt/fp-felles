package no.nav.vedtak.felles.integrasjon.ansatt;

import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class AnsattInfoDto {

    private AnsattInfoDto() {
    }

    // Finne ansattinfo basert på ansatt-ident
    public record IdentRequest(@NotNull @Pattern(regexp = "^[a-zA-Z]\\d{6}$") String ansattIdent) { }

    // Hente ansattinfo basert på Entra OID for ansatt
    public record OidRequest(@NotNull @Valid UUID ansattOid) { }

    // Ansattinfo
    public record Respons(@NotNull UUID ansattOid, @NotNull String ansattIdent, @NotNull String navn, String ansattVedEnhetId) {}

}
