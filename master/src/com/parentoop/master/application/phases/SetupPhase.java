package com.parentoop.master.application.phases;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.*;
import com.parentoop.core.loader.TaskDescriptor;
import com.parentoop.core.networking.Messages;
import com.parentoop.master.execution.ExecutionPhase;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class SetupPhase extends ExecutionPhase<Path> {

    private static final int SETUP_RETRY_COUNT = 5;
    private static final int MAX_POOL_SIZE = 15;

    private ListeningScheduledExecutorService mExecutor;
    private Future<?> mNextPhaseFuture;
    private Set<PeerCommunicator> mIdlePeers;

    @Override
    public int getPhaseCode() {
        return Messages.SETTING_UP;
    }

    @Override
    public Collection<Integer> getHandledMessageCodes() {
        return Lists.newArrayList(Messages.IDLE);
    }

    @Override
    public void onEnterPhase(ExecutionPhase previousPhase) {
        int poolSize = Math.min(MAX_POOL_SIZE, getParticipatingPeers().size());
        mExecutor = MoreExecutors.listeningDecorator(Executors.newScheduledThreadPool(poolSize));
        mIdlePeers = Sets.newHashSet();

        List<ListenableFuture<PeerCommunicator>> setupFutures = new ArrayList<>();
        for (PeerCommunicator peer : getParticipatingPeers()) {
            setupFutures.add(mExecutor.submit(new PeerSetupCallable(peer)));
        }
        ListenableFuture<List<PeerCommunicator>> setupFuture = Futures.successfulAsList(setupFutures);
        Futures.addCallback(setupFuture, new SetupCallback(), getTaskExecutor());
    }

    @Override
    public void onExitPhase(ExecutionPhase nextPhase) {
        mExecutor.shutdownNow();
        mIdlePeers.clear();
    }

    @Override
    public void handleMessage(Message message, PeerCommunicator sender) {
        if (message.getCode() == Messages.IDLE) {
            mIdlePeers.add(sender);
            checkGoToNextPhase();
        }
    }

    private boolean checkGoToNextPhase() {
        if (mIdlePeers.containsAll(getParticipatingPeers())) {
            if (mNextPhaseFuture != null) mNextPhaseFuture.cancel(false);
            goToNextPhase();
            return true;
        }
        return false;
    }

    private class SetupCallback implements FutureCallback<List<PeerCommunicator>> {

        @Override
        public void onSuccess(List<PeerCommunicator> peersSetup) {
            // keep only those who have correctly setup
            retainAllPeers(peersSetup);
            if (getParticipatingPeers().size() == 0) {
                throw new IllegalStateException("No slave machines could be correctly configured.");
            }
            if (checkGoToNextPhase()) return;

            mNextPhaseFuture = mExecutor.schedule(new Runnable() {
                @Override
                public void run() {
                    // c'mon, more than 5 seconds just to set up? Who's not setup yet WILL be purged! ò.ó
                    retainAllPeers(mIdlePeers);
                    if (getParticipatingPeers().size() == 0) {
                        throw new IllegalStateException("No slave machines could be correctly configured.");
                    }
                    goToNextPhase();
                }
            }, 5, TimeUnit.SECONDS);
        }

        @Override
        public void onFailure(Throwable throwable) {
            failExecution(throwable);
        }
    }

    private class PeerSetupCallable implements Callable<PeerCommunicator> {

        private PeerCommunicator mPeer;

        private PeerSetupCallable(PeerCommunicator peer) {
            mPeer = peer;
        }

        @Override
        public PeerCommunicator call() throws Exception {
            Path jarFile = getTask().getJarFile();
            TaskDescriptor taskDescriptor = getTask().getDescriptor();

            IOException lastException = null;
            int retriesLeft = SETUP_RETRY_COUNT;
            boolean successful = false, jarSent = false;
            while (retriesLeft --> 0 && !successful) {
                try {
                    if (!jarSent) mPeer.dispatchMessage(new Message(Messages.LOAD_JAR, jarFile));
                    jarSent = true;
                    mPeer.dispatchMessage(new Message(Messages.LOAD_DESCRIPTOR, taskDescriptor));
                    successful = true;
                } catch (IOException ex) {
                    lastException = ex;
                }
            }
            if (!successful) throw lastException;
            return mPeer;
        }
    }
}
