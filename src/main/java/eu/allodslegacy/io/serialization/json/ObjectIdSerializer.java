package eu.allodslegacy.io.serialization.json;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.bson.types.ObjectId;

import java.io.IOException;

public class ObjectIdSerializer extends JsonSerializer<ObjectId> {

    @Override
    public void serialize(ObjectId id, JsonGenerator generator, SerializerProvider provider) throws IOException {

        if (id == null) {
            generator.writeNull();
        } else {
            generator.writeString(id.toString());
        }

    }

}
