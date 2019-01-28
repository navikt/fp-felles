package no.nav.vedtak.felles.integrasjon.sigrun;

import java.time.Year;
import java.util.List;
import java.util.Map;

public class SigrunResponse {

    Map<Year, List<BeregnetSkatt>> beregnetSkatt;

    public SigrunResponse(Map<Year, List<BeregnetSkatt>> beregnetSkatt) {
        this.beregnetSkatt = beregnetSkatt;
    }

    public Map<Year, List<BeregnetSkatt>> getBeregnetSkatt() {
        return beregnetSkatt;
    }
}