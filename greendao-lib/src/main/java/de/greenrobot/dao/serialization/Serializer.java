package de.greenrobot.dao.serialization;

import java.io.IOException;

public interface Serializer {
    byte[] serializeObject(Object object) throws IOException;

    Object deserializeObject(byte[] byteArray) throws IOException, ClassNotFoundException;
}
