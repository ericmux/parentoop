package com.parentoop.master.application.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.parentoop.core.api.InputReader;
import com.parentoop.core.data.DataPool;
import com.parentoop.core.networking.Messages;
import com.parentoop.master.execution.ExecutionPhase;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MappingPhase extends ExecutionPhase<Path> {

    private static final int MAX_POOL_SIZE = 15;

    private ExecutorService mExecutorService;

    private Path mInputPath;
    private Iterator<Serializable> mInputIterator;
    private boolean mFinishedInput;

    private Set<PeerCommunicator> mIdleSlaves;

    private BlockingQueue<PeerCommunicator> mChunkSendingQueue;
    private AtomicInteger mActiveChunkSendingThreads;
    private Set<String> mFoundKeys;

    public MappingPhase(Path inputPath) {
        mInputPath = inputPath;
    }

    @Override
    public int getPhaseCode() {
        return Messages.MAPPING;
    }

    @Override
    public Collection<Integer> getHandledMessageCodes() {
        return Lists.newArrayList(Messages.IDLE, Messages.KEY_FOUND);
    }

    @Override
    public void onEnterPhase(ExecutionPhase previousPhase) {
        int chunkSenderThreads = Math.min(MAX_POOL_SIZE, getParticipatingPeers().size());
        mExecutorService = Executors.newFixedThreadPool(chunkSenderThreads + 1); // +1 for the input reader one
        mIdleSlaves = Sets.newHashSet();
        mFoundKeys = Sets.newConcurrentHashSet();
        mFinishedInput = false;

        final DataPool<Serializable> inputDataPool = new DataPool<>();
        mInputIterator = inputDataPool.iterator();
        mExecutorService.submit(new Runnable() {
            @Override
            public void run() {
                InputReader inputReader = getTask().getInputReader();
                inputReader.read(mInputPath, inputDataPool);
                inputDataPool.close();
            }
        });

        mActiveChunkSendingThreads = new AtomicInteger(0);
        mChunkSendingQueue = new LinkedBlockingQueue<>(getParticipatingPeers());
        while (chunkSenderThreads --> 0) {
            mExecutorService.submit(new ChunkSendingRunnable());
        }
    }

    @Override
    public void handleMessage(Message message, PeerCommunicator sender) throws Exception {
        switch (message.getCode()) {
            case Messages.IDLE:
                if (mIdleSlaves.contains(sender)) return;
                mIdleSlaves.add(sender);
                if (mFinishedInput && mIdleSlaves.containsAll(getParticipatingPeers())) {
                    goToNextPhase();
                }
                break;
            case Messages.KEY_FOUND:
                String key = message.getData();
                mFoundKeys.add(key);
                break;
        }
    }

    @Override
    public void onExitPhase(ExecutionPhase nextPhase) throws IOException {
        mExecutorService.shutdownNow();
        mChunkSendingQueue.clear();
        if (nextPhase instanceof ReducingPhase) {
            ((ReducingPhase) nextPhase).setKeysToReduce(mFoundKeys);
        }
    }

    private class ChunkSendingRunnable implements Callable<Void> {

        @Override
        public Void call() throws Exception {
            if (!mInputIterator.hasNext()) return null;
            mActiveChunkSendingThreads.getAndIncrement();
            try {
                while (true) {
                    PeerCommunicator peer = mChunkSendingQueue.take();
                    Serializable chunk;
                    try {
                        chunk = mInputIterator.next();
                    } catch (RuntimeException ex) {
                        if (!mInputIterator.hasNext()) break;
                        continue;
                    }
                    peer.dispatchMessage(new Message(Messages.MAP_CHUNK, chunk));
                    mChunkSendingQueue.put(peer);
                }
                return null;
            } finally {
                int activeThreads = mActiveChunkSendingThreads.decrementAndGet();
                if (activeThreads == 0) {
                    mFinishedInput = true;
                    mIdleSlaves.clear();
                    for (PeerCommunicator peer : getParticipatingPeers()) {
                        peer.dispatchMessage(new Message(Messages.END_OF_CHUNK_STREAM));
                    }
                }
            }
        }
    }
}
