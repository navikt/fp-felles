package no.nav.vedtak.sikkerhet.abac;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collection;
import java.util.List;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;
import javax.jws.WebService;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.jboss.weld.interceptor.util.proxy.TargetInstanceProxy;
import org.w3c.dom.Element;

import no.nav.vedtak.feil.FeilFactory;
import no.nav.vedtak.log.sporingslogg.Sporingsdata;
import no.nav.vedtak.sikkerhet.context.SubjectHandler;
import no.nav.vedtak.util.env.Environment;

@BeskyttetRessurs(action = BeskyttetRessursActionAttributt.DUMMY, ressurs = BeskyttetRessursResourceAttributt.DUMMY, resource = "")
@Interceptor
@Priority(Interceptor.Priority.APPLICATION + 11)
@Dependent
public class BeskyttetRessursInterceptor {

    private Pep pep;
    private AbacSporingslogg sporingslogg;
    private AbacAuditlogger abacAuditlogger;
    private Environment env  = Environment.current();

    @Inject
    public BeskyttetRessursInterceptor(Pep pep, AbacSporingslogg sporingslogg, AbacAuditlogger abacAuditlogger) {
        this.pep = pep;
        this.sporingslogg = sporingslogg;
        this.abacAuditlogger = abacAuditlogger;
    }

    @AroundInvoke
    public Object wrapTransaction(final InvocationContext invocationContext) throws Exception {
        AbacAttributtSamling attributter = hentAttributter(invocationContext);
        Tilgangsbeslutning beslutning = pep.vurderTilgang(attributter);

        if (beslutning.fikkTilgang()) {
            return proceed(invocationContext, attributter, beslutning);
        } else {
            return ikkeTilgang(attributter, beslutning);
        }
    }

    private Object proceed(InvocationContext invocationContext, AbacAttributtSamling attributter, Tilgangsbeslutning beslutning) throws Exception {
        Method method = invocationContext.getMethod();
        boolean sporingslogges = method.getAnnotation(BeskyttetRessurs.class).sporingslogg();
        if (sporingslogges) {

            if (abacAuditlogger.isEnabled()) {
                final String uid = SubjectHandler.getSubjectHandler().getUid();
                abacAuditlogger.loggTilgang(uid, beslutning.getPdpRequest(), attributter);
            }

            // bygger sporingsdata før kallet til invocationContext.proceed,
            // da vi heller vil ha evt. exceptions fra sporing før forretningslogikk har kjørt
            List<Sporingsdata> sporingsdata = sporingslogg.byggSporingsdata(beslutning, attributter);
            Object resultat = invocationContext.proceed();
            // logger til slutt, det skal ikke logges dersom operasjonen ikke lot seg utføre
            // i motsatt fall blir sporingsloggen misvisende
            if (!abacAuditlogger.isEnabled()) {
                sporingslogg.logg(sporingsdata);
            }
            return resultat;
        } else {
            return invocationContext.proceed();
        }
    }

    private Object ikkeTilgang(AbacAttributtSamling attributter, Tilgangsbeslutning beslutning) {
        if (abacAuditlogger.isEnabled()) {
            final String uid = SubjectHandler.getSubjectHandler().getUid();
            abacAuditlogger.loggDeny(uid, beslutning.getPdpRequest(), attributter);
        } else {
            sporingslogg.loggDeny(beslutning, attributter);
        }

        switch (beslutning.getBeslutningKode()) {
            case AVSLÅTT_KODE_6:
                throw PepFeil.FACTORY.ikkeTilgangKode6().toException();
            case AVSLÅTT_KODE_7:
                throw PepFeil.FACTORY.ikkeTilgangKode7().toException();
            case AVSLÅTT_EGEN_ANSATT:
                throw PepFeil.FACTORY.ikkeTilgangEgenAnsatt().toException();
            default:
                throw PepFeil.FACTORY.ikkeTilgang().toException();
        }
    }

    private AbacAttributtSamling hentAttributter(InvocationContext invocationContext) {
        Class<?> clazz = getOpprinneligKlasse(invocationContext);
        Method method = invocationContext.getMethod();

        AbacAttributtSamling attributter = clazz.getAnnotation(WebService.class) != null
            ? AbacAttributtSamling.medSamlToken(SamlUtils.hentSamlToken())
            : AbacAttributtSamling.medJwtToken(hentOidcTOken());
        BeskyttetRessurs beskyttetRessurs = method.getAnnotation(BeskyttetRessurs.class);
        attributter.setActionType(beskyttetRessurs.action());

        if(!beskyttetRessurs.property().isEmpty()) {
            var resource = env.getProperty(beskyttetRessurs.property());
            attributter.setResource(resource);
        } else if (!beskyttetRessurs.resource().isEmpty()) {
            attributter.setResource(beskyttetRessurs.resource());
        } else {
            if (beskyttetRessurs.ressurs() == BeskyttetRessursResourceAttributt.DUMMY) {
                throw new IllegalArgumentException("Beskyttet resource() må være satt");
            } else if (beskyttetRessurs.ressurs() != BeskyttetRessursResourceAttributt.DUMMY) {
                // Legacy fallback
                attributter.setResource(beskyttetRessurs.ressurs().getEksternKode());
            }
        }

        attributter.setAction(utledAction(clazz, method));
        Parameter[] parameterDecl = method.getParameters();
        for (int i = 0; i < method.getParameterCount(); i++) {
            Object parameterValue = invocationContext.getParameters()[i];
            TilpassetAbacAttributt tilpassetAnnotering = parameterDecl[i].getAnnotation(TilpassetAbacAttributt.class);
            leggTilAttributterFraParameter(attributter, parameterValue, tilpassetAnnotering);
        }
        return attributter;
    }

    @SuppressWarnings("rawtypes")
    static void leggTilAttributterFraParameter(AbacAttributtSamling attributter, Object parameterValue, TilpassetAbacAttributt tilpassetAnnotering) {
        if (tilpassetAnnotering != null) {
            leggTil(attributter, tilpassetAnnotering, parameterValue);
        } else {
            if (parameterValue instanceof AbacDto) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                attributter.leggTil(((AbacDto) parameterValue).abacAttributter());
            } else if (parameterValue instanceof Collection) { // NOSONAR for å støtte både enkelt-DTO-er og collection av DTO-er
                leggTilAbacDtoSamling(attributter, (Collection) parameterValue);
            }
        }
    }

    private static void leggTilAbacDtoSamling(AbacAttributtSamling attributter, Collection<?> parameterValue) {
        for (Object value : parameterValue) {
            if (value instanceof AbacDto) {
                attributter.leggTil(((AbacDto) value).abacAttributter());
            } else {
                throw BeskyttetRessursFeil.FACTORY.ugyldigInputForventetAbacDto(value != null ? value.getClass().getName() : "null").toException();
            }
        }
    }

    @SuppressWarnings("rawtypes")
    private static Class<?> getOpprinneligKlasse(InvocationContext invocationContext) {
        Object target = invocationContext.getTarget();
        if (target instanceof TargetInstanceProxy) {
            return ((TargetInstanceProxy) target).weld_getTargetClass();
        }
        return target.getClass();
    }

    private static String hentOidcTOken() {
        return SubjectHandler.getSubjectHandler().getInternSsoToken();
    }

    private static String utledAction(Class<?> clazz, Method method) {
        return ActionUthenter.action(clazz, method);
    }

    private static void leggTil(AbacAttributtSamling attributter, TilpassetAbacAttributt tilpassetAnnotering, Object verdi) {
        try {
            AbacDataAttributter dataAttributter = tilpassetAnnotering.supplierClass().getDeclaredConstructor().newInstance().apply(verdi);
            attributter.leggTil(dataAttributter);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException(e);
        } catch (InvocationTargetException e) {
            throw new IllegalStateException(e.getCause());
        }
    }

    
    private static class SamlUtils {

        private static final TransformerFactory transformerFactory;

        static {
            transformerFactory = TransformerFactory.newInstance();
            try {
                transformerFactory.setFeature(javax.xml.XMLConstants.FEATURE_SECURE_PROCESSING, true);
            } catch (TransformerException e) {
                throw new ExceptionInInitializerError(e);
            }
        }

        private SamlUtils() {
            throw new IllegalStateException("Utility class");
        }
        
        private static String hentSamlToken() {
            try {
                return getSamlAssertionAsString(SubjectHandler.getSubjectHandler().getSamlToken());
            } catch (Exception e) {
                throw FeilFactory.create(BeskyttetRessursFeil.class).kunneIkkeGjøreSamlTokenOmTilStreng(e).toException();
            }
        }

        private static String getSamlAssertionAsString(Element element) throws TransformerException {
            StringWriter writer = new StringWriter();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(element), new StreamResult(writer));
            return writer.toString();
        }

    }
}
