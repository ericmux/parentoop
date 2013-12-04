package com.parentoop.network.api.messaging;

import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.util.HashMap;
import java.util.Map;

public class MessageRouter implements MessageHandler {

    private Map<Integer, MessageHandler> mHub = new HashMap<>();
    private MessageHandler mDefaultHandler;

    @Override
    public void handle(Message message, PeerCommunicator sender) {
        int type = message.getType();
        MessageHandler handler = mHub.get(type);
        if (handler == null) handler = mDefaultHandler;
        if (handler == null) {
            throw new MissingMessageHandlerException("There is no message handler registered to message type " + type);
        }
        handler.handle(message, sender);
    }

    public void registerHandler(int messageCode, MessageHandler handler) {
        mHub.put(messageCode, handler);
    }

    public void unregisterHandler(MessageHandler messageHandler) {
        mHub.values().remove(messageHandler);
    }

    public void unregisterHandlerFor(Integer messageType) {
        mHub.remove(messageType);
    }

    public void registerDefaultHandler(MessageHandler handler) {
        mDefaultHandler = handler;
    }

    public void unregisterDefaultHandler() {
        mDefaultHandler = null;
    }
}
