package no.nav.vedtak.sikkerhet.kontekst;

/**
 * SPI for å tilby kontekst som er satt i aktuell tråd.
 * Må implementeres i moduler som ønsker en injectable i stedet for å bruke KontekstHolder direkte
 */
public interface KontekstProvider {

    default Kontekst getKontekst() {
        return KontekstHolder.getKontekst();
    }

}
