package no.nav.vedtak.felles.integrasjon.spokelse;

import java.time.LocalDateTime;
import java.util.List;

public record SykepengeVedtak(String vedtaksreferanse,
                             List<SykepengeUtbetaling> utbetalinger,
                             LocalDateTime vedtattTidspunkt) {

    public List<SykepengeUtbetaling> utbetalingerNonNull() {
        return utbetalinger != null ? utbetalinger : List.of();
    }
}
