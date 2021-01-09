package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;
import eu.allodslegacy.io.serialization.SerializationId;
import org.jetbrains.annotations.NotNull;

@SerializationId(3)
public class ChooseShardMsg implements CppInSerializable {

    @NotNull
    private String shardName;

    public ChooseShardMsg() {
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        this.shardName = in.readUTF();
    }

    public String getShardName() {
        return this.shardName;
    }
}
