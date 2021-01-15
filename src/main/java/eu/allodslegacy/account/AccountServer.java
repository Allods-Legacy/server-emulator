package eu.allodslegacy.account;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import com.typesafe.config.Config;
import eu.allodslegacy.FrontEndInfo;
import eu.allodslegacy.account.api.AccountServerHttpAPI;
import eu.allodslegacy.account.db.dao.DAOFactory;
import org.apache.commons.lang3.SerializationUtils;

import java.util.HashMap;
import java.util.Map;

public class AccountServer extends AbstractBehavior<AccountServerProtocol.Command> {

    private final ShardHolder shardHolder;

    private AccountServer(ActorContext<AccountServerProtocol.Command> context) throws Exception {
        super(context);
        Config config = context.getSystem().settings().config().getConfig("account-server");
        DAOFactory factory = DAOFactory.create(config.getConfig("database"));
        this.shardHolder = new ShardHolder();
        AuthListener authListener = new AuthListener(config.getConfig("auth-listener"), factory.getAccountDataSetDAO(), context.getSelf());
        AccountServerHttpAPI api = new AccountServerHttpAPI(config.getConfig("api"), factory.getAccountDataSetDAO());
        authListener.start(context.getSystem());
        api.start(context.getSystem());
    }

    public static Behavior<AccountServerProtocol.Command> create() {
        return Behaviors.setup(context -> {
            context.getLog().info("Starting account server ...");
            return new AccountServer(context);
        });
    }

    @Override
    public Receive<AccountServerProtocol.Command> createReceive() {
        return newReceiveBuilder()
                .onMessage(AccountServerProtocol.RegisterShard.class, msg -> {
                    getContext().getLog().info("Register shard: {}", msg.name);
                    Map<Integer, Integer> factionCounts = new HashMap<>();
                    for (int id : msg.factions) {
                        factionCounts.put(id, 0);
                    }
                    this.shardHolder.addShard(new FrontEndInfo(msg.host, msg.port, msg.name, msg.comment, 0, msg.maxUsersOnShard, msg.realmMode, factionCounts));
                    return this;
                })
                .onMessage(AccountServerProtocol.RequestShardList.class, msg -> {
                    msg.replyTo.tell(new AccountServerProtocol.ShardListResult(SerializationUtils.clone(this.shardHolder.getShards())));
                    return this;
                })
                .onMessage(AccountServerProtocol.RequestHostPort.class, msg -> {
                    FrontEndInfo shard = this.shardHolder.getShard(msg.name);
                    if (shard == null) {
                        msg.replyTo.tell(AccountServerProtocol.ShardNotFound.INSTANCE);
                    } else {
                        msg.replyTo.tell(new AccountServerProtocol.HostPortResult(shard.getHost(), shard.getPort()));
                    }
                    return this;
                })
                .build();
    }
}
