package com.parentoop.network.api.messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public interface MessageHandler {

    public void handle(MessageType messageType, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException;

}
