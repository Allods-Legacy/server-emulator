package eu.allodslegacy.io.serialization;

import akka.util.ByteString;
import org.reflections.Reflections;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MsgFactory<T extends CppInSerializable> {

    private final Map<Byte, Class<? extends T>> idToClass;

    private MsgFactory(Map<Byte, Class<? extends T>> idToClass) {
        this.idToClass = idToClass;
    }

    public static <T extends CppInSerializable> MsgFactory<T> create(String packageName, Class<T> clazz) throws Exception {
        Map<Byte, Class<? extends T>> idToClass = new HashMap<>();
        Set<Class<? extends T>> msgClasses = new Reflections(packageName).getSubTypesOf(clazz);
        for (Class<? extends T> msgClass : msgClasses) {
            SerializationId serializationId = msgClass.getAnnotation(SerializationId.class);
            if (serializationId != null) {
                if (idToClass.containsKey(serializationId.value())) {
                    throw new Exception();
                }
                idToClass.put(serializationId.value(), msgClass);
            }
        }
        return new MsgFactory<>(idToClass);
    }

    public T deserializeMsg(ByteString byteString) throws SerializationException {
        ByteArrayInputStream buffer = new ByteArrayInputStream(byteString.toArray());
        SerializationDataInput in = new SerializationDataInput(buffer);
        try {
            byte id = in.readByte();
            if (!idToClass.containsKey(id)) {
                throw new SerializationException();
            }
            Constructor<? extends T> msgClass = idToClass.get(id).getDeclaredConstructor();
            T msg = msgClass.newInstance();
            msg.readCpp(in);
            return msg;
        } catch (Exception exception) {
            throw new SerializationException();
        }
    }
}
