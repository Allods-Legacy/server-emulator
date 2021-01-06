package eu.allodslegacy.io.net.version;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public final class NetVersionCheckFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(ByteString serverNetVersion) {
        return Flow.of(ByteString.class)
                .take(1)
                .map(clientNetVersion -> {
                    if (!clientNetVersion.equals(serverNetVersion)) {
                        throw new Exception("Wrong client version");
                    }
                    return serverNetVersion;
                })
                .watchTermination((notUsed, done) -> done);
    }
}
