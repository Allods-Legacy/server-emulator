package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.*;

@SerializationId(12) //1.1.02 11 //1.1.04 12
public class ValidationMsg implements CppSerializable {

    private byte[] seed;

    public ValidationMsg(byte[] seed) {
        this.seed = seed;
    }

    public ValidationMsg() {
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        byte seedSize = in.readByte();
        if (seedSize < 8) {
            throw new SerializationException();
        }
        this.seed = new byte[seedSize];
        for (int i = 0; i < seedSize; i++) {
            this.seed[i] = in.readByte();
        }
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeByte(this.seed.length);
        for (byte b : seed) {
            out.write(b);
        }
    }

    public byte[] getSeed() {
        return this.seed;
    }
}
