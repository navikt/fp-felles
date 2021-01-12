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

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import no.nav.vedtak.apptjeneste.AppServiceHandler;
import no.nav.vedtak.konfig.KonfigVerdi;
import no.nav.vedtak.log.mdc.MDCOperations;
import no.nav.vedtak.util.env.Environment;

@ApplicationScoped
public class SensuKlient implements AppServiceHandler {

    private static final Logger LOG = LoggerFactory.getLogger(SensuKlient.class);
    private static ExecutorService executorService;

    private String sensuHost;
    private int sensuPort;
    private final int maxRetrySendSensu = 2;

    private AtomicBoolean kanKobleTilSensu = new AtomicBoolean(false);

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
            final String json = sensuRequest.toJson();
            final String jsonForEx = formatForException(json, sensuRequest.getAntallEvents());
            executorService.execute(() -> {
                long startTs = System.nanoTime();
                try {
                    int rounds = maxRetrySendSensu; // prøver par ganger hvis broken pipe, uten å logge første gang
                    while (rounds > 0 && kanKobleTilSensu.get() && !Thread.currentThread().isInterrupted()) {
                        rounds--;
                        // sensu har en ping/pong/heartbeat protokol, men støtter ikke det p.t., så
                        // åpner ny socket/outputstream for hver melding
                        try (Socket socket = establishSocketConnectionIfNeeded()) {
                            try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                                writer.write(json, 0, json.length());
                                writer.flush();
                            }
                        } catch (UnknownHostException ex) {
                            sjekkBroken(callId, jsonForEx, ex);
                            break;
                        } catch (IOException ex) {
                            // ink. SocketException
                            if (rounds <= 0) {
                                LOG.warn("Feil ved tilkobling til metrikkendepunkt. Kan ikke publisere melding fra callId[" + callId + "]: "
                                        + jsonForEx, ex);
                                break;
                            }
                        } catch (Exception ex) {
                            sjekkBroken(callId, jsonForEx, ex);
                            break;
                        }

                        Thread.sleep(50); // kort pause før retry
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    long tidBrukt = System.nanoTime() - startTs;
                    // Enable ved behov LOG.debug("Ferdig med logging av metrikker for callId {}. Tid brukt: {}ms", callId, TimeUnit.NANOSECONDS.toMillis(tidBrukt));
                }
            });
        } else {
            LOG.info("Sensu klienten er ikke startet ennå!");
        }
    }

    private static String formatForException(String json, int antallEvents) {
        if (antallEvents > 1) {
            int maxSubstrLen = 1000;
            String substr = json.substring(0, Math.min(json.length(), maxSubstrLen)) + "....";
            return String.format("events[%s]: ", antallEvents, substr);
        } else {
            return String.format("events[%s]: ", antallEvents, json);
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
        socket.setSoTimeout(60000);
        socket.setReuseAddress(true);
        socket.connect(new InetSocketAddress(sensuHost, sensuPort), 30000);
        return socket;
    }

    @Override
    public synchronized void start() {
        if (Environment.current().isLocal()){
            LOG.info("Kjører lokalt, kobler ikke opp mot sensu-server.");
            return;
        }
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
