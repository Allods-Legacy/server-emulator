package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppInSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;
import eu.allodslegacy.io.serialization.SerializationException;

public class CertificateRequestMsg implements CppInSerializable {

    private byte[] seed;

    public CertificateRequestMsg() {
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        int seedSize = in.readInt();
        if (seedSize < 0) {
            throw new SerializationException();
        }
        this.seed = new byte[seedSize];
        for (int i = 0; i < seedSize; i++) {
            this.seed[i] = in.readByte();
        }
    }

    public byte[] getSeed() {
        return this.seed;
    }
}
