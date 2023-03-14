package no.nav.vedtak.sikkerhet.oidc.validator;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.interfaces.RSAPublicKey;

import org.jose4j.base64url.Base64Url;
import org.jose4j.jwk.PublicJsonWebKey;
import org.jose4j.jwk.RsaJsonWebKey;
import org.jose4j.lang.JoseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KeyStoreTool {

    private static RsaJsonWebKey jwk;

    private static final Logger LOG = LoggerFactory.getLogger(KeyStoreTool.class);

    static {
        PublicKey myPublicKey;
        PrivateKey myPrivateKey;
        var keystorePassword = getKeyStoreAndKeyPassword();
        var keyAndCertAlias = getKeyAndCertAlias();

        try (var keystoreFile = KeyStoreTool.class.getResourceAsStream("/test-keystore.jks")) {
            var ks = KeyStore.getInstance("JKS");
            ks.load(keystoreFile, keystorePassword);

            KeyStore.ProtectionParameter protParam = new KeyStore.PasswordProtection(keystorePassword);
            var pk = (KeyStore.PrivateKeyEntry) ks.getEntry(keyAndCertAlias, protParam);
            myPrivateKey = pk.getPrivateKey();
            var cert = ks.getCertificate(keyAndCertAlias);
            myPublicKey = cert.getPublicKey();

            //KeyStoreTool.keystore = ks;
        } catch (KeyStoreException | NoSuchAlgorithmException | CertificateException | IOException | UnrecoverableEntryException e) {
            LOG.error("Error during loading of keystore. Do you have your keystore in order, soldier?", e);
            throw new RuntimeException(e);
        }

        try {
            jwk = (RsaJsonWebKey) PublicJsonWebKey.Factory.newPublicJwk(myPublicKey);
            jwk.setPrivateKey(myPrivateKey);
            jwk.setKeyId("1");
        } catch (JoseException e) {
            LOG.error("Error during init of JWK: " + e);
            throw new RuntimeException(e);
        }

    }

    public static char[] getKeyStoreAndKeyPassword() {
        return "changeit".toCharArray();
    }

    public static String getKeyAndCertAlias() {
        return System.getProperty("no.nav.modig.security.appkey", "app-key");
    }

    public static RsaJsonWebKey getJsonWebKey() {
        return jwk;
    }

    public static String getJwks() {
        var kty = "RSA";
        var kid = "1";
        var use = "sig";
        var alg = "RS256";
        var e = Base64Url.encode(jwk.getRsaPublicKey().getPublicExponent().toByteArray());
        var publicKey = (RSAPublicKey) jwk.getPublicKey();

        var bytes = publicKey.getModulus().toByteArray();
        var n = Base64Url.encode(bytes);

        return String.format(
            "{\"keys\":[{" + "\"kty\":\"%s\"," + "\"alg\":\"%s\"," + "\"use\":\"%s\"," + "\"kid\":\"%s\"," + "\"n\":\"%s\"," + "\"e\":\"%s\"" + "}]}",
            kty, alg, use, kid, n, e);
    }
}
