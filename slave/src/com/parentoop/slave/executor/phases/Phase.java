package com.parentoop.slave.executor.phases;

import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeClient;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.executor.PhaseExecutor;
import com.parentoop.slave.executor.TaskParameters;

import java.io.IOException;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class Phase {

    private Class<? extends Phase> mNextPhaseClass;
    private Map<InetAddress, NodeClient> mPeers = new HashMap<>();
    protected PhaseExecutor mExecutor;
    protected NodeClient mMasterConnection;

    public void initialize(TaskParameters parameters) {
        mMasterConnection = parameters.getMasterConnection();
        mExecutor = parameters.getExecutor();
    }

    public void terminate(TaskParameters parameters) {
        /* No-op */
    }

    public abstract void execute(Message message, PeerCommunicator sender);

    public Phase nextPhase() {
        if (mNextPhaseClass.equals(getClass())) return this;
        try {
            Phase phase = mNextPhaseClass.newInstance();
            return phase;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    protected <T extends Phase> void nextPhase(Class<T> phaseClass) {
        mNextPhaseClass = phaseClass;
    }

    protected void dispatchMessageToSlave(InetAddress slaveAddress, Message message) {
        try {
            NodeClient nodeClient = mPeers.get(slaveAddress);
            if (nodeClient == null) {
                nodeClient = new NodeClient(slaveAddress, Ports.SLAVE_SLAVE_PORT, mExecutor);
                mPeers.put(slaveAddress, nodeClient);
            }
            nodeClient.dispatchMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void dispatchMessageToMaster(Message message) {
        try {
            mMasterConnection.dispatchMessage(message);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void closeSlaves() {
        for (NodeClient slave : mPeers.values()) {
            try {
                slave.shutdown();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
