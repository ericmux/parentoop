package com.parentoop.network.api;

import com.parentoop.network.api.messaging.MessageHandler;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

public class NodeServer {

    // threshold on the number of simultaneous client/server socket connections.
    private static final int DEFAULT_BACKLOG = 200;
    private static final int THREAD_POOL_SIZE = 5;

    private int mConnectionPort;
    private int mBacklog;

    private ServerSocket mServerSocket;
    private MessageHandler mMessageHandler;
    private ScheduledExecutorService mExecutorService;

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
        if (mServerSocket != null) throw new IllegalStateException("Server already started");

        mServerSocket = new ServerSocket(mConnectionPort, mBacklog);
        mExecutorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        mExecutorService.submit(new ListeningRunnable());
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
        if (mServerSocket == null) throw new IllegalStateException("Attempting shutdown on non started server");

        for (PeerHandler peerHandler : mPeerHandlers.values()) {
            peerHandler.shutdown();
        }
        mExecutorService.shutdown();
        mServerSocket.close();
    }

    public boolean isStarted() {
        return mServerSocket != null;
    }

    public boolean isRunning() {
        return isStarted() && !mServerSocket.isClosed();
    }

    public boolean isShutdown() {
        return isStarted() && mServerSocket.isClosed();
    }

    private class ListeningRunnable implements Runnable {

        @Override
        public void run() {
            while (!mServerSocket.isClosed()) {
                try {
                    Socket socket = mServerSocket.accept();
                    PeerHandler peerHandler = new PeerHandler(socket);
                    mPeerHandlers.put(socket.getInetAddress(), peerHandler);
                } catch (SocketException ex) {
                    String excMessage = ex.getMessage();
                    if (excMessage == null) excMessage = "";
                    if (mServerSocket.isClosed() || excMessage.contains("closed")) {
                        try {
                            shutdown();
                        } catch (IOException e) {
                            // we are shutting down anyway, eat
                        }
                    } else {
                        ex.printStackTrace();
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        }
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
        protected void handleMessage(Message message) {
            mMessageHandler.handle(message, this);
        }
    }
}