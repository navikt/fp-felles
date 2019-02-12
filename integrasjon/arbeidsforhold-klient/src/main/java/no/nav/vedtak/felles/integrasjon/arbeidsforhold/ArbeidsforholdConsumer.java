package no.nav.vedtak.felles.integrasjon.arbeidsforhold;

import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.FinnArbeidsforholdPrArbeidstakerUgyldigInput;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.binding.HentArbeidsforholdHistorikkSikkerhetsbegrensning;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.FinnArbeidsforholdPrArbeidstakerResponse;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkRequest;
import no.nav.tjeneste.virksomhet.arbeidsforhold.v3.meldinger.HentArbeidsforholdHistorikkResponse;

public interface ArbeidsforholdConsumer {

    FinnArbeidsforholdPrArbeidstakerResponse finnArbeidsforholdPrArbeidstaker(FinnArbeidsforholdPrArbeidstakerRequest request) // NOSONAR
            throws FinnArbeidsforholdPrArbeidstakerSikkerhetsbegrensning, FinnArbeidsforholdPrArbeidstakerUgyldigInput;

    HentArbeidsforholdHistorikkResponse hentArbeidsforholdHistorikk(HentArbeidsforholdHistorikkRequest request)
            throws HentArbeidsforholdHistorikkArbeidsforholdIkkeFunnet, HentArbeidsforholdHistorikkSikkerhetsbegrensning;
}
