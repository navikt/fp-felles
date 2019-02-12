package no.nav.vedtak.felles.integrasjon.felles.ws.doc;

import java.util.ArrayList;
import java.util.List;

import javax.jws.WebService;

import io.github.swagger2markup.markup.builder.MarkupBlockStyle;
import io.github.swagger2markup.markup.builder.MarkupDocBuilder;
import no.nav.vedtak.felles.integrasjon.felles.ws.SoapWebService;

class WebServicesModell implements MarkupOutput {

    private List<Entry> entries = new ArrayList<>();

    @Override
    public void apply(int sectionLevel, MarkupDocBuilder doc) {
        entries.forEach(entry -> {
            doc.sectionTitleLevel(sectionLevel, entry.getNavn());

            doc.paragraph(generateTjenesteBeskrivelse(entry));
            doc.block(generateKonfigurasjonsBeskrivelse(entry), MarkupBlockStyle.EXAMPLE);

            if (entry.docs != null) {
                entry.docs.forEach(txt -> doc.block(txt, MarkupBlockStyle.PASSTHROUGH));
            }
        });
    }

    private String generateTjenesteBeskrivelse(Entry entry) {
        return "\n" + entry.getTjenesteBeskrivelse() + "[Tjenestebeskrivelse]";
    }


    private String generateKonfigurasjonsBeskrivelse(Entry entry) {
        String konfig = ".Konfigurasjon" +
            "\n* *Endepunkt:* " + entry.getEndpoint() +
            "\n* *Klasse:* " + entry.targetClassQualifiedName;
        if (entry.getEndpointInterface() != null) {
            konfig += "\n* *Interface:* " + entry.getEndpointInterface();
        }
        if (entry.getTargetNamespace() != null) {
            konfig += "\n* *Target namespace:* " + entry.getTargetNamespace();
        }
        if (entry.getServiceName() != null) {
            konfig += "\n* *Service name:* " + entry.getServiceName();
        }
        if (entry.getPortName() != null) {
            konfig += "\n* *Port name:* " + entry.getPortName();
        }
        konfig += "";
        return konfig;
    }

    Entry leggTil(String targetClassQualifiedName, String docs, SoapWebService soapWebService, WebService webService) {
        Entry entry = new Entry(targetClassQualifiedName, docs, soapWebService, webService);
        entries.add(entry);
        return entry;
    }

    public List<Entry> getEntries() {
        return entries;
    }

    public static class Entry {
        private final String targetClassQualifiedName;
        private final SoapWebService soapWebService;
        private final WebService webService;
        private List<String> docs = new ArrayList<>();

        Entry(String targetClassQualifiedName, String javaDoc, SoapWebService soapWebService, WebService webService) {
            super();
            this.targetClassQualifiedName = targetClassQualifiedName;
            this.soapWebService = soapWebService;
            this.webService = webService;
            leggTil(javaDoc);
        }

        public String getNavn() {
            return webService.serviceName();
        }

        void leggTil(String doc) {
            if (doc != null && !doc.isEmpty()) {
                this.docs.add(doc);
            }
        }

        public List<String> getDocs() {
            return docs;
        }

        public String getEndpoint() {
            return soapWebService.endpoint();
        }

        String getEndpointInterface() {
            if (webService != null && !webService.endpointInterface().isEmpty()) {
                return webService.endpointInterface();
            }
            return null;
        }

        String getTargetNamespace() {
            if (webService != null && !webService.targetNamespace().isEmpty()) {
                return webService.targetNamespace();
            }
            return null;
        }

        String getServiceName() {
            if (webService != null && !webService.serviceName().isEmpty()) {
                return webService.serviceName();
            }
            return null;
        }

        String getPortName() {
            if (webService != null && !webService.portName().isEmpty()) {
                return webService.portName();
            }
            return null;
        }

        String getTjenesteBeskrivelse() {
            return soapWebService.tjenesteBeskrivelseURL();
        }
    }
}
