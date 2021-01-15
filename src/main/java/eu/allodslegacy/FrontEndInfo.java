package eu.allodslegacy;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.Map;

public class FrontEndInfo implements Serializable {

    @NotNull
    private final Map<Integer, Integer> factionCounts;
    @NotNull
    private String host;
    private int port;
    @NotNull
    private String shardName;
    @NotNull
    private String shardComment;
    private int userNumber;
    private int maxUsersOnShard;
    @NotNull
    private RealmMode realmMode;

    public FrontEndInfo(@NotNull String host, int port, @NotNull String shardName, @NotNull String shardComment, int userNumber, int maxUsersOnShard, @NotNull RealmMode realmMode, @NotNull Map<Integer, Integer> factionCounts) {
        this.host = host;
        this.port = port;
        this.shardName = shardName;
        this.shardComment = shardComment;
        this.userNumber = userNumber;
        this.maxUsersOnShard = maxUsersOnShard;
        this.realmMode = realmMode;
        this.factionCounts = factionCounts;
    }

    public @NotNull String getHost() {
        return host;
    }

    public void setHost(@NotNull String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public @NotNull String getShardName() {
        return shardName;
    }

    public void setShardName(@NotNull String shardName) {
        this.shardName = shardName;
    }

    public @NotNull String getShardComment() {
        return shardComment;
    }

    public void setShardComment(@NotNull String shardComment) {
        this.shardComment = shardComment;
    }

    public int getMaxUsersOnShard() {
        return maxUsersOnShard;
    }

    public void setMaxUsersOnShard(int maxUsersOnShard) {
        this.maxUsersOnShard = maxUsersOnShard;
    }

    public @NotNull RealmMode getRealmMode() {
        return realmMode;
    }

    public void setRealmMode(@NotNull RealmMode realmMode) {
        this.realmMode = realmMode;
    }

    public int getUserNumber() {
        return userNumber;
    }

    public void setUserNumber(int userNumber) {
        this.userNumber = userNumber;
    }

    public @NotNull Map<Integer, Integer> getFactionCounts() {
        return factionCounts;
    }
}
