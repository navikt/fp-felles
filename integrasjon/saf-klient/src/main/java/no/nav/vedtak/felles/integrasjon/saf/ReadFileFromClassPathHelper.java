package no.nav.vedtak.felles.integrasjon.saf;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.apache.commons.io.IOUtils;

final class ReadFileFromClassPathHelper {
    private ReadFileFromClassPathHelper() {
    }

    @SuppressWarnings("resource")
    static String hent(String filsti) {
        Objects.requireNonNull(filsti, "Må oppgi en filsti det skal hentes fra");

        InputStream inputStream = ReadFileFromClassPathHelper.class.getClassLoader().getResourceAsStream(filsti);
        if (inputStream == null) {
            throw new IllegalArgumentException("Finner ikke fil på classpath '" + filsti + "'.");
        }
        try {
            return IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
        } catch (Exception e) {
            throw new IllegalArgumentException("Feil ved lesing av fil '" + filsti + "'", e);
        }
    }
}
