package com.parentoop.slave.executor.phases;

import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;
import com.parentoop.core.loader.Task;
import com.parentoop.core.loader.TaskDescriptor;
import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.api.SlaveStorage;
import com.parentoop.slave.executor.TaskParameters;
import com.parentoop.slave.utils.service.ServiceUtils;

import java.io.Serializable;
import java.nio.file.Path;

public class LoadPhase extends Phase {

    private Path mJarPath;
    private TaskDescriptor mDescriptor;
    private Mapper mMapper;
    private Reducer mReducer;

    @Override
    public void initialize(TaskParameters parameters) {
        super.initialize(parameters);
        //noinspection unchecked
        SlaveStorage<Serializable> storage = (SlaveStorage<Serializable>) ServiceUtils.load(SlaveStorage.class);
        parameters.setStorage(storage);
        try {
            storage.initialize();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void terminate(TaskParameters parameters) {
        parameters.setMapper(mMapper);
        parameters.setReducer(mReducer);
        super.terminate(parameters);
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
        dispatchIdleMessage();
        nextPhase(MapPhase.class);
    }
}
