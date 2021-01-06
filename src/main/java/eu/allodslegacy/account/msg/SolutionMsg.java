package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;

public class SolutionMsg implements CppInSerializable {

    private long first;
    private long second;

    public SolutionMsg() {
    }

    public long getFirst() {
        return first;
    }

    public long getSecond() {
        return second;
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        this.first = in.readLong();
        this.second = in.readLong();
    }
}
