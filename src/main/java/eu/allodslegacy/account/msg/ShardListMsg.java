package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationId;

@SerializationId(9)
public class ShardListMsg implements CppOutSerializable {
    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeInt(0);
    }
}
