package eu.allodslegacy.io.net;

import akka.Done;
import akka.NotUsed;
import akka.actor.typed.ActorSystem;
import akka.stream.KillSwitches;
import akka.stream.UniqueKillSwitch;
import akka.stream.javadsl.*;
import akka.util.ByteString;

import java.util.concurrent.CompletionStage;

public class Client {

    private Source<ByteString, NotUsed> netIn;
    private Sink<ByteString, NotUsed> netOut;
    private UniqueKillSwitch connectionKillSwitch;
    private final ActorSystem<Void> actorSystem;
    private final Tcp.IncomingConnection connection;

    public Client(Tcp.IncomingConnection connection, ActorSystem<Void> actorSystem) {
        Sink<ByteString, Source<ByteString, NotUsed>> sink = FramingFlow.in().toMat(BroadcastHub.of(ByteString.class, 1), Keep.right());
        Source<ByteString, Sink<ByteString, NotUsed>> source = MergeHub.of(ByteString.class).via(FramingFlow.out());
        Flow<ByteString, ByteString, CompletionStage<Done>> serverFlow = Flow
                .fromSinkAndSourceCoupledMat(sink, source, Keep.both())
                .joinMat(KillSwitches.singleBidi(), Keep.both())
                .watchTermination((materialized, done) -> {
                    this.netOut = materialized.first().second();
                    this.netIn = materialized.first().first();
                    this.connectionKillSwitch = materialized.second();
                    return done;
                });
        connection.handleWith(serverFlow, actorSystem).thenRun(this::onConnectionClosed);
        this.actorSystem = actorSystem;
        this.connection = connection;
    }

    public <T extends Client> CompletionStage<T> attachFlow(Flow<ByteString, ByteString, CompletionStage<Done>> flow) {
        return this.netIn
                .viaMat(flow, Keep.right())
                .to(netOut)
                .run(actorSystem)
                .whenComplete((done, err) -> {
                    if (err != null) {
                        this.abortConnection(err);
                    }
                }).thenApply(done -> (T) this);
                /*.exceptionally(exception -> {
                    this.abortConnection(exception);
                    return Done.done();
                }).thenApply(done -> (T) this);*/
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

    public void onConnectionClosed() {
    }
}
