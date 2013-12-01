package com.parentoop.network.api;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class NodeServer {

    // threshold on the number of simultaneous client/server socket connections.
    private static final int DEFAULT_BACKLOG = 200;
    public static final int DEFAULT_PORT = 13371;

    private ServerSocket mServerSocket;
    private Runnable mListeningTask;
    private HashSet<PeerHandler> mPeerHandlers;
    private MessageReader mMessageReader;
    private HashMap<InetAddress, Socket> mDispatcherSockets;
    private final ExecutorService mExecutorService = Executors.newFixedThreadPool(DEFAULT_BACKLOG);

    public NodeServer(int serverPort, MessageReader messageReader, int backlog) throws IOException {
        mServerSocket = new ServerSocket(serverPort, backlog);
        mPeerHandlers = new HashSet<>();
        mDispatcherSockets = new HashMap<>();
        mMessageReader = messageReader;
    }

    public NodeServer(int serverPort, MessageReader messageReader) throws IOException{
        mServerSocket = new ServerSocket(serverPort, DEFAULT_BACKLOG);
        mPeerHandlers = new HashSet<>();
        mDispatcherSockets = new HashMap<>();
        mMessageReader = messageReader;
    }

    public void startServer() {
        mListeningTask = new Runnable() {
            @Override
            public void run() {
                while (!mServerSocket.isClosed()) {
                    try {
                        Socket socket = mServerSocket.accept();
                        PeerHandler peerHandler = new PeerHandler(socket);
                        mPeerHandlers.add(peerHandler);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        };
        mExecutorService.submit(mListeningTask);
    }

    public void dispatchMessage(MessageType type, InetAddress peerAddress, Serializable message) throws IOException{
        Socket dispatchSocket;
        if(!mDispatcherSockets.containsKey(peerAddress))
            dispatchSocket = new Socket(peerAddress, DEFAULT_PORT);
        else dispatchSocket = mDispatcherSockets.get(peerAddress);
        mDispatcherSockets.put(dispatchSocket.getInetAddress(), dispatchSocket);
        OutputStream sos = dispatchSocket.getOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(sos);
        oos.writeInt(type.getId());
        oos.writeObject(message);
        oos.flush();
    }

    public Collection<InetAddress> getConnectedPeers(){
        Collection<InetAddress> peers = new HashSet<>();
        for(PeerHandler peerHandler : mPeerHandlers) peers.add(peerHandler.getSocket().getInetAddress());
        return peers;
    }

    public InetAddress getServerAddress() {
        return mServerSocket.getInetAddress();
    }

    public void closeDispatching(InetAddress peerAddress) throws IOException{
        Socket dispatchSocket = mDispatcherSockets.get(peerAddress);
        dispatchSocket.close();
    }

    public void closeReading() throws IOException{
        for(PeerHandler peer : mPeerHandlers) {
            peer.closeConnection();
        }
    }

    public void shutDownServer() throws IOException {
        mExecutorService.shutdown();
        mServerSocket.close();
    }

    /**
     * Inner class aiming to handle reading from remote machines.
     */
    private class PeerHandler {

        private Socket mSocket;
        private ObjectInputStream mInputStream;
        private ObjectOutputStream mOutputStream;
        private Runnable mListenToPeerTask;
        private Future<?> mPendingListeningTask;

        public PeerHandler(Socket socket) throws IOException {
            mSocket = socket;
            mOutputStream = new ObjectOutputStream(new BufferedOutputStream(socket.getOutputStream()));
            mInputStream = new ObjectInputStream(new BufferedInputStream(socket.getInputStream()));
            mOutputStream.flush();
            mListenToPeerTask = new Runnable() {
                @Override
                public void run() {
                    while (!mSocket.isClosed() && !mSocket.isInputShutdown() && !Thread.interrupted()) {
                        try {
                            int code = mInputStream.readInt();
                            MessageType type = MessageType.fromId(code);
                            mMessageReader.read(type, mInputStream);
                            mInputStream.close();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            };
            mPendingListeningTask = mExecutorService.submit(mListenToPeerTask);
        }

        public void closeConnection() throws IOException {
            mPendingListeningTask.cancel(true);
            mInputStream.close();
            mSocket.close();
        }

        private Socket getSocket() {
            return mSocket;
        }

    }

}