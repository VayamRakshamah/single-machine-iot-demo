package io.github.iotdemo.edge.mqtt;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.openssl.PEMKeyPair;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;

import javax.net.ssl.*;
import java.io.FileReader;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.Certificate;

final class TlsSocketFactory {
    private TlsSocketFactory() {}

    static SSLSocketFactory create(String caPath, String certificatePath, String privateKeyPath) throws MqttTlsException {
        if (blank(caPath) || blank(certificatePath) || blank(privateKeyPath)) {
            throw new MqttTlsException("MQTT TLS requires CA, certificate, and private-key paths");
        }
        try {
            Certificate ca = certificate(caPath); Certificate device = certificate(certificatePath); PrivateKey key = privateKey(privateKeyPath);
            KeyStore trust = KeyStore.getInstance(KeyStore.getDefaultType()); trust.load(null); trust.setCertificateEntry("ca", ca);
            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm()); tmf.init(trust);
            KeyStore identity = KeyStore.getInstance(KeyStore.getDefaultType()); identity.load(null);
            identity.setKeyEntry("device", key, new char[0], new Certificate[]{device});
            KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()); kmf.init(identity, new char[0]);
            SSLContext context = SSLContext.getInstance("TLSv1.2"); context.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);
            return context.getSocketFactory();
        } catch (Exception error) { throw new MqttTlsException("Unable to initialize MQTT TLS certificates", error); }
    }

    private static Certificate certificate(String path) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(path))) {
            return new JcaX509CertificateConverter().getCertificate((X509CertificateHolder) parser.readObject());
        }
    }
    private static PrivateKey privateKey(String path) throws Exception {
        try (PEMParser parser = new PEMParser(new FileReader(path))) {
            Object object = parser.readObject(); JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
            return object instanceof PEMKeyPair pair ? converter.getKeyPair(pair).getPrivate() : converter.getPrivateKey((PrivateKeyInfo) object);
        }
    }
    private static boolean blank(String value) { return value == null || value.isBlank(); }
    static class MqttTlsException extends RuntimeException {
        MqttTlsException(String message) { super(message); }
        MqttTlsException(String message, Throwable cause) { super(message, cause); }
    }
}
