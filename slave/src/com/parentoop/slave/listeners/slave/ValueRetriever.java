package com.parentoop.slave.listeners.slave;

import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.network.api.messaging.MessageType;
import com.parentoop.slave.application.SlaveApplication;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;

public class ValueRetriever implements MessageHandler {

    @Override
    public void handle(MessageType messageType, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException {
        String key = (String) inputStream.readObject();
        SlaveApplication.getInstance().sendKeyValues(key, senderAddress);
    }
}
