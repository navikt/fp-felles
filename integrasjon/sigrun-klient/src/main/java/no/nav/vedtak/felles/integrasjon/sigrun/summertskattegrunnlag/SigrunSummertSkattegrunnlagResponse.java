package no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag;

import java.time.Year;
import java.util.Map;
import java.util.Optional;

public class SigrunSummertSkattegrunnlagResponse {

    Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap;

    public SigrunSummertSkattegrunnlagResponse(Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap) {
        this.summertskattegrunnlagMap = summertskattegrunnlagMap;
    }

    public Map<Year, Optional<SSGResponse>> getSummertskattegrunnlagMap() {
        return summertskattegrunnlagMap;
    }
}
