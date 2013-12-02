package com.parentoop.slave.listeners.slave;

import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.slave.application.SlaveApplication;

import java.io.IOException;

public class ValueRetriever implements MessageHandler {

    @Override
    public void handle(Message message, PeerCommunicator sender) {
        String key = message.getData();
        try {
            SlaveApplication.getInstance().sendKeyValues(key, sender);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
