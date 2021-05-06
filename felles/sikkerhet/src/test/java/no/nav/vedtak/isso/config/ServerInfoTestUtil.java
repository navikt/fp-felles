package no.nav.vedtak.isso.config;

public class ServerInfoTestUtil {

    /**
     * Øker scope til public slik at tester kan fjerne og re-initalisere instansen
     */
    public static void clearServerInfoInstance() {
        ServerInfo.clearInstance();
    }
}
