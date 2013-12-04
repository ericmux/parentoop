package com.parentoop.master.application.phases;

import com.google.common.collect.Sets;
import com.parentoop.core.data.Datum;
import com.parentoop.core.networking.Messages;
import com.parentoop.core.utils.MoreSets;
import com.parentoop.master.execution.ExecutionPhase;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ReducingPhase extends ExecutionPhase<Path> {

    private static final int MAX_POOL_SIZE = 15;
    private static final int REDUCE_RETRY_COUNT = 10;

    private Set<String> mKeysToReduce;
    private ExecutorService mExecutorService;

    private InetAddress[] mPeersAddresses;
    private Set<PeerCommunicator> mFinishedPeers;

    private Path mOutputFile;
    private PrintStream mOutputStream;

    void setKeysToReduce(Set<String> keysToReduce) {
        mKeysToReduce = new HashSet<>(keysToReduce);
    }

    @Override
    public int getPhaseCode() {
        return Messages.REDUCING;
    }

    @Override
    public Collection<Integer> getHandledMessageCodes() {
        return Sets.newHashSet(Messages.RESULT_PAIR, Messages.END_OF_RESULT_STREAM);
    }

    @Override
    public void onEnterPhase(ExecutionPhase previousPhase) throws IOException {
        if (mKeysToReduce == null) throw new IllegalStateException("Can't start reducing without keys set");
        Collection<PeerCommunicator> peers = getParticipatingPeers();
        int poolSize = Math.min(MAX_POOL_SIZE, peers.size());
        mExecutorService = Executors.newFixedThreadPool(poolSize);
        mFinishedPeers = Sets.newHashSet();
        mOutputFile = Files.createTempFile("parentoot", ".txt");
        mOutputStream = new PrintStream(new BufferedOutputStream(Files.newOutputStream(mOutputFile)));

        List<InetAddress> peerAddresses = new ArrayList<>(peers.size());
        for (PeerCommunicator peer : peers) {
            peerAddresses.add(peer.getAddress());
        }
        mPeersAddresses = peerAddresses.toArray(new InetAddress[peerAddresses.size()]);

        List<Set<String>> disjointKeys = MoreSets.disjointSubsets(mKeysToReduce, peers.size());
        Iterator<Set<String>> keysIterator = disjointKeys.iterator();
        for (PeerCommunicator peer : peers) {
            Set<String> keysForPeer = keysIterator.next();
            mExecutorService.submit(new PeerReduceSetupRunnable(peer, keysForPeer));
        }
    }

    @Override
    public void handleMessage(Message message, PeerCommunicator sender) {
        switch (message.getCode()) {
            case Messages.RESULT_PAIR:
                Datum datum = message.getData();
                mOutputStream.println(datum.getKey() + " : " + datum.getValue());
                mOutputStream.flush();
                mKeysToReduce.remove(datum.getKey());
                break;
            case Messages.END_OF_RESULT_STREAM:
                mFinishedPeers.add(sender);
                if (mFinishedPeers.containsAll(getParticipatingPeers())) {
                    if (!mKeysToReduce.isEmpty()) {
                        failExecution(new IllegalStateException("Finished but not received all keys to be reduced."));
                    } else {
                        // no need to close the stream here, it will be done on onExitPhase
                        succeedExecution(mOutputFile);
                    }
                }
                break;
        }
    }

    @Override
    public void onExitPhase(ExecutionPhase nextPhase) {
        mExecutorService.shutdownNow();
        mFinishedPeers.clear();
        mOutputStream.close();
    }

    private class PeerReduceSetupRunnable implements Runnable {

        private PeerCommunicator mPeer;
        private String[] mKeys;

        private PeerReduceSetupRunnable(PeerCommunicator peer, Collection<String> keys) {
            mPeer = peer;
            mKeys = keys.toArray(new String[keys.size()]);
        }

        @Override
        public void run() {
            IOException lastException = null;
            int retriesLeft = REDUCE_RETRY_COUNT;
            boolean successful = false, addressesSent = false;
            while (retriesLeft --> 0 && !successful) {
                try {
                    if (!addressesSent) mPeer.dispatchMessage(new Message(Messages.LOAD_SLAVE_ADDRESSES, mPeersAddresses));
                    addressesSent = true;
                    mPeer.dispatchMessage(new Message(Messages.REDUCE_KEYS, mKeys));
                    successful = true;
                } catch (IOException ex) {
                    lastException = ex;
                }
            }
            if (!successful) {
                failExecution(new IOException("One or more slaves couldn't complete reduce phase.", lastException));
            }
        }
    }
}
