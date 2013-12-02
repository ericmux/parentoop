package com.parentoop.network.api.messaging;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public class MessageRouter implements MessageReader {

    private Map<MessageType, MessageHandler> mHub = new HashMap<>();
    private MessageHandler mDefaultHandler;

    @Override
    public void read(MessageType type, ObjectInputStream inputStream, InetAddress senderAddress) throws IOException, ClassNotFoundException {
        MessageHandler handler = mHub.get(type);
        if (handler == null) handler = mDefaultHandler;
        if (handler == null) {
            throw new MissingMessageHandlerException("There is no message handler registered to message type " + type);
        }
        handler.handle(type, inputStream, senderAddress);
    }

    public void register(MessageType type, MessageHandler handler) {
        mHub.put(type, handler);
    }

    public void unregister(MessageType type) {
        mHub.remove(type);
    }

    public void registerDefault(MessageHandler handler) {
        mDefaultHandler = handler;
    }

    public void unregisterDefault() {
        mDefaultHandler = null;
    }

}
