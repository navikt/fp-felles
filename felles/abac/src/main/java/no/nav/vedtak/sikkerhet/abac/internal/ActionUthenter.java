package no.nav.vedtak.sikkerhet.abac.internal;

import java.lang.reflect.Method;

import jakarta.ws.rs.Path;

public class ActionUthenter {

    private static final String SLASH = "/";

    private ActionUthenter() {

    }

    public static String action(Class<?> clazz, Method method) {
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

    private static String ensureStartsWithSlash(String value) {
        return value.startsWith(SLASH) ? value : SLASH + value;
    }
}
