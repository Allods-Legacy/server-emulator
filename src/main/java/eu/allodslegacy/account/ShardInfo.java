package eu.allodslegacy.account;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationException;
import org.jetbrains.annotations.NotNull;

public class ShardInfo implements CppOutSerializable {

    @NotNull
    private final String shardName;
    @NotNull
    private final String shardComment;
    private final int avatarNumber;
    private final int population;
    private final int maxUsersOnShard;
    private final FactionCount[] factionCounts;
    private final boolean isPvp;

    public ShardInfo(@NotNull String shardName, @NotNull String shardComment, int avatarNumber, int population, int maxUsersOnShard, FactionCount[] factionCounts, boolean isPvp) {
        this.shardName = shardName;
        this.shardComment = shardComment;
        this.avatarNumber = avatarNumber;
        this.population = population;
        this.maxUsersOnShard = maxUsersOnShard;
        this.factionCounts = factionCounts;
        this.isPvp = isPvp;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeBoolean(isPvp);
        out.writeInt(this.avatarNumber);
        if (this.factionCounts.length > 20) {
            throw new SerializationException();
        }
        out.writeInt(this.factionCounts.length);
        for (FactionCount faction : this.factionCounts) {
            if (faction == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                faction.writeCpp(out);
            }
        }

        out.writeInt(this.maxUsersOnShard);
        out.writeInt(this.population);
        out.writeUTF(this.shardComment);
        out.writeUTF(this.shardName);
    }
}
