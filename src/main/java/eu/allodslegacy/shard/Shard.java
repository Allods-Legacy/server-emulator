package eu.allodslegacy.shard;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import eu.allodslegacy.RealmMode;
import eu.allodslegacy.account.AccountServerProtocol;

import java.util.LinkedList;

public class Shard extends AbstractBehavior<Shard.Cmd> {

    private final ActorRef<AccountServerProtocol.Command> accountServer;

    private Shard(ActorContext<Cmd> context, ActorRef<AccountServerProtocol.Command> accountServer) {
        super(context);
        this.accountServer = accountServer;
    }

    public static Behavior<Cmd> create(ActorRef<AccountServerProtocol.Command> accountServer) {
        return Behaviors.setup(context -> {
            context.getLog().info("Starting shard ...");
            Config config = context.getSystem().settings().config().getConfig("shard");
            String host = config.getString("frontend.host");
            int port = config.getInt("frontend.port");
            accountServer.tell(new AccountServerProtocol.RegisterShard("shard", host, port, "test shard", RealmMode.PVP, 1000, new LinkedList<>()));
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
