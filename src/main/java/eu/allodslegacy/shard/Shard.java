package eu.allodslegacy.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import eu.allodslegacy.account.AccountServer;

public class Shard extends AbstractBehavior<Shard.Cmd> {

    private final ActorRef<AccountServer.Cmd> accountServer;

    private Shard(ActorContext<Cmd> context, ActorRef<AccountServer.Cmd> accountServer) {
        super(context);
        this.accountServer = accountServer;
        Config config = context.getSystem().settings().config().getConfig("shard");
        String host = config.getString("frontend.host");
        int port = config.getInt("frontend.port");
        accountServer.tell(new AccountServer.RegisterShard("shard", "test shard", true, 1000));
    }

    public static Behavior<Cmd> create(ActorRef<AccountServer.Cmd> accountServer) {
        return Behaviors.setup(context -> {
            context.getLog().info("Starting shard ...");
            return new Shard(context, accountServer);
        });
    }

    @Override
    public Receive<Cmd> createReceive() {
        return newReceiveBuilder().build();
    }

    public interface Cmd {
    }
}
