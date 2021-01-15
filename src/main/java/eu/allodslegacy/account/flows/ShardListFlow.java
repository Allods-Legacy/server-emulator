package eu.allodslegacy.account.flows;

import akka.Done;
import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.AskPattern;
import akka.stream.Attributes;
import akka.stream.javadsl.Flow;
import akka.stream.stage.AbstractInHandler;
import akka.stream.stage.AbstractOutHandler;
import akka.stream.stage.AsyncCallback;
import akka.stream.stage.GraphStageLogic;
import akka.util.ByteString;
import eu.allodslegacy.FrontEndInfo;
import eu.allodslegacy.RealmMode;
import eu.allodslegacy.account.AccountServerClient;
import eu.allodslegacy.account.AccountServerProtocol;
import eu.allodslegacy.account.db.dataset.Account;
import eu.allodslegacy.account.db.dataset.AccountShardInfo;
import eu.allodslegacy.account.msg.ChooseShardMsg;
import eu.allodslegacy.account.msg.HostPortMsg;
import eu.allodslegacy.account.msg.ShardListMsg;
import eu.allodslegacy.account.msg.ShardNotFoundMsg;
import eu.allodslegacy.io.net.NetGraphStage;
import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.CppSerializer;
import eu.allodslegacy.io.serialization.MsgFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletionStage;

public class ShardListFlow extends NetGraphStage {

    private final MsgFactory<CppInSerializable> msgFactory;
    private final ActorRef<AccountServerProtocol.Command> accountServer;
    private final ActorSystem<Void> actorSystem;
    private final AccountServerClient client;

    public ShardListFlow(ActorSystem<Void> actorSystem, AccountServerClient client, MsgFactory<CppInSerializable> msgFactory, ActorRef<AccountServerProtocol.Command> accountServer) {
        this.msgFactory = msgFactory;
        this.accountServer = accountServer;
        this.actorSystem = actorSystem;
        this.client = client;
    }

    public static Flow<ByteString, ByteString, CompletionStage<Done>> create(ActorSystem<Void> actorSystem, AccountServerClient client, MsgFactory<CppInSerializable> msgFactory, ActorRef<AccountServerProtocol.Command> accountServer) {
        return Flow.of(ByteString.class)
                .via(new ShardListFlow(actorSystem, client, msgFactory, accountServer))
                .watchTermination((notUsed, done) -> done);
    }

    @Override
    public GraphStageLogic createLogic(Attributes inheritedAttributes) {
        return new GraphStageLogic(shape) {

            private AsyncCallback<AccountServerProtocol.ShardListResult> shardListCallback;
            private AsyncCallback<AccountServerProtocol.HostPortResult> hostPortCallback;
            private AsyncCallback<AccountServerProtocol.ShardNotFound> shardNotFoundCallback;

            {
                setHandler(in, new AbstractInHandler() {
                    @Override
                    public void onPush() throws Exception {
                        CppInSerializable msg = msgFactory.deserializeMsg(grab(in));
                        if (msg instanceof ChooseShardMsg) {
                            ChooseShardMsg chooseShardMsg = (ChooseShardMsg) msg;
                            if (chooseShardMsg.getShardName().isEmpty()) {
                                AskPattern.ask(accountServer, AccountServerProtocol.RequestShardList::new, Duration.ofSeconds(10), actorSystem.scheduler())
                                        .thenAccept(result -> {
                                            if (result instanceof AccountServerProtocol.ShardListResult) {
                                                shardListCallback.invoke((AccountServerProtocol.ShardListResult) result);
                                            }
                                        });
                            } else {
                                AskPattern.<AccountServerProtocol.Command, AccountServerProtocol.Result>ask(accountServer, replyTo -> new AccountServerProtocol.RequestHostPort(replyTo, chooseShardMsg.getShardName()), Duration.ofSeconds(10), actorSystem.scheduler())
                                        .thenAccept(result -> {
                                            if (result instanceof AccountServerProtocol.HostPortResult) {
                                                hostPortCallback.invoke((AccountServerProtocol.HostPortResult) result);
                                            } else if (result instanceof AccountServerProtocol.ShardNotFound) {
                                                shardNotFoundCallback.invoke((AccountServerProtocol.ShardNotFound) result);
                                            }
                                        });
                            }
                        }
                    }
                });

                setHandler(out, new AbstractOutHandler() {
                    @Override
                    public void onPull() {
                        pull(in);
                    }
                });
            }

            @Override
            public void preStart() {
                shardListCallback = createAsyncCallback(shardListResult -> {
                    Account account = client.getAccount();
                    if (account == null) {
                        throw new Exception();
                    }
                    List<ShardListMsg.ShardInfo> result = new ArrayList<>();
                    for (FrontEndInfo serverShardInfo : shardListResult.shardInfos) {
                        int avatarNumber = 0;
                        for (AccountShardInfo accountShardInfo : account.getAccountShardInfos()) {
                            if (accountShardInfo.getShard().equals(serverShardInfo.getShardName())) {
                                avatarNumber = accountShardInfo.getAvatarNumber();
                                break;
                            }
                        }
                        List<ShardListMsg.ShardInfo.FactionCount> factionCounts = new ArrayList<>();
                        for (Map.Entry<Integer, Integer> entry : serverShardInfo.getFactionCounts().entrySet()) {
                            factionCounts.add(new ShardListMsg.ShardInfo.FactionCount(entry.getKey(), entry.getValue()));
                        }
                        result.add(new ShardListMsg.ShardInfo(
                                serverShardInfo.getShardName(),
                                serverShardInfo.getShardComment(),
                                avatarNumber,
                                serverShardInfo.getUserNumber(),
                                serverShardInfo.getMaxUsersOnShard(),
                                factionCounts.toArray(ShardListMsg.ShardInfo.FactionCount[]::new),
                                serverShardInfo.getRealmMode().equals(RealmMode.PVP)
                        ));
                    }
                    emit(out, ByteString.fromArray(CppSerializer.serializeWithId(new ShardListMsg(result.toArray(ShardListMsg.ShardInfo[]::new)))));
                });
                hostPortCallback = createAsyncCallback(hostPortResult -> emit(out, ByteString.fromArray(CppSerializer.serializeWithId(new HostPortMsg(hostPortResult.host, hostPortResult.port)))));
                shardNotFoundCallback = createAsyncCallback(res -> emit(out, ByteString.fromArray(CppSerializer.serializeWithId(new ShardNotFoundMsg()))));
            }
        };
    }
}
