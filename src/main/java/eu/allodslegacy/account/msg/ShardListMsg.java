package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationException;
import eu.allodslegacy.io.serialization.SerializationId;
import org.jetbrains.annotations.NotNull;

import java.io.Serializable;

@SerializationId(10)
public class ShardListMsg implements CppOutSerializable {

    private final ShardInfo[] shardList;

    public ShardListMsg(ShardInfo[] shardList) {
        this.shardList = shardList;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        if (this.shardList.length > 100) {
            throw new SerializationException();
        }
        out.writeInt(this.shardList.length);
        for (ShardInfo shardInfo : this.shardList) {
            if (shardInfo == null) {
                out.writeBoolean(false);
            } else {
                out.writeBoolean(true);
                shardInfo.writeCpp(out);
            }
        }
    }

    public static class ShardInfo implements CppOutSerializable {

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

        public static class FactionCount implements Serializable, CppOutSerializable {

            private final int factionResourceId;
            private final int count;

            public FactionCount(int factionResourceId, int count) {
                this.factionResourceId = factionResourceId;
                this.count = count;
            }

            @Override
            public void writeCpp(SerializationDataOutput out) throws Exception {
                out.writeInt(this.count);
                out.writeInt(this.factionResourceId);
            }
        }
    }
}
