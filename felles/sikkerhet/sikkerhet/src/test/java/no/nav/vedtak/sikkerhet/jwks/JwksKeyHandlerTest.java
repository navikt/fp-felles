package no.nav.vedtak.sikkerhet.jwks;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.interfaces.RSAPublicKey;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

public class JwksKeyHandlerTest {

    @Test
    public void skal_parse_jwks_og_hente_ut_key() throws Exception {
        String jsonStreng = les("example-jwks.json");

        JwksKeyHandler handler = new JwksKeyHandlerImpl(new TestKeySupplier(jsonStreng), null);

        Key key = handler.getValidationKey(new JwtHeader("", "RS256"));

        assertThat(key.getAlgorithm()).isEqualTo("RSA");
        assertThat(key.getFormat()).isEqualTo("X.509");
        assertThat(key).isInstanceOf(RSAPublicKey.class);
        RSAPublicKey publicKey = (RSAPublicKey) key;

        String pubKeyHex = "00cdae1d97db1db0df9024c6f066993b6cce04398be2c42037309216a950fa2900c13579a6dc801d80b7244cbf2ed31c452bf713baf4c825bfed6b734fe0a005842295bfe5cf5266103dd38224e112894dda469c898d3a506c9c4a3f301188a4fb73f1b9578aaf7bb0e8b544a21337aeb0a3765910fe372dbafb94ac279d6ec6f6855c3a435ecf41b59446af2fde0b57986303f0a711eded1db5fd19a0c39f650a05259f9ae5d305cd778ff057f3fdcd2e04b49d75df85a8e87354388c6ef148f57284db64d682161a9cfa4fb06199622a106c97eee55b69851eb3363894a7d3b8940fea1246c1a5103c49a5130b3dcf722ac5603834cef57ca7686063d8f20a3d";
        assertThat(publicKey.getModulus()).isEqualTo(new BigInteger(pubKeyHex, 16));
        assertThat(publicKey.getPublicExponent()).isEqualTo(new BigInteger("65537", 10));
    }

    @Test
    public void skal_returnere_null_dersom_key_ikke_finnes_i_jwks() throws Exception {
        String jsonStreng = les("example-jwks.json");

        JwksKeyHandler handler = new JwksKeyHandlerImpl(new TestKeySupplier(jsonStreng), null);

        Key key = handler.getValidationKey(new JwtHeader("tull og tøys", "RS256"));
        assertThat(key).isNull();
    }

    @Test
    public void __key_rotation__skal_ikke_hente_nye_nøkler_fra_jwks_supplier_dersom_nøkkel_finnes_i_cache() throws Exception {
        String jwks1 = les("example-jwks.json");
        String jwks2 = les("example2-jwks.json");
        TestKeySupplier keySupplier = new TestKeySupplier(jwks2, jwks1);

        // first time a key is requested, jwks2 will be downloaded
        JwksKeyHandler handler = new JwksKeyHandlerImpl(keySupplier, null);
        Key key = handler.getValidationKey(new JwtHeader("8d3074d68906276778eb3aea6a4b698e893d934b", "RS256"));
        assertThat(key).isInstanceOf(RSAPublicKey.class);
        assertThat(keySupplier.getCounter()).isEqualTo(1);

        // asking for a key which is in jwks2 will use cache
        handler.getValidationKey(new JwtHeader("98f252c36ece673b2609f8d2d1b387a00de68e51", "RS256"));
        handler.getValidationKey(new JwtHeader("8d3074d68906276778eb3aea6a4b698e893d934b", "RS256"));

        assertThat(keySupplier.getCounter()).isEqualTo(1);
    }

    @Test
    public void __key_rotation__skal_hente_nye_nøkler_fra_jwks_supplier_dersom_nøkkel_ikke_finnes_i_cache() throws Exception {
        String jwks1 = les("example-jwks.json");
        String jwks2 = les("example2-jwks.json");
        TestKeySupplier keySupplier = new TestKeySupplier(jwks1, jwks2);

        // asks for key with blank kid, will download jwks1 (which has one entry)
        JwksKeyHandler handler = new JwksKeyHandlerImpl(keySupplier, null);
        Key key = handler.getValidationKey(new JwtHeader("", "RS256"));
        assertThat(key).isInstanceOf(RSAPublicKey.class);
        assertThat(keySupplier.getCounter()).isEqualTo(1);

        // asking for key which is not in jwks1 key store, will download jwks2
        key = handler.getValidationKey(new JwtHeader("98f252c36ece673b2609f8d2d1b387a00de68e51", "RS256"));
        assertThat(key).isInstanceOf(RSAPublicKey.class);
        assertThat(keySupplier.getCounter()).isEqualTo(2);
    }

    private static String les(String filnavn) throws IOException {
        return new String(Files.readAllBytes(Paths.get("src/test/resources/" + filnavn)));
    }

    private static class TestKeySupplier implements Supplier<String> {

        private String[] replies;
        private int index;

        TestKeySupplier(String... replies) {
            this.replies = replies;
        }

        @Override
        public String get() {
            return replies[index++];
        }

        public int getCounter() {
            return index;
        }
    }
}
