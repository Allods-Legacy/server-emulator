package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.CppSerializable;
import eu.allodslegacy.io.serialization.SerializationDataInput;
import eu.allodslegacy.io.serialization.SerializationDataOutput;
import eu.allodslegacy.io.serialization.SerializationException;

public class RSAPublicKeyMsg implements CppSerializable {

    private byte[] modulus;
    private byte[] exponent;

    public RSAPublicKeyMsg(byte[] modulus, byte[] exponent) {
        this.modulus = modulus;
        this.exponent = exponent;
    }

    public RSAPublicKeyMsg() {
    }

    public byte[] getModulus() {
        return modulus;
    }

    public byte[] getExponent() {
        return exponent;
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        int exponentSize = in.readInt();

        if (exponentSize < 0 || exponentSize > 2000) {
            throw new SerializationException();
        }

        this.exponent = new byte[exponentSize];
        for (int i = 0; i < exponentSize; i++) {
            this.exponent[i] = in.readByte();
        }

        int modulusSize = in.readInt();

        if (modulusSize < 0 || modulusSize > 2000) {
            throw new SerializationException();
        }

        this.modulus = new byte[modulusSize];
        for (int i = 0; i < modulusSize; i++) {
            this.modulus[i] = in.readByte();
        }
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.writeInt(this.exponent.length);
        for (byte b : this.exponent) {
            out.write(b);
        }
        out.writeInt(this.modulus.length);
        for (byte b : this.modulus) {
            out.write(b);
        }
    }
}
