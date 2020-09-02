package no.nav.vedtak.sts.client;

import java.util.HashMap;

import javax.xml.namespace.QName;

import org.apache.cxf.Bus;
import org.apache.cxf.BusException;
import org.apache.cxf.binding.soap.Soap12;
import org.apache.cxf.binding.soap.SoapMessage;
import org.apache.cxf.endpoint.Client;
import org.apache.cxf.endpoint.Endpoint;
import org.apache.cxf.endpoint.EndpointException;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.interceptor.LoggingInInterceptor;
import org.apache.cxf.interceptor.LoggingOutInterceptor;
import org.apache.cxf.message.Message;
import org.apache.cxf.service.model.EndpointInfo;
import org.apache.cxf.ws.addressing.WSAddressingFeature;
import org.apache.cxf.ws.policy.EndpointPolicy;
import org.apache.cxf.ws.policy.PolicyBuilder;
import org.apache.cxf.ws.policy.PolicyEngine;
import org.apache.cxf.ws.policy.attachment.reference.ReferenceResolver;
import org.apache.cxf.ws.policy.attachment.reference.RemoteReferenceResolver;
import org.apache.cxf.ws.security.trust.STSClient;
import org.apache.neethi.Policy;

import no.nav.vedtak.sts.client.NAVSTSClient.StsClientType;
import no.nav.vedtak.util.env.Environment;

@SuppressWarnings("deprecation")
public class StsConfigurationUtil {

    private static final Environment ENV = Environment.current();

    private StsConfigurationUtil() {
        throw new IllegalAccessError("Skal ikke instansieres");
    }

    @SuppressWarnings("resource")
    public static <T> T wrapWithSts(T port, NAVSTSClient.StsClientType samlTokenType) {
        Client client = ClientProxy.getClient(port);
        switch (samlTokenType) {
            case SECURITYCONTEXT_TIL_SAML:
                configureStsForOnBehalfOfWithOidc(client);
                break;
            case SYSTEM_SAML:
                configureStsForSystemUser(client);
                break;
            default:
                throw new IllegalArgumentException("Unknown enum value: " + samlTokenType);
        }
        return port;
    }

    public static void configureStsForOnBehalfOfWithOidc(Client client) {
        String location = requireProperty(SecurityConstants.STS_URL_KEY);
        String username = requireProperty(SecurityConstants.SYSTEMUSER_USERNAME);
        String password = requireProperty(SecurityConstants.SYSTEMUSER_PASSWORD);
        configureStsForOnBehalfOfWithOidc(client, location, username, password);
    }

    public static void configureStsForOnBehalfOfWithOidc(Client client, String stsURL, String stsUsername, String stsPassword) {
        STSClient stsClient = createBasicSTSClient(StsClientType.SECURITYCONTEXT_TIL_SAML, client.getBus(), stsURL, stsUsername, stsPassword);
        stsClient.setOnBehalfOf(new OnBehalfOfWithOidcCallbackHandler());
        client.getRequestContext().put("security.sts.client", stsClient);
        client.getRequestContext().put(org.apache.cxf.ws.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT, false);
        setEndpointPolicyReference(client, "classpath:stsPolicy.xml");
    }

    public static void configureStsForSystemUser(Client client) {
        String location = requireProperty(SecurityConstants.STS_URL_KEY);
        String username = requireProperty(SecurityConstants.SYSTEMUSER_USERNAME);
        String password = requireProperty(SecurityConstants.SYSTEMUSER_PASSWORD);

        configureStsForSystemUser(client, location, username, password);
    }

    public static void configureStsForSystemUser(Client client, String location, String username, String password) {
        new WSAddressingFeature().initialize(client, client.getBus());

        STSClient stsClient = createBasicSTSClient(StsClientType.SYSTEM_SAML, client.getBus(), location, username, password);
        client.getRequestContext().put("security.sts.client", stsClient);
        client.getRequestContext().put(org.apache.cxf.ws.security.SecurityConstants.CACHE_ISSUED_TOKEN_IN_ENDPOINT, false);
        setEndpointPolicyReference(client, "classpath:stsPolicy.xml");
    }

    private static String requireProperty(String key) {
        String property = ENV.getProperty(key);
        if (property == null) {
            throw StsFeil.FACTORY.påkrevdSystemPropertyMangler(key).toException();
        }
        return property;
    }

    @SuppressWarnings("resource")
    private static STSClient createBasicSTSClient(StsClientType type, Bus bus, String location, String username, String password) {
        STSClient stsClient = new NAVSTSClient(bus, type);
        stsClient.setWsdlLocation("wsdl/ws-trust-1.4-service.wsdl");
        stsClient.setServiceQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceProvider"));
        stsClient.setEndpointQName(new QName("http://docs.oasis-open.org/ws-sx/ws-trust/200512/wsdl", "SecurityTokenServiceSOAP"));
        stsClient.setEnableAppliesTo(false);
        stsClient.setAllowRenewing(false);

        try {
            // Endpoint must be set on clients request context
            // as the wrapping requestcontext is not available
            // when creating the client from WSDL (ref cxf-users mailinglist)
            stsClient.getClient().getRequestContext().put(Message.ENDPOINT_ADDRESS, location);
        } catch (BusException | EndpointException e) {
            throw StsFeil.FACTORY.kunneIkkeSetteEndpointAddress(location, e).toException();
        }

        stsClient.getOutInterceptors().add(new LoggingOutInterceptor());
        stsClient.getInInterceptors().add(new LoggingInInterceptor());

        HashMap<String, Object> properties = new HashMap<>();
        properties.put(org.apache.cxf.ws.security.SecurityConstants.USERNAME, username);
        properties.put(org.apache.cxf.ws.security.SecurityConstants.PASSWORD, password);
        stsClient.setProperties(properties);
        return stsClient;
    }

    private static void setEndpointPolicyReference(Client client, String uri) {
        Policy policy = resolvePolicyReference(client, uri);
        setClientEndpointPolicy(client, policy);
    }

    private static Policy resolvePolicyReference(Client client, String uri) {
        PolicyBuilder policyBuilder = client.getBus().getExtension(PolicyBuilder.class);
        ReferenceResolver resolver = new RemoteReferenceResolver("", policyBuilder);
        return resolver.resolveReference(uri);
    }

    private static void setClientEndpointPolicy(Client client, Policy policy) {
        Endpoint endpoint = client.getEndpoint();
        EndpointInfo endpointInfo = endpoint.getEndpointInfo();

        PolicyEngine policyEngine = client.getBus().getExtension(PolicyEngine.class);
        SoapMessage message = new SoapMessage(Soap12.getInstance());
        EndpointPolicy endpointPolicy = policyEngine.getClientEndpointPolicy(endpointInfo, null, message);
        policyEngine.setClientEndpointPolicy(endpointInfo, endpointPolicy.updatePolicy(policy, message));
    }

}
