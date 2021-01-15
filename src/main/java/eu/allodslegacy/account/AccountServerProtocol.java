package eu.allodslegacy.account;

import akka.actor.typed.ActorRef;
import eu.allodslegacy.FrontEndInfo;
import eu.allodslegacy.RealmMode;

import java.util.List;

public interface AccountServerProtocol {

    enum ShardNotFound implements Result {
        INSTANCE
    }

    interface Command {
    }

    interface Result {
    }

    final class RequestShardList implements Command {

        public final ActorRef<Result> replyTo;

        public RequestShardList(ActorRef<Result> replyTo) {
            this.replyTo = replyTo;
        }

    }

    final class RequestHostPort implements Command {

        public final String name;
        public final ActorRef<Result> replyTo;

        public RequestHostPort(ActorRef<Result> replyTo, String name) {
            this.name = name;
            this.replyTo = replyTo;
        }
    }

    final class RegisterShard implements Command {

        public final String host;
        public final int port;
        public final String name;
        public final String comment;
        public final RealmMode realmMode;
        public final int maxUsersOnShard;
        public final List<Integer> factions;

        public RegisterShard(String name, String host, int port, String comment, RealmMode realmMode, int maxUsersOnShard, List<Integer> factions) {
            this.name = name;
            this.host = host;
            this.port = port;
            this.comment = comment;
            this.realmMode = realmMode;
            this.maxUsersOnShard = maxUsersOnShard;
            this.factions = factions;
        }
    }

    final class ShardListResult implements Result {

        public final FrontEndInfo[] shardInfos;

        public ShardListResult(FrontEndInfo[] shardInfos) {
            this.shardInfos = shardInfos;
        }
    }

    final class HostPortResult implements Result {

        public final String host;
        public final int port;

        public HostPortResult(String host, int port) {
            this.host = host;
            this.port = port;
        }
    }
}
