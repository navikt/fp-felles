package no.nav.vedtak.felles.integrasjon.person;

import java.io.Serializable;

public record PDLExceptionExtension(String code, PDLExceptionDetails details) implements Serializable {

}
