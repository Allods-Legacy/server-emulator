package eu.allodslegacy.account.flows;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import eu.allodslegacy.account.msg.ValidationMsg;
import eu.allodslegacy.io.serialization.CppSerializer;
import eu.allodslegacy.io.serialization.SerializationException;

import java.util.concurrent.CompletionStage;

public final class ValidationCheckerFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(byte[] seed) throws SerializationException {
        Sink<ByteString, CompletionStage<Done>> sink = Flow.of(ByteString.class)
                .take(1)
                .toMat(Sink.foreach(b -> {
                    ValidationMsg validation = CppSerializer.deserialize(b, ValidationMsg.class);
                }), Keep.right());
        Source<ByteString, NotUsed> source = Source.single(ByteString.fromArray(CppSerializer.serializeWithId(new ValidationMsg(seed))));
        return Flow.fromSinkAndSourceMat(sink, source, Keep.left());
    }
}
