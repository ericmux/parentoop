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

    public void register(Integer messageType, MessageHandler handler) {
        mHub.put(messageType, handler);
    }

    public void unregister(Integer messageType) {
        mHub.remove(messageType);
    }

    public void registerDefault(MessageHandler handler) {
        mDefaultHandler = handler;
    }

    public void unregisterDefault() {
        mDefaultHandler = null;
    }
}
