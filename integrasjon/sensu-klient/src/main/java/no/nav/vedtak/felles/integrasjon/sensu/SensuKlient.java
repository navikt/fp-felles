package no.nav.vedtak.felles.integrasjon.sensu;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class SensuKlient {

    private static final Logger LOG = LoggerFactory.getLogger(SensuKlient.class);
    private static final ExecutorService executorService = Executors.newFixedThreadPool(2);
    private static final Socket socket = new Socket();

    private String sensuHost;
    private int sensuPort;

    SensuKlient() {
        // CDI-proxy
    }

    @Inject
    public SensuKlient(@KonfigVerdi(value = "SENSU_HOST", defaultVerdi = "sensu.nais") String sensuHost,
                       @KonfigVerdi(value = "SENSU_PORT", defaultVerdi = "3030") Integer sensuPort) {
        this.sensuHost = sensuHost;
        this.sensuPort = sensuPort;
    }

    public void logMetrics(SensuEvent metrics) {
        var callId = MDCOperations.getCallId();
        doLogMetrics(metrics, callId);
    }

    private void doLogMetrics(SensuEvent sensuEvent, String callId) {
        LOG.info("Før launch av metrikklogg for callId: {}", callId);
        executorService.execute(() -> {
            long startTs = System.currentTimeMillis();
            try (Socket socket = establishSocketConnections()) {
                String data = sensuEvent.toSensuRequest().toJson();
                LOG.debug("Sender json metrikk til sensu: {}", data);
                try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                    LOG.info("Start logging av metrikker for callId {}", callId);
                    writer.write(data, 0, data.length());
                    writer.flush();
                    LOG.debug("Skrev {} bytes med data.", data.length());
                } catch (IOException e) {
                    LOG.error("Feil ver sending av event {}", sensuEvent, e);
                }
            } catch (Exception ex) {
                if (System.getenv("NAIS_CLUSTER_NAME") != null) {
                    LOG.error("Feil ved tilkobling til metrikkendepunktet", ex);
                }
            } finally {
                long tidBrukt = System.currentTimeMillis() - startTs;
                LOG.info("Ferdig med logging av metrikker for callId {}. Tid brukt: {}", callId, tidBrukt);
            }
        });
    }

    private Socket establishSocketConnections() throws Exception {
        if (socket.isClosed() || !socket.isConnected()) {
            try {
                socket.setSoTimeout(1000);
                socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);
                return socket;
            } catch (Exception ex) {
                final String feilMelding = "Feil ved start av socket tilkobling.";
                LOG.error(feilMelding, ex);
                throw new Exception(feilMelding, ex);
            }
        }
        return socket;
    }
}
