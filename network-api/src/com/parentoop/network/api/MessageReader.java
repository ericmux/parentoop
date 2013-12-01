package com.parentoop.network.api;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;

public interface MessageReader {

    public void read(MessageType type, ObjectInputStream inputStream) throws IOException, ClassNotFoundException;

}
