package no.nav.vedtak.felles.integrasjon.jms.precond;

/**
 * Sjekker om forutsetningene er tilstede for å behandle meldinger,
 * slik at man kan la være å lese dem når når man allerede vet at de ikke kan håndters,
 * og dermed ikke bruke opp redelivery-forsøk.</p>
 * <p>
 * Implementasjoner vil typisk sjekke om databasen og/eller andre eksterne avhengigheter er oppe.
 */
public interface PreconditionChecker {

    PreconditionCheckerResult check();
}
