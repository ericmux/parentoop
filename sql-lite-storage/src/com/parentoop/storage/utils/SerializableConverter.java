package com.parentoop.storage.utils;

import java.io.*;

public class SerializableConverter {

    // Prevents instantiation
    private SerializableConverter() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
    
    public static byte[] toByteArray(Serializable serializable) throws IOException {
        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
        ObjectOutputStream objectStream = new ObjectOutputStream(byteStream);
        objectStream.writeObject(serializable);
        return byteStream.toByteArray();
    }

    public static <T> T toObject(byte[] byteArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        //noinspection unchecked
        return (T) objectStream.readObject();
    }
    
}
