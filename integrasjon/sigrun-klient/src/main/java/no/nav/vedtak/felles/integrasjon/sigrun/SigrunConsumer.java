package no.nav.vedtak.felles.integrasjon.sigrun;

import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;

public interface SigrunConsumer {

 SigrunResponse beregnetskatt(Long aktørId);

 SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId);

}
