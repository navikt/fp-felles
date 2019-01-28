package no.nav.vedtak.felles.testutilities.db;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Bruk denne ved utvikling av enhetstester når du har behov for å se testdata i databasen.
 * <p>
 * Bruk av denne skal ikke pushes til kode-repo.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface Commit {
}
