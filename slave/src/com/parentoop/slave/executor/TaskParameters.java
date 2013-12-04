package com.parentoop.slave.executor;

import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.network.api.NodeClient;
import com.parentoop.network.api.NodeServer;
import com.parentoop.slave.api.SlaveStorage;

import java.io.Serializable;

public final class TaskParameters {

    private Mapper mMapper;
    private Reducer mReducer;
    private SlaveStorage<Serializable> mStorage;
    private PhaseExecutor mExecutor;
    private NodeClient mMasterConnection;
    private NodeServer mSlaveConnection;
    private boolean mVerbose;

    public Mapper getMapper() {
        return mMapper;
    }

    public void setMapper(Mapper mapper) {
        mMapper = mapper;
    }

    public Reducer getReducer() {
        return mReducer;
    }

    public void setReducer(Reducer reducer) {
        mReducer = reducer;
    }

    public SlaveStorage<Serializable> getStorage() {
        return mStorage;
    }

    public void setStorage(SlaveStorage<Serializable> storage) {
        mStorage = storage;
    }

    public NodeClient getMasterConnection() {
        return mMasterConnection;
    }

    public void setMasterConnection(NodeClient masterConnection) {
        mMasterConnection = masterConnection;
    }

    public NodeServer getSlaveConnection() {
        return mSlaveConnection;
    }

    public void setSlaveConnection(NodeServer slaveConnection) {
        mSlaveConnection = slaveConnection;
    }

    public PhaseExecutor getExecutor() {
        return mExecutor;
    }

    public void setExecutor(PhaseExecutor executor) {
        mExecutor = executor;
    }

}
