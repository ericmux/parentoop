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

    private final NodeServer mMasterClientServer;
    private final NodeServer mMasterSlaveServer;

    private final MessageRouter mClientMessageRouter = new MessageRouter();
    private final MessageRouter mSlaveMessageRouter = new MessageRouter();

    private Path mJarPath;
    private Path mInputPath;
    private TaskExecution mTaskExecution;

    public MasterApplication() {
        mMasterClientServer = new NodeServer(Ports.MASTER_CLIENT_PORT, mClientMessageRouter, 1);
        mMasterSlaveServer = new NodeServer(Ports.MASTER_SLAVE_PORT, mSlaveMessageRouter);
    }

    public void start() throws IOException {
        mClientMessageRouter.registerDefaultHandler(new ClientMessageHandler());
        mSlaveMessageRouter.registerDefaultHandler(new SlaveMessageHandler());

        mMasterClientServer.startServer();
        mMasterSlaveServer.startServer();
    }

    private void startTask(PeerCommunicator sender, String configuratorName) throws IOException {
        if (mTaskExecution != null) {
            sender.dispatchMessage(new Message(Messages.FAILURE, "Application busy. Currently running another task."));
            return;
        }
        Task task = Task.load(mJarPath, configuratorName);
        if (task == null) {
            sender.dispatchMessage(new Message(Messages.FAILURE, "Failed to load task."));
            return;
        }
        TaskExecution taskExecution = new MapReduceTaskExecution(mInputPath, task, this);
        taskExecution.setSlaveServer(mMasterSlaveServer, mSlaveMessageRouter);
        taskExecution.start();
        mTaskExecution = taskExecution;
    }

    @Override
    public void onEnterExecutionPhase(int code) {

    }

    @Override
    public void onExecutionFailed(Throwable throwable) {

    }

    @Override
    public void onExecutionSuccessful(Path result) {

    }

    private class ClientMessageHandler implements MessageHandler {

        @Override
        public void handle(Message message, PeerCommunicator sender) {
            switch (message.getCode()) {
                case Messages.LOAD_JAR:
                    mJarPath = message.getData();
                    break;
                case Messages.LOAD_INPUT_PATH:
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
            }
        }
    }

    private class SlaveMessageHandler implements MessageHandler {

        @Override
        public void handle(Message message, PeerCommunicator sender) {
        }
    }
}
