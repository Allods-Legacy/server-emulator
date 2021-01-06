package eu.allodslegacy.account.flows;

import akka.Done;
import akka.NotUsed;
import akka.stream.javadsl.Flow;
import akka.stream.javadsl.Keep;
import akka.stream.javadsl.Sink;
import akka.stream.javadsl.Source;
import akka.util.ByteString;
import eu.allodslegacy.account.msg.ProblemMsg;
import eu.allodslegacy.account.msg.SolutionMsg;
import eu.allodslegacy.io.serialization.CppSerializer;
import eu.allodslegacy.io.serialization.SerializationException;

import java.util.concurrent.CompletionStage;

public final class DDOSCheckerFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(long toFactorize) throws SerializationException {
        Sink<ByteString, CompletionStage<Done>> sink = Flow.of(ByteString.class)
                .take(1)
                .toMat(Sink.foreach(b -> {
                    SolutionMsg solution = CppSerializer.deserialize(b, SolutionMsg.class);
                    if (solution.getFirst() * solution.getSecond() != toFactorize) {
                        throw new Exception("DDOS check failed");
                    }
                }), Keep.right());
        Source<ByteString, NotUsed> source = Source.single(ByteString.fromArray(CppSerializer.serializeWithId(new ProblemMsg(toFactorize))));
        return Flow.fromSinkAndSourceMat(sink, source, Keep.left());
    }
}
