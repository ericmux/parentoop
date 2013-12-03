package com.parentoop.slave.application;

import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.listeners.master.MasterRouter;
import com.parentoop.slave.listeners.slave.SlaveRouter;
import com.parentoop.slave.utils.ServiceUtils;
import com.parentoop.storage.api.SlaveStorage;

import java.io.IOException;

public class SlaveApplication implements Initializable, Finalizable {

    private static final int SLAVE_LISTENS_MASTER_PORT = 13371;
    private static final int SLAVE_LISTENS_SLAVE_PORT = 13372;

    public static class ServiceNotAvailableException extends IllegalStateException { /* No-op */ }

    private static SlaveApplication sInstance;

    public static SlaveApplication getInstance() throws IOException {
        if (sInstance == null) {
            sInstance = new SlaveApplication();
        }
        return sInstance;
    }

    private final SlaveStorage mSlaveStorage;
    private final NodeServer mSlaveListener;
    private final NodeServer mMasterListener;
    private Mapper mMapper;
    private Reducer mReducer;

    private SlaveApplication() throws IOException {
        mSlaveStorage = ServiceUtils.load(SlaveStorage.class);
        mMasterListener = new NodeServer(SLAVE_LISTENS_MASTER_PORT, new MasterRouter());
        mSlaveListener = new NodeServer(SLAVE_LISTENS_SLAVE_PORT, new SlaveRouter());
    }

    @Override
    public void initialize() throws Exception {
        mSlaveStorage.initialize();
        mMasterListener.startServer();
        mSlaveListener.startServer();
    }

    @Override
    public void terminate() throws Exception {
        mSlaveListener.shutdown();
        mMasterListener.shutdown();
        mSlaveStorage.terminate();
    }

    public void sendKeyValues(String key, PeerCommunicator sender) throws IOException {
        // TODO: Use common message types from Core module
        for (Object value : mSlaveStorage.read(key)) {
            sender.dispatchMessage(new Message(-1, value));
        }
        sender.dispatchMessage(new Message(-1));
    }

    public SlaveStorage getSlaveStorage() {
        return mSlaveStorage;
    }

    public void setMapper(Mapper mapper) {
        mMapper = mapper;
    }

    public void setReducer(Reducer reducer) {
        mReducer = reducer;
    }
}
