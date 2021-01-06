package eu.allodslegacy.account.certificate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.DigestException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class ServerCertificate {

    private static final int SIGNATURE_SIZE = 16;
    private static final String HASH_FUNCTION = "MD5";

    private final byte[] certificate;

    private ServerCertificate(byte[] certificate) {
        this.certificate = certificate;
    }

    public static ServerCertificate load(String certificatePath) throws IOException {
        byte[] certificate = Files.readAllBytes(Path.of(certificatePath));
        return new ServerCertificate(certificate);
    }

    public byte[] sign(byte[] seed) throws DigestException, NoSuchAlgorithmException {
        MessageDigest hashFunction = MessageDigest.getInstance(HASH_FUNCTION);
        byte[] temp = new byte[seed.length + certificate.length];
        System.arraycopy(seed, 0, temp, 0, seed.length);
        System.arraycopy(certificate, 0, temp, seed.length, certificate.length);
        byte[] result = new byte[certificate.length];
        System.arraycopy(certificate, 0, result, 0, certificate.length - SIGNATURE_SIZE);
        hashFunction.update(temp);
        hashFunction.digest(result, certificate.length - SIGNATURE_SIZE, SIGNATURE_SIZE);
        return result;
    }
}
