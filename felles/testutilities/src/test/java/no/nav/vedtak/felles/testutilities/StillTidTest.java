package no.nav.vedtak.felles.testutilities;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;

import org.junit.Rule;
import org.junit.Test;

import no.nav.vedtak.util.FPDateUtil;

public class StillTidTest {

    private int year = 2011;
    private int month = 3;
    private int dayOfMonth = 27;
    private int hour = 10;
    private int minute = 30;

    @Rule
    public final StillTid stillTid = new StillTid().medTid(LocalDateTime.of(year, month, dayOfMonth, hour, minute));
    
    @Test
    public void testName() {
        LocalDateTime tid = FPDateUtil.nå();
        LocalDateTime comp = LocalDateTime.of(year, month, dayOfMonth, hour, minute);

        assertThat(tid).isEqualToIgnoringHours(comp);
    }
}
