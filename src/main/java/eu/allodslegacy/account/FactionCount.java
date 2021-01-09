package eu.allodslegacy.account;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;

public class FactionCount implements CppOutSerializable {

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
