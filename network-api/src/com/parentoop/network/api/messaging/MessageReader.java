package com.parentoop.network.api.messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public interface MessageReader {

    public void read(MessageType type, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException;

}
