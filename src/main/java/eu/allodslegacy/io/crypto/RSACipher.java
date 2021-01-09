package eu.allodslegacy.io.crypto;

import akka.NotUsed;
import akka.stream.javadsl.Flow;

import javax.crypto.*;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.CompletionStage;

public class RSACipher {

    private final static int DECRYPT_SIZE = 128;
    private final static int ENCRYPT_SIZE = 86;
    private final static int BUFFER_BLOCK_SIZE = 128;
    private final static String ALGORITHM = "RSA/ECB/OAEPWithSHA-1AndMGF1Padding";

    private final SecureRandom secureRandom;

    private RSAPrivateKey decryptKey;
    private RSAPublicKey publicKey;

    private RSAPublicKey cryptKey;

    public RSACipher(SecureRandom secureRandom) {
        this.secureRandom = secureRandom;
    }

    public RSACipher(SecureRandom secureRandom, RSAPrivateKey decryptKey, RSAPublicKey publicKey, RSAPublicKey cryptKey) {
        this.secureRandom = secureRandom;
        this.decryptKey = decryptKey;
        this.publicKey = publicKey;
        this.cryptKey = cryptKey;
    }

    private static Flow<byte[], byte[], NotUsed> cipherFlow(Cipher cipher, int blockSize) {
        return Flow.of(byte[].class).map(msg -> {
            int blocksNum = msg.length / blockSize + (msg.length % blockSize == 0 ? 0 : 1);
            byte[] buffer = new byte[blocksNum * BUFFER_BLOCK_SIZE];
            int offset = 0;
            int bytesToDecrypt = msg.length;
            for (int i = 0; i < blocksNum; bytesToDecrypt -= blockSize) {
                offset += cipher.doFinal(msg, i * blockSize, Math.min(bytesToDecrypt, blockSize), buffer, offset);
                ++i;
            }
            byte[] result = new byte[offset];
            System.arraycopy(buffer, 0, result, 0, offset);
            return result;
        });
    }

    public Flow<byte[], byte[], CompletionStage<NotUsed>> decrypt() {
        return Flow.lazyFlow(() -> {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, decryptKey, secureRandom);
            return cipherFlow(cipher, DECRYPT_SIZE);
        });
    }

    public Flow<byte[], byte[], CompletionStage<NotUsed>> encrypt() {
        return Flow.lazyFlow(() -> {
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, this.cryptKey, this.secureRandom);
            return cipherFlow(cipher, ENCRYPT_SIZE);
        });
    }

    public byte[] decrypt(byte[] data) throws BadPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, decryptKey, secureRandom);
        return this.doCipher(data, cipher, DECRYPT_SIZE);
    }

    public byte[] encrypt(byte[] data) throws BadPaddingException, ShortBufferException, IllegalBlockSizeException, NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, cryptKey, this.secureRandom);
        return doCipher(data, cipher, ENCRYPT_SIZE);
    }

    private byte[] doCipher(byte[] msg, Cipher cipher, int blockSize) throws BadPaddingException, ShortBufferException, IllegalBlockSizeException {
        int blocksNum = msg.length / blockSize + (msg.length % blockSize == 0 ? 0 : 1);
        byte[] buffer = new byte[blocksNum * BUFFER_BLOCK_SIZE];
        int offset = 0;
        int bytesToDecrypt = msg.length;
        for (int i = 0; i < blocksNum; bytesToDecrypt -= blockSize) {
            offset += cipher.doFinal(msg, i * blockSize, Math.min(bytesToDecrypt, blockSize), buffer, offset);
            ++i;
        }
        byte[] result = new byte[offset];
        System.arraycopy(buffer, 0, result, 0, offset);
        return result;
    }

    public void setCryptKey(RSAPublicKey cryptKey) {
        this.cryptKey = cryptKey;
    }

    public void setDecryptKey(RSAPrivateKey decryptKey, RSAPublicKey publicKey) {
        this.decryptKey = decryptKey;
        this.publicKey = publicKey;
    }

    public RSAPublicKey getPublicKey() {
        return publicKey;
    }
}
