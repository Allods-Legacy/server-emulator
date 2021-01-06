package eu.allodslegacy.account.msg;

import eu.allodslegacy.io.serialization.*;
import org.jetbrains.annotations.NotNull;

@SerializationId(7)
public class RSAEncryptedMsg implements CppSerializable {

    @NotNull
    private EncryptionMethod encryptionMethod;
    @NotNull
    private byte[] data;

    public RSAEncryptedMsg() {
    }

    public RSAEncryptedMsg(@NotNull EncryptionMethod encryptionMethod, byte[] data) {
        this.encryptionMethod = encryptionMethod;
        this.data = data;
    }

    @Override
    public void readCpp(SerializationDataInput in) throws Exception {
        this.data = in.readByteArray();
        this.encryptionMethod = EncryptionMethod.readCpp(in);
    }

    @NotNull
    public EncryptionMethod getEncryptionMethod() {
        return encryptionMethod;
    }

    @NotNull
    public byte[] getData() {
        return data;
    }

    @Override
    public void writeCpp(SerializationDataOutput out) throws Exception {
        out.write(data);
        out.writeByte(this.encryptionMethod.ordinal());
    }

    public enum EncryptionMethod {
        NOT_ENCRYPTED,
        SECRET_KEY,
        RANDOM_KEY,
        RANDOM_AND_SECRET_KEY;

        public static EncryptionMethod readCpp(@NotNull SerializationDataInput in) throws Exception {
            int value = in.readByte();
            if (value < values().length) {
                return values()[value];
            } else {
                throw new SerializationException();
            }

        }
    }
}
