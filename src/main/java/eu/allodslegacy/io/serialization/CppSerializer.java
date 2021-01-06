package eu.allodslegacy.io.serialization;

import akka.util.ByteString;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public final class CppSerializer {

    public static byte[] serializeWithId(CppOutSerializable msg) throws SerializationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        SerializationDataOutput out = new SerializationDataOutput(buffer);
        SerializationId serializationId = msg.getClass().getAnnotation(SerializationId.class);
        if (serializationId == null) {
            throw new SerializationException();
        }
        try {
            out.writeByte(serializationId.value());
            msg.writeCpp(out);
        } catch (Exception exception) {
            throw new SerializationException();
        }
        return buffer.toByteArray();
    }

    public static byte[] serialize(CppOutSerializable msg) throws SerializationException {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        SerializationDataOutput out = new SerializationDataOutput(buffer);
        try {
            msg.writeCpp(out);
        } catch (Exception exception) {
            throw new SerializationException();
        }
        return buffer.toByteArray();
    }

    public static <T extends CppInSerializable> T deserialize(ByteString data, Class<T> clazz) throws SerializationException {
        return deserialize(data.toArray(), clazz);
    }

    public static <T extends CppInSerializable> T deserialize(byte[] data, Class<T> clazz) throws SerializationException {
        ByteArrayInputStream buffer = new ByteArrayInputStream(data);
        SerializationDataInput in = new SerializationDataInput(buffer);
        try {
            T msg = clazz.getDeclaredConstructor().newInstance();
            msg.readCpp(in);
            return msg;
        } catch (Exception exception) {
            throw new SerializationException();
        }
    }
}
