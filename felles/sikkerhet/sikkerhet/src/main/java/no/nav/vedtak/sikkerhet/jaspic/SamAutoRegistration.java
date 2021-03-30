package no.nav.vedtak.sikkerhet.jaspic;

import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.module.ServerAuthModule;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import no.nav.vedtak.sikkerhet.ContextPathHolder;

/**
 *
 * Denne er ikke lenger annotert med @WebListener for å unngå automatisk
 * oppstart. Dette bryter den hardkodede koblingen mellom token validering og
 * jaas. Applikasjoner som trenger denne fremdeles må enten registrere den
 * programmatisk eller i lokal web.xml. På sikt skal all Jaas/jaspic-kode dø.
 *
 */
public class SamAutoRegistration implements ServletContextListener {
    private static final String CONTEXT_REGISTRATION_ID = "no.nav.vedtak.sikkerhet.jaspic.registrationId";

    public SamAutoRegistration() {
    }

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ContextPathHolder.instance(sce.getServletContext().getContextPath());
        registerServerAuthModule(new OidcAuthModule(), sce.getServletContext());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        deregisterServerAuthModule(sce.getServletContext());
    }

    // For bruk i applikasjonslokale WebListeners
    public static void initServerAuthModule(ServletContext context) {
        ContextPathHolder.instance(context.getContextPath());
        registerServerAuthModule(new OidcAuthModule(), context);
    }

    /**
     * Registers a server auth module as the one and only module for the application
     * corresponding to the given servlet context.
     *
     * This will override any other modules that have already been registered,
     * either via proprietary means or using the standard API.
     *
     * @param serverAuthModule the server auth module to be registered
     * @param servletContext   the context of the app for which the module is
     *                         registered
     * @return A String identifier assigned by an underlying factory corresponding
     *         to an underlying factory-factory-factory registration
     */
    public static String registerServerAuthModule(ServerAuthModule serverAuthModule, ServletContext servletContext) {

        // Register the factory-factory-factory for the SAM
        String registrationId = AuthConfigFactory.getFactory().registerConfigProvider(
                new OidcAuthConfigProvider(serverAuthModule),
                "HttpServlet",
                getAppContextID(servletContext),
                "Default single SAM authentication config provider");

        // Remember the registration ID returned by the factory, so we can unregister
        // the JASPIC module when the web module
        // is undeployed. JASPIC being the low level API that it is won't do this
        // automatically.
        servletContext.setAttribute(CONTEXT_REGISTRATION_ID, registrationId);

        return registrationId;
    }

    /**
     * Deregisters the server auth module (and encompassing wrappers/factories) that
     * was previously registered via a call to registerServerAuthModule.
     *
     * @param servletContext the context of the app for which the module is
     *                       deregistered
     */
    public static void deregisterServerAuthModule(ServletContext servletContext) {
        String registrationId = (String) servletContext.getAttribute(CONTEXT_REGISTRATION_ID);
        if (!isEmpty(registrationId)) {
            AuthConfigFactory.getFactory().removeRegistration(registrationId);
        }
    }

    /**
     * Gets the app context ID from the servlet context.
     *
     * The app context ID is the ID that JASPIC associates with the given
     * application. In this case that given application is the web application
     * corresponding to the ServletContext.
     *
     * @param context the servlet context for which to obtain the JASPIC app context
     *                ID
     * @return the app context ID for the web application corresponding to the given
     *         context
     */
    public static String getAppContextID(ServletContext context) {
        return context.getVirtualServerName() + " " + context.getContextPath();
    }

    public static boolean isEmpty(String string) {
        return string == null || string.isEmpty();
    }
}
