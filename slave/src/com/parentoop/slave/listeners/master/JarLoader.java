package com.parentoop.slave.listeners.master;

import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;

import java.io.File;

public class JarLoader implements MessageHandler {

    @Override
    public void handle(Message message, PeerCommunicator sender) {
        File jarFile = message.getData();

    }
}
