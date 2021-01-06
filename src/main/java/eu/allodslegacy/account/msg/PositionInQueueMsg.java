package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationId;

@SerializationId(5)
public class PositionInQueueMsg implements CppOutSerializable {

    private int position;

    public PositionInQueueMsg() {
    }

    public PositionInQueueMsg(int position) {
        this.position = position;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeInt(this.position);
    }
}
