package eu.allodslegacy.account.db.dataset;

import org.bson.codecs.pojo.annotations.BsonCreator;
import org.bson.codecs.pojo.annotations.BsonProperty;
import org.jetbrains.annotations.NotNull;

public class AccountShardInfo {

    @NotNull
    private final String shard;
    private final int avatarNumber;

    @BsonCreator
    public AccountShardInfo(@BsonProperty("shard") @NotNull String shard, @BsonProperty("avatarNumber") int avatarNumber) {
        this.shard = shard;
        this.avatarNumber = avatarNumber;
    }

    public @NotNull String getShard() {
        return shard;
    }

    public int getAvatarNumber() {
        return avatarNumber;
    }
}
