package no.nav.vedtak.felles.integrasjon.sigrun;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import no.nav.vedtak.feil.Feil;
import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.feil.LogLevel;
import no.nav.vedtak.feil.deklarasjon.DeklarerteFeil;
import no.nav.vedtak.feil.deklarasjon.TekniskFeil;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SSGResponse;
import no.nav.vedtak.felles.integrasjon.sigrun.summertskattegrunnlag.SigrunSummertSkattegrunnlagResponse;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.util.FPDateUtil;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.time.Year;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;


@ApplicationScoped
public class SigrunConsumerImpl implements SigrunConsumer {

    private static final ObjectMapper mapper = getObjectMapper();
    private static final String TEKNISK_NAVN = "skatteoppgjoersdato";
    private SigrunRestClient sigrunRestClient;


    SigrunConsumerImpl() {
        //CDI
    }

    @Inject
    public SigrunConsumerImpl(SigrunRestClient sigrunRestClient, @KonfigVerdi("SigrunRestBeregnetSkatt.url") URI endpoint) {
        this.sigrunRestClient = sigrunRestClient;
        this.sigrunRestClient.setEndpoint(endpoint);
    }

    private static ObjectMapper getObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper;
    }

    private static <T> List<T> fromJsonList(String json, TypeReference<List<T>> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw JsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }

    private static <T> T fromJson(String json, TypeReference<T> typeReference) {
        try {
            return mapper.readValue(json, typeReference);
        } catch (IOException e) {
            throw JsonMapperFeil.FACTORY.ioExceptionVedLesing(e).toException();
        }
    }

    @Override
    public SigrunResponse beregnetskatt(Long aktørId) {
        Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt = new HashMap<>();
        ferdiglignedeBeregnetSkattÅr(aktørId)
            .stream()
            .collect(Collectors.toMap(år -> år, år -> {
                String resultat = sigrunRestClient.hentBeregnetSkattForAktørOgÅr(aktørId, år.toString());
                return resultat != null ? resultat : "";
            }))
            .forEach((resulatÅr, skatt) -> leggTilBS(årTilListeMedSkatt, resulatÅr, skatt));

        return new SigrunResponse(årTilListeMedSkatt);
    }

    @Override
    public SigrunSummertSkattegrunnlagResponse summertSkattegrunnlag(Long aktørId) {
        Map<Year, Optional<SSGResponse>> summertskattegrunnlagMap = hentÅrsListeForSummertskattegrunnlag()
            .stream()
            .collect(Collectors.toMap(år -> år, år -> {
                String resultat = sigrunRestClient.hentSummertskattegrunnlag(aktørId, år.toString());
                if (resultat == null) {
                    return Optional.empty();
                }
                return Optional.of(fromJson(resultat, new TypeReference<>() {
                }));
            }));
        return new SigrunSummertSkattegrunnlagResponse(summertskattegrunnlagMap);
    }

    private void leggTilBS(Map<Year, List<BeregnetSkatt>> årTilListeMedSkatt, Year år, String skatt) {
        årTilListeMedSkatt.put(år, skatt.isEmpty()
            ? Collections.emptyList()
            : fromJsonList(skatt, new TypeReference<>() {
        }));
    }

    private List<Year> ferdiglignedeBeregnetSkattÅr(Long aktørId) {
        Year iFjor = Year.now(FPDateUtil.getOffset()).minusYears(1L);
        if (iFjorErFerdiglignetBeregnet(aktørId, iFjor)) {
            return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
        } else {
            Year iForifjor = iFjor.minusYears(1L);
            return asList(iForifjor, iForifjor.minusYears(1L), iForifjor.minusYears(2L));
        }
    }

    private List<Year> hentÅrsListeForSummertskattegrunnlag() {
        Year iFjor = Year.now(FPDateUtil.getOffset()).minusYears(1L);
        //filteret(SummertSkattegrunnlagForeldrepenger) i Sigrun er ikke impl. tidligere enn 2018
        if (iFjor.equals(Year.of(2018))) {
            return List.of(iFjor);
        } else if (iFjor.equals(Year.of(2019))) {
            return List.of(iFjor, iFjor.minusYears(1L));
        }
        return asList(iFjor, iFjor.minusYears(1L), iFjor.minusYears(2L));
    }

    private boolean iFjorErFerdiglignetBeregnet(Long aktørId, Year iFjor) {
        String json = sigrunRestClient.hentBeregnetSkattForAktørOgÅr(aktørId, iFjor.toString());
        List<BeregnetSkatt> beregnetSkatt = json != null
            ? fromJsonList(json, new TypeReference<>() {
        })
            : new ArrayList<>();
        return beregnetSkatt.stream()
            .anyMatch(l -> l.getTekniskNavn().equals(TEKNISK_NAVN));
    }

    interface JsonMapperFeil extends DeklarerteFeil {

        JsonMapperFeil FACTORY = FeilFactory.create(JsonMapperFeil.class);

        @TekniskFeil(feilkode = "F-918328", feilmelding = "Fikk IO exception ved parsing av JSON", logLevel = LogLevel.WARN)
        Feil ioExceptionVedLesing(IOException cause);
    }
}
