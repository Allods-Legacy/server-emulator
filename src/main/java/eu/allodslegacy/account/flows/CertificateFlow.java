package eu.allodslegacy.account.flows;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import eu.allodslegacy.account.certificate.ServerCertificate;
import eu.allodslegacy.account.msg.CertificateRequestMsg;
import eu.allodslegacy.account.msg.CertificateResponse;
import eu.allodslegacy.account.msg.RSAEncryptedMsg;
import eu.allodslegacy.io.crypto.RSACipher;
import eu.allodslegacy.io.serialization.CppSerializer;

import java.util.concurrent.CompletionStage;

public final class CertificateFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(RSACipher clientCipher, RSACipher serverCipher, ServerCertificate certificate) {
        return Flow.of(ByteString.class)
                .take(1)
                .map(b -> CppSerializer.deserialize(b.toArray(), RSAEncryptedMsg.class))
                .map(msg -> {
                    if (!msg.getEncryptionMethod().equals(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY)) {
                        throw new Exception("Wrong encryption method");
                    }
                    return msg.getData();
                })
                .via(clientCipher.decrypt())
                .map(b -> CppSerializer.deserialize(ByteString.fromArray(b), CertificateRequestMsg.class).getSeed())
                .map(certificate::sign)
                .map(CertificateResponse::new)
                .map(CppSerializer::serialize)
                .via(clientCipher.encrypt())
                .map(data -> new RSAEncryptedMsg(RSAEncryptedMsg.EncryptionMethod.RANDOM_KEY, data))
                .map(CppSerializer::serializeWithId)
                .map(ByteString::fromArray)
                .watchTermination((notUSed, done) -> done);
    }
}
