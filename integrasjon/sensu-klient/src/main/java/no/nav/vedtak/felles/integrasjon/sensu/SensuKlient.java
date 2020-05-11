package no.nav.vedtak.felles.integrasjon.sensu;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

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

    private String sensuHost;
    private int sensuPort;
    private final int maxRetrySendSensu = 2;

    private AtomicBoolean kanKobleTilSensu = new AtomicBoolean(false);
    private AtomicLong counterEvents = new AtomicLong(0L);

    SensuKlient() {
        // CDI-proxy
    }

    @Inject
    public SensuKlient(@KonfigVerdi(value = "sensu.host", defaultVerdi = "sensu.nais") String sensuHost,
                       @KonfigVerdi(value = "sensu.port", defaultVerdi = "3030") Integer sensuPort) {
        this.sensuHost = sensuHost;
        this.sensuPort = sensuPort;
    }

    public void logMetrics(SensuEvent metrics) {
        logMetrics(List.of(metrics));
    }

    /** Sender et set med events samlet til Sensu. */
    public void logMetrics(List<SensuEvent> metrics) {
        var event = SensuEvent.createBatchSensuRequest(metrics);
        logMetrics(event);
    }

    /**
     * @param sensuRequest - requst til å sende sensu. Kan inneholde mange metrikker
    */
    public void logMetrics(SensuEvent.SensuRequest sensuRequest) {
        var callId = MDCOperations.getCallId();
        if (executorService != null) {
            if (!kanKobleTilSensu.get()) {
                return; // ignorer, har skrudd av pga ingen tilkobling til sensu
            }
            String json = sensuRequest.toJson();
            executorService.execute(() -> {
                long startTs = System.nanoTime();
                try {
                    int rounds = maxRetrySendSensu; // prøver par ganger hvis broken pipe, uten å logge første gang
                    while (rounds > 0 && kanKobleTilSensu.get() && !Thread.currentThread().isInterrupted()) {
                        rounds--;
                        // sensu har en ping/pong/heartbeat protokol, men støtter ikke det p.t., så åpner ny socket/outputstream for hver melding
                        try (Socket socket = establishSocketConnectionIfNeeded()) {
                            try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                                writer.write(json, 0, json.length());
                                writer.flush();
                            }
                        } catch (UnknownHostException ex) {
                            sjekkBroken(callId, json, ex);
                            break;
                        } catch (IOException ex) {
                            // ink. SocketException
                            if (rounds <= 0) {
                                LOG.warn("Feil ved tilkobling til metrikkendepunkt. Kan ikke publisere melding fra callId[" + callId + "]: " + json, ex);
                                break;
                            }
                        } catch (Exception ex) {
                            sjekkBroken(callId, json, ex);
                            break;
                        }

                        Thread.sleep(50); // kort pause før retry
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    long tidBrukt = System.nanoTime() - startTs;
                    LOG.debug("Ferdig med logging av metrikker for callId {}. Tid brukt: {}ms", callId, TimeUnit.NANOSECONDS.toMillis(tidBrukt));
                }
            });
        } else {
            LOG.info("Sensu klienten er ikke startet ennå!");
        }
    }

    private void sjekkBroken(String callId, String json, Exception ex) {
        if (System.getenv("NAIS_CLUSTER_NAME") != null) {
            // broken, skrur av tilkobling så ikke flooder loggen
            kanKobleTilSensu.set(false);
            LOG.warn("Feil ved tilkobling til metrikkendepunkt. callId[" + callId + "]. Skrur av. Forsøkte melding: " + json, ex);
        }
    }

    private synchronized Socket establishSocketConnectionIfNeeded() throws Exception {
        Socket socket = new Socket();
        socket.setSoTimeout(5000);
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);
        return socket;
    }

    @Override
    public synchronized void start() {
        if (executorService != null) {
            throw new IllegalArgumentException("Service allerede startet, stopp først.");
        }
        executorService = Executors.newFixedThreadPool(3);
        kanKobleTilSensu.set(true);
    }

    @Override
    public synchronized void stop() {
        if (executorService != null) {
            kanKobleTilSensu.set(false);
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
