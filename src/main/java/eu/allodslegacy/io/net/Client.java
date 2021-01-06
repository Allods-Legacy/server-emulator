package eu.allodslegacy.io.net;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.japi.Pair;
import akka.stream.KillSwitches;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

public class Client {

    private static final Flow<ByteString, ByteString, Pair<Pair<Source<ByteString, NotUsed>, Sink<ByteString, NotUsed>>, UniqueKillSwitch>> serverFlow;

    static {
        Sink<ByteString, Source<ByteString, NotUsed>> sink = FramingFlow.in().toMat(BroadcastHub.of(ByteString.class, 1), Keep.right());
        Source<ByteString, Sink<ByteString, NotUsed>> source = MergeHub.of(ByteString.class).via(FramingFlow.out());
        serverFlow = Flow.fromSinkAndSourceMat(sink, source, Keep.both()).joinMat(KillSwitches.singleBidi(), Keep.both());
    }

    private final Source<ByteString, NotUsed> netIn;
    private final Sink<ByteString, NotUsed> netOut;
    private final UniqueKillSwitch connectionKillSwitch;
    private final ActorSystem<Void> actorSystem;
    private final Tcp.IncomingConnection connection;

    public Client(Tcp.IncomingConnection connection, ActorSystem<Void> actorSystem) {
        Pair<Pair<Source<ByteString, NotUsed>, Sink<ByteString, NotUsed>>, UniqueKillSwitch> materialized = connection.handleWith(serverFlow, actorSystem);
        this.netOut = materialized.first().second();
        this.netIn = materialized.first().first();
        this.connectionKillSwitch = materialized.second();
        this.actorSystem = actorSystem;
        this.connection = connection;
    }

    public <T extends Client> CompletionStage<T> attachFlow(Flow<ByteString, ByteString, CompletionStage<Done>> flow) {
        CompletableFuture<T> completion = new CompletableFuture<>();
        this.netIn
                .viaMat(flow, Keep.right())
                .to(netOut)
                .run(actorSystem)
                .exceptionally(exception -> {
                    completion.completeExceptionally(exception);
                    this.closeConnection();
                    return Done.done();
                })
                .whenComplete(((done, throwable) -> completion.complete((T) this)));
        return completion;
    }

    public <T> T attachSource(Source<ByteString, T> source) {
        return source.toMat(netOut, Keep.left()).run(actorSystem);
    }

    public String getIp() {
        return this.connection.remoteAddress().getAddress().toString();
    }

    public void closeConnection() {
        this.connectionKillSwitch.shutdown();
    }

    public void abortConnection(Throwable exception) {
        this.connectionKillSwitch.abort(exception);
    }
}
