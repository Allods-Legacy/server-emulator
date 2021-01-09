package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationId;

@SerializationId(11)
public class ShardNotFoundMsg implements CppOutSerializable {

    public ShardNotFoundMsg() {
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
    }
}
