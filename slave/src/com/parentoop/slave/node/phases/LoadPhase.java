package com.parentoop.slave.node.phases;

import com.parentoop.core.loader.Task;
import com.parentoop.core.loader.TaskDescriptor;
import com.parentoop.core.networking.Messages;
import com.parentoop.core.networking.Ports;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeClient;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.api.SlaveStorage;
import com.parentoop.slave.node.PhaseExecutor;
import com.parentoop.slave.utils.service.ServiceUtils;

import java.io.Serializable;
import java.net.InetAddress;
import java.nio.file.Path;

public class LoadPhase extends Phase {

    private Path mJarPath;
    private TaskDescriptor mDescriptor;

    public LoadPhase(PhaseExecutor executor) throws Exception {
        super();
        mExecutor = executor;
        mMasterConnection = new NodeClient(InetAddress.getLocalHost(), Ports.MASTER_SLAVE_PORT, executor);
        mSlaveConnection = new NodeServer(Ports.SLAVE_SLAVE_PORT, executor);
        //noinspection unchecked
        mStorage = (SlaveStorage<Serializable>) ServiceUtils.load(SlaveStorage.class);
        mStorage.initialize();
    }

    public LoadPhase() throws Exception {
        super();
        mStorage.initialize();
    }

    @Override
    public void execute(Message message, PeerCommunicator sender) {
        switch (message.getCode()) {
            case Messages.LOAD_JAR:
                mJarPath = message.getData();
                break;
            case Messages.LOAD_DESCRIPTOR:
                mDescriptor = message.getData();
                break;
        }
        loadTask();
    }

    private void loadTask() {
        if (mJarPath == null || mDescriptor == null) return;
        Task task = Task.load(mJarPath, mDescriptor);
        mMapper = task.getMapper();
        mReducer = task.getReducer();
        nextPhase(MapPhase.class);
    }
}
