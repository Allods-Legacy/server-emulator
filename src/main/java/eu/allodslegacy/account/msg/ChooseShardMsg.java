package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;
import org.jetbrains.annotations.NotNull;

public class ChooseShardMsg implements CppInSerializable {

    @NotNull
    private String shardName;

    public ChooseShardMsg(@NotNull String shardName) {
        this.shardName = shardName;
    }

    public ChooseShardMsg() {

    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        this.shardName = in.readUTF();
    }
}
