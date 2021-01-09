package eu.allodslegacy.account.flows;

import akka.Done;
import akka.stream.javadsl.Flow;
import akka.util.ByteString;
import eu.allodslegacy.account.FactionCount;
import eu.allodslegacy.account.ShardInfo;
import eu.allodslegacy.account.msg.ChooseShardMsg;
import eu.allodslegacy.account.msg.HostPortMsg;
import eu.allodslegacy.account.msg.ShardListMsg;
import eu.allodslegacy.account.msg.ShardNotFoundMsg;
import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.CppSerializer;
import eu.allodslegacy.io.serialization.MsgFactory;

import java.util.concurrent.CompletionStage;

public final class ShardListFlow {

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(MsgFactory<CppInSerializable> msgFactory) {
        return Flow.of(ByteString.class)
                .map(msgFactory::deserializeMsg)
                .map(msg -> {
                    if (msg instanceof ChooseShardMsg) {
                        ChooseShardMsg chooseShardMsg = (ChooseShardMsg) msg;
                        if (chooseShardMsg.getShardName().equals("")) {
                            return new ShardListMsg(new ShardInfo[]{new ShardInfo("default", "comment", 3, 11, 500, new FactionCount[]{new FactionCount(41240, 10), new FactionCount(41238, 20)}, true)});
                        } else {
                            return new HostPortMsg("127.0.0.1", 9000);
                        }
                    }
                    return new ShardNotFoundMsg();
                })
                .map(CppSerializer::serializeWithId)
                .map(ByteString::fromArray)
                .watchTermination((notUsed, done) -> done);
    }

}
