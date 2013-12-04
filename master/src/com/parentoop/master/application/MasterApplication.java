package com.parentoop.master.application;

import com.parentoop.core.loader.Task;
import com.parentoop.core.networking.Messages;
import com.parentoop.core.networking.Ports;
import com.parentoop.master.execution.TaskExecution;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.network.api.messaging.MessageRouter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

public class MasterApplication implements TaskExecution.TaskExecutionListener<Path> {

    private final boolean mVerboseMode;

    private final NodeServer mMasterClientServer;
    private final NodeServer mMasterSlaveServer;

    private final MessageRouter mClientMessageRouter = new MessageRouter();
    private final MessageRouter mSlaveMessageRouter = new MessageRouter();

    private Path mJarPath;
    private Path mInputPath;
    private TaskExecution mTaskExecution;

    public MasterApplication(boolean verboseMode) {
        mVerboseMode = verboseMode;

        mMasterClientServer = new NodeServer(Ports.MASTER_CLIENT_PORT, mClientMessageRouter, 1);
        mMasterClientServer.setPeerConnetionListener(new ClientConnectionListener());

        mMasterSlaveServer = new NodeServer(Ports.MASTER_SLAVE_PORT, mSlaveMessageRouter);
        mMasterSlaveServer.setPeerConnetionListener(new SlaveConnectionListener());
    }

    public void start() throws IOException {
        mClientMessageRouter.registerDefaultHandler(new ClientMessageHandler());
        mSlaveMessageRouter.registerDefaultHandler(new SlaveMessageHandler());

        mMasterClientServer.startServer();
        mMasterSlaveServer.startServer();
    }

    public void shutdown() throws IOException {
        if (mTaskExecution != null) mTaskExecution.abort();
        mMasterSlaveServer.shutdown();
        mMasterClientServer.shutdown();
    }

    private void startTask(PeerCommunicator sender, String configuratorName) throws IOException {
        debug("Attempting task load.");
        if (mTaskExecution != null) {
            sender.dispatchMessage(new Message(Messages.FAILURE, "Application busy. Currently running another task."));
            debug("Task starting failed. Application busy.");
            return;
        }
        Task task = Task.load(mJarPath, configuratorName);
        if (task == null) {
            sender.dispatchMessage(new Message(Messages.FAILURE, "Failed to load task."));
            debug("Task starting failed. Loading failure.");
            return;
        }
        debug("Task named \"" + task.getDescriptor().getTaskName() + "\" loaded. Starting execution.");
        TaskExecution taskExecution = new MapReduceTaskExecution(mInputPath, task, this);
        taskExecution.setSlaveServer(mMasterSlaveServer, mSlaveMessageRouter);
        taskExecution.start();
        mTaskExecution = taskExecution;
        debug("Task started and running.");
    }

    @Override
    public void onEnterExecutionPhase(int code) {
        try {
            debug("Entered execution phase with code " + code);
            mMasterClientServer.broadcastMessage(new Message(code));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExecutionFailed(Throwable throwable) {
        try {
            debug("Task execution failed with message: " + throwable.getMessage());
            throwable.printStackTrace();
            mTaskExecution = null;
            mMasterClientServer.broadcastMessage(new Message(Messages.FAILURE, throwable));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onExecutionSuccessful(Path result) {
        try {
            debug("Task completed successfully. Path to result file: " + result);
            mTaskExecution = null;
            mMasterClientServer.broadcastMessage(new Message(Messages.SEND_RESULT, result));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void debug(String message) {
        if (!mVerboseMode) return;
        System.out.println(message);
    }

    private class ClientMessageHandler implements MessageHandler {

        @Override
        public void handle(Message message, PeerCommunicator sender) {
            switch (message.getCode()) {
                case Messages.LOAD_JAR:
                    debug("JAR file received from client.");
                    mJarPath = message.getData();
                    break;
                case Messages.LOAD_INPUT_PATH:
                    debug("Input path received from client.");
                    mInputPath = new File(message.<String>getData()).toPath();
                    break;
                case Messages.START_TASK:
                    String configuratorName = message.getData();
                    try {
                        startTask(sender, configuratorName);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    debug("Unhandled message with code " + message.getCode() + " received from client.");
                    break;
            }
        }
    }

    private class SlaveMessageHandler implements MessageHandler {

        @Override
        public void handle(Message message, PeerCommunicator sender) {
            if (mVerboseMode) {
                System.out.println("Unhandled message with code " + message.getCode() +
                        " received from slave with IP address " + sender.getAddress().getHostAddress());
            }
        }
    }

    private class SlaveConnectionListener implements NodeServer.PeerConnetionListener {

        @Override
        public void onPeerConnected(PeerCommunicator peer) {
            System.out.println("Slave with IP " + peer.getAddress().getHostAddress() + " has just connected.");
        }

        @Override
        public void onPeerDisconnected(PeerCommunicator peer) {
            System.out.println("Slave with IP " + peer.getAddress().getHostAddress() + " has disconnected.");
        }
    }

    private class ClientConnectionListener implements NodeServer.PeerConnetionListener {

        @Override
        public void onPeerConnected(PeerCommunicator peer) {
            System.out.println("Client connected with IP " + peer.getAddress().getHostAddress());
        }

        @Override
        public void onPeerDisconnected(PeerCommunicator peer) {
            System.out.println("Client disconnected with IP " + peer.getAddress().getHostAddress());
        }
    }
}
