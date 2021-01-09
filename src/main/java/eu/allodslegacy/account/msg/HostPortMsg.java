package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppOutSerializable;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationId;

@SerializationId(3)
public class HostPortMsg implements CppOutSerializable {

    private final String host;
    private final int port;

    public HostPortMsg(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeUTF(this.host);
        out.writeInt(this.port);
    }
}
