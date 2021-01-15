package eu.allodslegacy;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.Behaviors;
import akka.cluster.typed.Cluster;
import akka.cluster.typed.ClusterSingleton;
import akka.cluster.typed.SingletonActor;
import eu.allodslegacy.account.AccountServer;
import eu.allodslegacy.account.AccountServerProtocol;
import eu.allodslegacy.shard.Shard;

public class Starter {

    public static void main(String[] args) {
        ActorSystem<Void> system = ActorSystem.create(RootBehaviour.create(), "AllodsLegacy");
    }

    public static class RootBehaviour {

        static Behavior<Void> create() {
            return Behaviors.setup(context -> {
                Cluster cluster = Cluster.get(context.getSystem());
                ClusterSingleton singleton = ClusterSingleton.get(context.getSystem());
                ActorRef<AccountServerProtocol.Command> accountServer = singleton.init(
                        SingletonActor.of(
                                Behaviors.supervise(AccountServer.create())
                                        .onFailure(
                                                SupervisorStrategy.restart()
                                        )
                                , "AccountServer"));
                if (cluster.selfMember().hasRole("shard")) {
                    context.spawn(Shard.create(accountServer), "shard");
                }
                return Behaviors.empty();
            });
        }

    }

}
