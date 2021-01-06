package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationId;

@SerializationId(6)
public class ProblemMsg implements CppOutSerializable {

    private long toFactorize;

    public ProblemMsg(long toFactorize) {
        this.toFactorize = toFactorize;
    }

    public ProblemMsg() {

    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeLong(this.toFactorize);
    }
}
