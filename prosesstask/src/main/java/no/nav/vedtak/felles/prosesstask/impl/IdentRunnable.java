package no.nav.vedtak.felles.prosesstask.impl;

import java.time.LocalDateTime;

/** Runnable med angitt id. */
interface IdentRunnable extends Runnable {
    Long getId();
    
    /** Tid task ble plukket til in-memory kø. */
    LocalDateTime getCreateTime();
}