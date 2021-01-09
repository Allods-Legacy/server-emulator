package eu.allodslegacy.account.msg;

import eu.allodslegacy.account.ShardInfo;
import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationException;
import eu.allodslegacy.io.serialization.SerializationId;

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
}
