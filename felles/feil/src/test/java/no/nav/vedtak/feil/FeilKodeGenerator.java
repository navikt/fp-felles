package no.nav.vedtak.feil;

        import java.util.Random;

class FeilKodeGenerator {

    private static final Random RANDOM = new Random();

    private static String lagFeilkode() {
        return String.format("FP-%06d", RANDOM.nextInt(1000000));
    }

    public static void main(String[] args) {
        for (int i = 0; i < 10; i++) {
            System.out.println(lagFeilkode());
        }
    }
}
