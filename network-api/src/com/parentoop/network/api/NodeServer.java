package com.parentoop.network.api;

import com.parentoop.network.api.messaging.MessageHandler;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.*;

public class NodeServer {

    // threshold on the number of simultaneous client/server socket connections.
    private static final int DEFAULT_BACKLOG = 200;
    private static final int THREAD_POOL_SIZE = 5;

    private int mConnectionPort;
    private int mBacklog;

    private ServerSocket mServerSocket;
    private MessageHandler mMessageHandler;
    private final ScheduledExecutorService mExecutorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);

    private HashMap<InetAddress, PeerHandler> mPeerHandlers = new HashMap<>();

    public NodeServer(int connectionPort, MessageHandler messageHandler) throws IOException {
        this(connectionPort, messageHandler, DEFAULT_BACKLOG);
    }

    public NodeServer(int connectionPort, MessageHandler messageHandler, int backlog) {
        mConnectionPort = connectionPort;
        mBacklog = backlog;
        mMessageHandler = messageHandler;
    }

    public void startServer() throws IOException {
        if (mServerSocket != null) throw new RuntimeException("Server already started");

        mServerSocket = new ServerSocket(mConnectionPort, mBacklog);
        Runnable listeningTask = new Runnable() {
            @Override
            public void run() {
                while (!mServerSocket.isClosed()) {
                    try {
                        Socket socket = mServerSocket.accept();
                        PeerHandler peerHandler = new PeerHandler(socket);
                        mPeerHandlers.put(socket.getInetAddress(), peerHandler);
                    } catch (IOException e) {
                        if (!(e instanceof SocketException && e.getMessage().contains("socket closed"))) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        };
        mExecutorService.submit(listeningTask);
    }

    public void broadcastMessage(Message message) throws IOException {
        for (PeerHandler peerHandler : getConnectedPeers()) {
            peerHandler.dispatchMessage(message);
        }
    }

    public Collection<PeerHandler> getConnectedPeers(){
        return Collections.unmodifiableCollection(mPeerHandlers.values());
    }

    public void shutdown() throws IOException {
        for (PeerHandler peerHandler : mPeerHandlers.values()) {
            peerHandler.shutdown();
        }
        mExecutorService.shutdown();
        mServerSocket.close();
    }

    /**
     * Inner class aiming to handle reading from remote machines.
     */
    private class PeerHandler extends PeerCommunicator {

        public PeerHandler(Socket socket) throws IOException {
            super(socket, mExecutorService);
        }

        protected void shutdown() throws IOException {
            try {
                super.shutdown();
            } finally {
                mPeerHandlers.values().remove(this);
            }
        }

        @Override
        void handleMessage(Message message) {
            mMessageHandler.handle(message, this);
        }
    }
}