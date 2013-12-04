package com.parentoop.slave.node.phases;

import com.parentoop.core.data.Datum;
import com.parentoop.core.data.NetworkDataPool;
import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

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

    @Override
    public void execute(Message message, PeerCommunicator sender) {
        switch (message.getCode()) {
            case Messages.MAP_CHUNK:
                if (mDataPersistorThread == null) startDataPersistor();
                Runnable task = new MapTask(message.<Serializable>getData());
                mMappersThreadPool.submit(task);
                break;
            case Messages.END_MAP:
                try {
                    mMappersThreadPool.shutdown();
                    mMappersThreadPool.awaitTermination(MAX_TIME_ALLOWED_IN_SECONDS, TimeUnit.SECONDS);
                    mDataPool.close();
                    mDataPersistorThread.join();
                    nextPhase(ReducePhase.class);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                break;
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
                    mStorage.insert(key, datum.getValue());
                }
            }
        });
        mDataPersistorThread.start();
    }
}
