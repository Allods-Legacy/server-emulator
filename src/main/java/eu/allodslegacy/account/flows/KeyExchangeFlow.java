package eu.allodslegacy.account.flows;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import eu.allodslegacy.account.msg.RSAEncryptedMsg;
import eu.allodslegacy.account.msg.RSAPublicKeyMsg;
import eu.allodslegacy.io.crypto.CryptoUtils;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.serialization.CppSerializer;

import java.security.interfaces.RSAPublicKey;
import java.util.concurrent.CompletionStage;

public final class KeyExchangeFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(RSACipher serverRSACipher, RSACipher clientRSACipher) {
        return Flow.of(ByteString.class)
                .take(1)
                .map(b -> CppSerializer.deserialize(b, RSAEncryptedMsg.class))
                .map(msg -> {
                    if (!msg.getEncryptionMethod().equals(RSAEncryptedMsg.EncryptionMethod.SECRET_KEY)) {
                        throw new Exception("Wrong encryption method");
                    }
                    return msg.getData();
                })
                .via(serverRSACipher.decrypt())
                .map(b -> {
                    RSAPublicKeyMsg msg = CppSerializer.deserialize(b, RSAPublicKeyMsg.class);
                    RSAPublicKey clientPublicKey = CryptoUtils.constructPublicKey(msg.getModulus(), msg.getExponent());
                    clientRSACipher.setCryptKey(clientPublicKey);
                    return CppSerializer.serialize(new RSAPublicKeyMsg(
                            clientRSACipher.getPublicKey().getModulus().toByteArray(),
                            clientRSACipher.getPublicKey().getPublicExponent().toByteArray()
                    ));
                })
                .via(clientRSACipher.encrypt())
                .via(serverRSACipher.encrypt())
                .map(b -> CppSerializer.serializeWithId(new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_AND_SECRET_KEY, b)))
                .map(ByteString::fromArray)
                .watchTermination((notUsed, done) -> done);
    }
}
