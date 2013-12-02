package com.parentoop.network.api.messaging;

import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

public interface MessageHandler {

    public void handle(Message message, PeerCommunicator sender);
}
