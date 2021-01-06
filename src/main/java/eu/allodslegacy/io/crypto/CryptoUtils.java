package eu.allodslegacy.io.crypto;

import java.io.IOException;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;

public final class CryptoUtils {

    public static RSAPublicKey loadPublicKey(String modulusFile, String exponentFile) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        byte[] pubExponent = Files.readAllBytes(Path.of(exponentFile));
        byte[] pubModulus = Files.readAllBytes(Path.of(modulusFile));
        return constructPublicKey(pubModulus, pubExponent);
    }

    public static RSAPrivateKey loadPrivateKey(String modulusFile, String exponentFile) throws InvalidKeySpecException, NoSuchAlgorithmException, IOException {
        byte[] prvExponent = Files.readAllBytes(Path.of(exponentFile));
        byte[] prvModulus = Files.readAllBytes(Path.of(modulusFile));
        return constructPrivateKey(prvModulus, prvExponent);
    }

    public static RSAPublicKey constructPublicKey(byte[] modulus, byte[] exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger m = new BigInteger(1, modulus);
        BigInteger e = new BigInteger(1, exponent);
        return (RSAPublicKey) KeyFactory.getInstance("RSA").generatePublic(new RSAPublicKeySpec(m, e));
    }

    public static RSAPrivateKey constructPrivateKey(byte[] modulus, byte[] exponent) throws NoSuchAlgorithmException, InvalidKeySpecException {
        BigInteger m = new BigInteger(1, modulus);
        BigInteger e = new BigInteger(1, exponent);
        return (RSAPrivateKey) KeyFactory.getInstance("RSA").generatePrivate(new RSAPrivateKeySpec(m, e));
    }
}
