package com.parentoop.slave.executor.phases;

import com.parentoop.core.api.Mapper;
import com.parentoop.core.data.Datum;
import com.parentoop.core.data.NetworkDataPool;
import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.slave.api.SlaveStorage;
import com.parentoop.slave.executor.TaskParameters;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MapPhase extends Phase {

    private static final int MAP_THREADS = 5;
    private static final long MAX_TIME_ALLOWED_IN_SECONDS = 60 * 60 * 24 * 7;

    private final ExecutorService mMappersThreadPool = Executors.newFixedThreadPool(MAP_THREADS);
    private NetworkDataPool mDataPool = new NetworkDataPool();
    private Thread mDataPersistorThread = null;
    private Set<String> mKeysFound = new HashSet<>();
    private Mapper mMapper;
    private SlaveStorage mStorage;

    @Override
    public void initialize(TaskParameters parameters) {
        super.initialize(parameters);
        mMapper = parameters.getMapper();
        mStorage = parameters.getStorage();
    }

    @Override
    public void execute(Message message, PeerCommunicator sender) {
        switch (message.getCode()) {
            case Messages.MAP_CHUNK:
                if (mDataPersistorThread == null) startDataPersistor();
                Runnable task = new MapTask(message.<Serializable>getData());
                mMappersThreadPool.submit(task);
                dispatchIdleMessage();
                break;
            case Messages.END_MAP:
                endMap();
                nextPhase(ReducePhase.class);
                dispatchIdleMessage();
                break;
        }
    }

    private void endMap() {
        try {
            mMappersThreadPool.shutdown();
            mMappersThreadPool.awaitTermination(MAX_TIME_ALLOWED_IN_SECONDS, TimeUnit.SECONDS);
            mDataPool.close();
            mDataPersistorThread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private class MapTask implements Runnable {

        private final Serializable mChunk;

        private MapTask(Serializable chunk) {
            mChunk = chunk;
        }

        @Override
        public void run() {
            //noinspection unchecked
            mMapper.map(mChunk, mDataPool);
        }
    }

    private void startDataPersistor() {
        mDataPersistorThread = new Thread(new Runnable() {
            public void run() {
                for (Datum datum : mDataPool) {
                    String key = datum.getKey();
                    if (!mKeysFound.contains(key)) {
                        mKeysFound.add(key);
                        dispatchMessageToMaster(new Message(Messages.KEY_FOUND, key));
                    }
                    //noinspection unchecked
                    mStorage.insert(key, datum.getValue());
                }
            }
        });
        mDataPersistorThread.start();
    }
}
