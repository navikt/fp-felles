package no.nav.vedtak.sikkerhet.abac.internal;

import no.nav.vedtak.sikkerhet.abac.beskyttet.ServiceType;

import javax.jws.WebMethod;
import javax.ws.rs.Path;
import java.lang.reflect.Method;

public class ActionUthenter {

    private static final String SLASH = "/";

    private ActionUthenter() {

    }

    public static String action(Class<?> clazz, Method method) {
        return action(clazz, method, ServiceType.REST);
    }

    public static String action(Class<?> clazz, Method method, ServiceType serviceType) {
        return ServiceType.WEBSERVICE.equals(serviceType)
                ? actionForWebServiceMethod(method)
                : actionForRestMethod(clazz, method);
    }

    private static String actionForRestMethod(Class<?> clazz, Method method) {
        Path pathOfClass = clazz.getAnnotation(Path.class);
        Path pathOfMethod = method.getAnnotation(Path.class);

        String path = "";
        if (pathOfClass != null) {
            path += ensureStartsWithSlash(pathOfClass.value());
        }
        if (pathOfMethod != null) {
            path += ensureStartsWithSlash(pathOfMethod.value());
        }
        return path;
    }

    private static String actionForWebServiceMethod(Method method) {
        WebMethod webMethodAnnotation = finnWebMethod(method);
        if (webMethodAnnotation.action().isEmpty()) {
            throw new IllegalArgumentException(
                    "Mangler action på @WebMethod-annotering for metode på Webservice " + method.getName());
        }
        return webMethodAnnotation.action();
    }

    private static WebMethod finnWebMethod(Method method) {
        // annoteringen finnes i et av interfacene
        for (Class<?> anInterface : method.getDeclaringClass().getInterfaces()) {
            try {
                Method deklarertMetode = anInterface.getDeclaredMethod(method.getName(), method.getParameterTypes());
                WebMethod annotation = deklarertMetode.getAnnotation(WebMethod.class);
                if (annotation != null) {
                    return annotation;
                }
            } catch (NoSuchMethodException e) {
                // forventet hvis webservice arver fra flere interface
            }
        }
        throw new IllegalArgumentException("Mangler @WebMethod-annotering i interface for " + method.getDeclaringClass() + "." + method.getName());
    }

    private static String ensureStartsWithSlash(String value) {
        return value.startsWith(SLASH) ? value : SLASH + value;
    }
}
