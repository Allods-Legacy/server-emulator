package eu.allodslegacy.account;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import eu.allodslegacy.account.api.AccountServerHttpAPI;
import eu.allodslegacy.account.db.dao.DAOFactory;

public class AccountServer extends AbstractBehavior<AccountServer.Cmd> {

    private AccountServer(ActorContext<Cmd> context) throws Exception {
        super(context);
        Config config = context.getSystem().settings().config().getConfig("account-server");
        DAOFactory factory = DAOFactory.create(config.getConfig("database"));
        AuthListener authListener = new AuthListener(config.getConfig("auth-listener"), factory.getAccountDataSetDAO());
        AccountServerHttpAPI api = new AccountServerHttpAPI(config.getConfig("api"), factory.getAccountDataSetDAO());
        authListener.start(context.getSystem());
        api.start(context.getSystem());
    }

    public static Behavior<Cmd> create() {
        return Behaviors.setup(context -> {
            context.getLog().info("Starting account server ...");
            return new AccountServer(context);
        });
    }

    @Override
    public Receive<Cmd> createReceive() {
        return newReceiveBuilder()
                .onMessage(RegisterShard.class, msg -> {
                    getContext().getLog().info("Register shard: {}", msg.name);
                    return this;
                })
                .build();
    }

    public interface Cmd {
    }

    public static final class RegisterShard implements Cmd {

        public final String name;
        public final String comment;
        public final boolean isPvp;
        public final int maxUsersOnShard;

        public RegisterShard(String name, String comment, boolean isPvp, int maxUsersOnShard) {
            this.name = name;
            this.comment = comment;
            this.isPvp = isPvp;
            this.maxUsersOnShard = maxUsersOnShard;
        }
    }
}
