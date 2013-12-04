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
        int code = message.getCode();
        MessageHandler handler = mHub.get(code);
        if (handler == null) handler = mDefaultHandler;
        if (handler == null) {
            throw new MissingMessageHandlerException("There is no message handler registered to message type " + code);
        }
        handler.handle(message, sender);
    }

    public void registerHandler(int messageCode, MessageHandler handler) {
        mHub.put(messageCode, handler);
    }

    public void unregisterHandler(MessageHandler messageHandler) {
        mHub.values().remove(messageHandler);
    }

    public void unregisterHandlerFor(int messageCode) {
        mHub.remove(messageCode);
    }

    public void registerDefaultHandler(MessageHandler handler) {
        mDefaultHandler = handler;
    }

    public void unregisterDefaultHandler() {
        mDefaultHandler = null;
    }
}
