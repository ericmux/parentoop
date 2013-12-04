package com.parentoop.slave.node.phases;

import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.core.networking.Messages;
import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeClient;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.api.SlaveStorage;
import com.parentoop.slave.node.PhaseExecutor;

import java.io.IOException;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.HashMap;
import java.util.Map;

public abstract class Phase {

    private Class<? extends Phase> mNextPhaseClass;
    private Map<InetAddress, NodeClient> mPeers = new HashMap<>();
    protected NodeServer mSlaveConnection;
    protected PhaseExecutor mExecutor;
    protected NodeClient mMasterConnection;
    protected SlaveStorage<Serializable> mStorage;
    protected Reducer mReducer;
    protected Mapper mMapper;

    public abstract void execute(Message message, PeerCommunicator sender);

    public Phase nextPhase() {
        if (mNextPhaseClass.equals(getClass())) return this;
        try {
            Phase phase = mNextPhaseClass.newInstance();
            phase.mMapper = mMapper;
            phase.mReducer = mReducer;
            phase.mStorage = mStorage;
            phase.mMasterConnection = mMasterConnection;
            phase.mExecutor = mExecutor;
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

    public void dispatchIdleMessage() {
        try {
            mMasterConnection.dispatchMessage(new Message(Messages.IDLE));
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
