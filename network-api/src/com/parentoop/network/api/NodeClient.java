package com.parentoop.network.api;

import com.parentoop.network.api.messaging.MessageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NodeClient extends PeerCommunicator {

    private MessageHandler mMessageHandler;

    public NodeClient(InetAddress address, int port, MessageHandler messageHandler) throws IOException {
        this(address, port, messageHandler, Executors.newSingleThreadScheduledExecutor());
    }

    public NodeClient(InetAddress address, int port,
                      MessageHandler messageHandler,
                      ScheduledExecutorService executorService) throws IOException {
        super(new Socket(address, port), executorService);
        mMessageHandler = messageHandler;
    }

    @Override
    protected void handleMessage(Message message) {
        mMessageHandler.handle(message, this);
    }

    public void shutdown() throws IOException {
        super.shutdown();
    }

    public boolean isConnected() {
        return mSocket.isConnected() && !mSocket.isClosed();
    }

    public boolean isShutdown() {
        return mSocket.isClosed();
    }
}
