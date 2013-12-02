package com.parentoop.slave.listeners.master;

import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.network.api.messaging.MessageType;

import java.io.ObjectInputStream;
import java.net.InetAddress;

public class MapChunk implements MessageHandler {

    @Override
    public void handle(MessageType messageType, ObjectInputStream inputStream, InetAddress senderAddress) {

    }

}
