package no.nav.vedtak.felles.integrasjon.sensu;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;

@ApplicationScoped
public class SensuKlient implements AppServiceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SensuKlient.class);
    private static ExecutorService executorService;
    private Socket socket;

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
        this.socket = new Socket();
    }

    public void logMetrics(SensuEvent metrics) {
        var callId = MDCOperations.getCallId();
        doLogMetrics(metrics, callId);
    }

    private void doLogMetrics(SensuEvent sensuEvent, String callId) {
        LOG.debug("Før launch av metrikklogg for callId: {}", callId);
        if (executorService != null) {
            executorService.execute(() -> {
                long startTs = System.currentTimeMillis();
                try {
                    Socket socket = establishSocketConnectionIfNeeded();
                    String data = sensuEvent.toSensuRequest().toJson();
                    LOG.debug("Sender json metrikk til sensu: {}", data);
                    try {
                        LOG.debug("Start logging av metrikker for callId {}", callId);
                        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8));
                        writer.write(data, 0, data.length());
                        writer.newLine();
                        writer.flush();
                        LOG.debug("Skrev {} bytes med data.", data.length());
                    } catch (IOException e) {
                        LOG.warn("Feil ver sending av event {}", sensuEvent, e);
                    }
                } catch (Exception ex) {
                    if (System.getenv("NAIS_CLUSTER_NAME") != null) {
                        LOG.warn("Feil ved tilkobling til metrikkendepunktet", ex);
                    }
                } finally {
                    long tidBrukt = System.currentTimeMillis() - startTs;
                    LOG.info("Ferdig med logging av metrikker for callId {}. Tid brukt: {}", callId, tidBrukt);
                }
            });
        } else {
            LOG.warn("Sensu klienten er ikke startet ennå!");
        }
    }

    private synchronized Socket establishSocketConnectionIfNeeded() throws Exception {
        if (socket.isClosed() || !socket.isConnected()) {
            try {
                socket = new Socket();
                socket.setSoTimeout(1000);
                socket.setKeepAlive(true);
                socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);
                return socket;
            } catch (Exception ex) {
                final String feilMelding = "Feil ved start av socket tilkobling.";
                LOG.debug(feilMelding, ex);
                throw new Exception(feilMelding, ex);
            }
        }
        return socket;
    }

    @Override
    public synchronized void start() {
        if (executorService != null) {
            throw new IllegalArgumentException("Service allerede startet, stopp først.");
        }
        executorService = Executors.newFixedThreadPool(2);
    }

    @Override
    public synchronized void stop() {
        if (executorService != null) {
            executorService.shutdown();
            try {
                executorService.awaitTermination(30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            executorService.shutdownNow();
        }
    }
}
