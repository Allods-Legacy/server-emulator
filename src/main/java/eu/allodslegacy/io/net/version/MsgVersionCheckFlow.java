package eu.allodslegacy.io.net.version;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public final class MsgVersionCheckFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(ByteString serverMsgVersion, ByteString okMessage) {
        return Flow.of(ByteString.class)
                .take(1)
                .map(clientMsgVersion -> {
                    if (!clientMsgVersion.equals(serverMsgVersion)) {
                        throw new Exception("Wrong msg version");
                    }
                    return okMessage;
                })
                .watchTermination((notUsed, done) -> done);
    }
}
