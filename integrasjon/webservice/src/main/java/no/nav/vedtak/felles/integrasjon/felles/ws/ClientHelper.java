package no.nav.vedtak.felles.integrasjon.felles.ws;

import javax.xml.namespace.QName;

import org.apache.cxf.feature.LoggingFeature;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.apache.cxf.ws.addressing.WSAddressingFeature;

import no.nav.cxf.metrics.MetricFeature;

public class ClientHelper {

    public static <PortType> PortType createServicePort(String serviceUrl, Class<PortType> serviceClazz, String wsdl, String namespace, String svcName, String portName) {

        JaxWsProxyFactoryBean factoryBean = new JaxWsProxyFactoryBean();
        factoryBean.setWsdlURL(wsdl);
        factoryBean.setServiceName(new QName(namespace, svcName));
        factoryBean.setEndpointName(new QName(namespace, portName));
        factoryBean.setServiceClass(serviceClazz);
        factoryBean.setAddress(serviceUrl);
        factoryBean.getFeatures().add(new WSAddressingFeature());
        factoryBean.getFeatures().add(new LoggingFeature());
        factoryBean.getFeatures().add(new MetricFeature());
        factoryBean.getOutInterceptors().add(new CallIdOutInterceptor());

        return factoryBean.create(serviceClazz);

    }
}
