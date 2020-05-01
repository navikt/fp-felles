package no.nav.vedtak.felles.integrasjon.sensu;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
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
    private Thread outputThread;

    private String sensuHost;
    private int sensuPort;

    private BlockingQueue<DataEvent> queue = new ArrayBlockingQueue<>(2000, false);

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
        doLogMetrics(metrics, callId);
    }

    private void doLogMetrics(SensuEvent sensuEvent, String callId) {
        try {
            boolean added = queue.offer(new DataEvent(sensuEvent, callId), 1, TimeUnit.SECONDS);
            if (!added) {
                LOG.warn("Sensu queue er full (remainingCapacity {}, kunne ikke legge til {}. Sjekk om sensu har stoppet publisering.",
                    queue.remainingCapacity(), sensuEvent);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            // do nothing, stopper antagelig server
        } catch (IOException e) {
            throw new IllegalArgumentException("Kunne ikke serialisere til json: " + sensuEvent, e);
        }
    }

    private synchronized Socket createSocket() throws Exception {
        Socket socket = new Socket();
        socket.setSoTimeout(1000);
        socket.setKeepAlive(true);
        socket.connect(new InetSocketAddress(sensuHost, sensuPort), 1000);
        return socket;
    }

    void feedSensu() {

        publiserTilSensu();
    }

    private void publiserTilSensu() {
        // publiserer i en dobbel, loop, en tråd, en åpen socket/outputstream, inntil interrupted
        while (true) {

            try (Socket socket = createSocket()) {

                publiserLoop(socket);

            } catch (InterruptedException e) {
                // avbryter
                LOG.warn(getClass().getSimpleName() + " interrupted, stopper publisering");
                Thread.currentThread().interrupt();
                return;
            } catch (UnknownHostException e) {
                // avbryter
                LOG.error(getClass().getSimpleName() + ": ukjent host, stopper publisering", e);
                return;
            } catch (Exception ex) {
                String cluster = System.getenv("NAIS_CLUSTER_NAME");
                if (cluster != null) {
                    LOG.warn("Feil ved tilkobling til metrikkendepunkt, vil forsøke igjen:" + cluster, ex);
                }

                // forsøker igjen om litt
                try {
                    Thread.sleep(1 * 1000L);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return; // avbryter
                }
            } catch (Throwable t) {
                // avbryter
                LOG.error("Kan ikke publisere til Sensu, stopper publisering", t);
                return;
            }

        }
    }

    private void publiserLoop(Socket socket) throws InterruptedException {
        try (OutputStream socketOutputStream = socket.getOutputStream();
                OutputStreamWriter writer = new OutputStreamWriter(socketOutputStream, StandardCharsets.UTF_8)) {

            // indre loop
            while (true) {

                var dataEvent = queue.poll(1000L, TimeUnit.MILLISECONDS);

                if (dataEvent != null) {
                    writer.write(dataEvent.json, 0, dataEvent.json.length());
                    writer.write('\n');
                    writer.flush();
                } else {
                    // heartbeat
                }
            }

        } catch (IOException e) {
            LOG.warn("Feil ved sending av event.", e);
        }
    }

    @Override
    public synchronized void start() {
        if (outputThread == null) {
            this.outputThread = new Thread(this::feedSensu, getClass().getSimpleName());
            this.outputThread.setDaemon(true);
            this.outputThread.start();
        }
    }

    @Override
    public synchronized void stop() {
        if (outputThread != null) {
            outputThread.interrupt();
            outputThread = null;
        }
    }

    static class DataEvent {
        SensuEvent sensuEvent;
        String callId;
        private String json;

        DataEvent(SensuEvent sensuEvent, String callId) throws IOException {
            this.sensuEvent = sensuEvent;
            this.callId = callId;
            this.json = sensuEvent.toSensuRequest().toJson();
        }

    }
}
