package com.parentoop.slave.executor;

import com.parentoop.core.networking.Messages;
import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeClient;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.slave.executor.phases.LoadPhase;
import com.parentoop.slave.executor.phases.Phase;
import com.parentoop.slave.view.Console;

import java.io.IOException;
import java.net.InetAddress;

public class PhaseExecutor implements MessageHandler {

    private final TaskParameters mTaskParameters;
    private boolean mVerbose;
    private NodeClient mMasterConnection;
    private InetAddress mMasterAddress;
    private Phase mPhase;

    public PhaseExecutor(InetAddress masterAddress) {
        mTaskParameters = new TaskParameters();
        mTaskParameters.setExecutor(this);
        mMasterAddress = masterAddress;
    }

    public void initialize() throws IOException {
        Console.println("Setting Master connection as client");
        mMasterConnection = new NodeClient(mMasterAddress, Ports.MASTER_SLAVE_PORT, this);
        mTaskParameters.setMasterConnection(mMasterConnection);
        Console.println("Setting Slave connection as server");
        NodeServer slaveConnection = new NodeServer(Ports.SLAVE_SLAVE_PORT, this);
        mTaskParameters.setSlaveConnection(slaveConnection);
        mPhase = new LoadPhase();
        mPhase.initialize(mTaskParameters);
        dispatchIdleMessage();
    }

    @Override
    public void handle(Message message, PeerCommunicator sender) {
        Console.println("Incomming message: " + message.getCode());
        mPhase.execute(message, sender);
        Phase phase = mPhase.nextPhase();
        if (!phase.equals(mPhase)) {
            mPhase.terminate(mTaskParameters);
            mPhase = phase;
            mPhase.initialize(mTaskParameters);
        }
        dispatchIdleMessage();
    }

    public void dispatchIdleMessage() {
        try {
            mMasterConnection.dispatchMessage(new Message(Messages.IDLE));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
