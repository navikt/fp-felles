package no.nav.vedtak.felles.jpa;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.TypedQuery;

import no.nav.vedtak.feil.FeilFactory;

/**
 * Enkle hjelpemetoder for å hente ut hibernate resultater på litt andre måter enn hibernate API gir som default.
 * Eksempel: som Optional, kast exception dersom intet resultat, og lignende.
 */
public class HibernateVerktøy {

    private HibernateVerktøy() {
    }

    public static <T> Optional<T> hentUniktResultat(TypedQuery<T> query) {
        List<T> resultatListe = query.getResultList();
        if (resultatListe.size() > 1) {
            throw FeilFactory.create(HibernateFeil.class).ikkeUniktResultat(formatter(query)).toException();
        }
        return resultatListe.size() == 1 ? Optional.of(resultatListe.get(0)) : Optional.empty();
    }

    public static <T> T hentEksaktResultat(TypedQuery<T> query) {
        List<T> resultatListe = query.getResultList();
        if (resultatListe.size() > 1) {
            throw FeilFactory.create(HibernateFeil.class).merEnnEttResultat(formatter(query)).toException();
        }
        if (resultatListe.isEmpty()) {
            throw FeilFactory.create(HibernateFeil.class).tomtResultat(formatter(query)).toException();
        }
        return resultatListe.get(0);
    }

    private static <T> String formatter(TypedQuery<T> query) {
        org.hibernate.query.Query<T> hibQuery = (org.hibernate.query.Query<T>) query;
        String queryString = hibQuery.getQueryString();
        Set<String> namedParameterNames = hibQuery.getParameterMetadata().getNamedParameterNames();
        String parameterString = namedParameterNames.stream()
                .map(navn -> navn + "=" + hibQuery.getParameterValue(navn))
                .collect(Collectors.joining(", "));

        return "'" + queryString + " {" + parameterString + "}";
    }

}
