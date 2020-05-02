package no.nav.vedtak.felles.integrasjon.sensu;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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
        var callId = MDCOperations.getCallId();
        doLogMetrics(callId, List.of(metrics));
    }

    /** @deprecated - kun eksperimentelt p.t. ser ut som sensu socket støtter arrays av events. */
    @Deprecated
    public void logMetrics(List<SensuEvent> metrics) {
        var callId = MDCOperations.getCallId();
        doLogMetrics(callId, metrics);
    }

    private void doLogMetrics(String callId, List<SensuEvent> sensuEvents) {
        if (executorService != null) {
            if (!kanKobleTilSensu.get()) {
                return; // ignorer, har skrudd av pga ingen tilkobling til sensu
            }
            String data = toJson(sensuEvents);
            int antall = sensuEvents.size();
            executorService.execute(() -> {
                long startTs = System.currentTimeMillis();
                try {
                    int rounds = 2; // prøver par ganger hvis broken pipe, uten å logge første gang
                    while (rounds > 0 && kanKobleTilSensu.get() && !Thread.currentThread().isInterrupted()) {
                        rounds--;
                        // sensu har en ping/pong/heartbeat protokol, men støtter ikke det p.t., så åpner ny socket/outputstream for hver melding
                        try (Socket socket = establishSocketConnectionIfNeeded()) {
                            try (OutputStreamWriter writer = new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)) {
                                writer.write(data, 0, data.length());
                                writer.flush();
                                trackProgress(antall);
                            } catch (SocketException e) {
                                throw e; // throw next level
                            } catch (IOException e) {
                                if (rounds <= 0) {
                                    LOG.warn("Feil ved sending av event: " + data, e);
                                    break;
                                }
                            }
                        } catch (SocketException ex) {
                            sjekkBroken(callId, data, ex);
                            if (rounds <= 0) {
                                LOG.warn("Feil ved tilkobling til metrikkendepunkt. Kan ikke publisere melding fra callId[" + callId + "]: " + data, ex);
                                break;
                            }
                        } catch (Exception ex) {
                            sjekkBroken(callId, data, ex);
                            break;
                        }
                        
                        Thread.sleep(500); // kort pause før retry
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                } finally {
                    long tidBrukt = System.currentTimeMillis() - startTs;
                    LOG.debug("Ferdig med logging av metrikker for callId {}. Tid brukt: {}", callId, tidBrukt);
                }
            });
        } else {
            LOG.info("Sensu klienten er ikke startet ennå!");
        }
    }

    private void trackProgress(int antall) {
        long s = counterEvents.getAndAdd(antall);
        long f = s - (s % 100);
        long v = s + antall;
        if ((v - f) >= 100) {
            LOG.info("Har publisert {} metrikker til sensu", v);
        }
    }

    private String toJson(List<SensuEvent> events) {
        if (events.size() == 1) {
            // standard, single event
            return events.get(0).toSensuRequest().toJson();
        } else {
            // json array
            return "[" + events.stream().map(e -> e.toSensuRequest().toJson()).collect(Collectors.joining(",")) + "]";
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
        try {
            Socket socket = new Socket();
            socket.setSoTimeout(1000);
            socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);
            return socket;
        } catch (Exception ex) {
            final String feilMelding = "Feil ved start av socket tilkobling.";
            LOG.debug(feilMelding, ex);
            throw new Exception(feilMelding, ex);
        }
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
