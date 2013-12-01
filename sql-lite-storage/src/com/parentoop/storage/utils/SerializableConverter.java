package com.parentoop.storage.utils;

import javax.sql.rowset.serial.SerialBlob;
import java.io.*;
import java.sql.Blob;
import java.sql.SQLException;

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

    public static Blob toBlob(Serializable serializable) throws IOException, SQLException {
        return new SerialBlob(toByteArray(serializable));
    }

    public static Object toObject(byte[] byteArray) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteStream = new ByteArrayInputStream(byteArray);
        ObjectInputStream objectStream = new ObjectInputStream(byteStream);
        return objectStream.readObject();
    }

    public static Object toObject(Blob blob) throws SQLException, IOException, ClassNotFoundException {
        return toObject(blob.getBytes(1, (int) blob.length()));
    }
    
}
