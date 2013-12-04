package com.parentoop.master.execution;

import com.google.common.collect.Sets;
import com.parentoop.core.loader.Task;
import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.NodeServer;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.network.api.messaging.MessageRouter;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CancellationException;
import java.util.concurrent.Executor;

public class TaskExecution<R> {

    private Task mTask;
    private boolean mStarted = false;
    private SingleThreadExecutor mTaskExecutor;
    private TaskExecutionListener<R> mExecutionListener;

    private NodeServer mMasterSlaveServer;
    private MessageRouter mSlaveMessageRouter;
    private MessageHandlerProxy mMessageHandlerProxy = new MessageHandlerProxy();

    private Set<PeerCommunicator> mParticipatingPeers;
    private List<ExecutionPhase<R>> mExecutionPhases;
    private ExecutionPhase<R> mCurrentPhase;
    private int mNextPhaseIdx;

    public TaskExecution(Task task, List<ExecutionPhase<R>> phases, TaskExecutionListener<R> listener) {
        mTask = task;
        mExecutionPhases = phases;
        mExecutionListener = listener;
    }

    public void setSlaveServer(NodeServer masterSlaveServer, MessageRouter slaveMessageRouter) {
        mMasterSlaveServer = masterSlaveServer;
        mSlaveMessageRouter = slaveMessageRouter;
    }

    public Task getTask() {
        return mTask;
    }

    public synchronized Collection<PeerCommunicator> getParticipatingPeers() {
        return Collections.unmodifiableCollection(mParticipatingPeers);
    }

    public int getCurrentPhaseCode() {
        return mCurrentPhase.getPhaseCode();
    }

    public void start() {
        if (mStarted) throw new IllegalStateException("Task execution can be started only once.");
        mStarted = true;
        mMasterSlaveServer.setClassLoader(mTask.getJarClassLoader());
        mParticipatingPeers = new HashSet<>(mMasterSlaveServer.getConnectedPeers());
        mTaskExecutor = new SingleThreadExecutor();
        mTaskExecutor.setUncaughtExceptionHandler(new UncaughtExceptionFailer());

        mCurrentPhase = null;
        mNextPhaseIdx = 0;
        goToNextPhase();
    }

    public void abort() {
        failExecution(new CancellationException("User cancelled task."));
    }

    Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    void retainAllPeers(final Collection<PeerCommunicator> peersToRetain) {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                HashSet<PeerCommunicator> peersToPurge = Sets.newHashSet(mParticipatingPeers);
                peersToPurge.removeAll(peersToRetain);
                synchronized (TaskExecution.this) {
                    mParticipatingPeers.retainAll(peersToRetain);
                }

                try {
                    mMasterSlaveServer.broadcastMessage(new Message(Messages.ABORT_TASK), peersToPurge);
                } catch (IOException e) {
                    // we are purging them, whatever
                }
            }
        });
    }

    void goToNextPhase() {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                ExecutionPhase<R> nextPhase;
                if (mNextPhaseIdx < mExecutionPhases.size()) nextPhase = mExecutionPhases.get(mNextPhaseIdx++);
                else nextPhase = null;

                transitionToPhase(nextPhase);
                if (nextPhase == null) {
                    mExecutionListener.onExecutionSuccessful(null);
                }
            }
        });
    }

    void failExecution(final Throwable throwable) {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                transitionToPhase(null);
                mExecutionListener.onExecutionFailed(throwable);
                mTaskExecutor.shutdownNow();
            }
        });
    }

    void succeedExecution(final R result) {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                transitionToPhase(null);
                mExecutionListener.onExecutionSuccessful(result);
                mTaskExecutor.shutdownNow();
            }
        });
    }

    private void transitionToPhase(ExecutionPhase<R> nextPhase) {
        try {
            ExecutionPhase<R> previousPhase = mCurrentPhase;
            mCurrentPhase = nextPhase;

            if (previousPhase != null) {
                mSlaveMessageRouter.unregisterHandler(mMessageHandlerProxy);
                previousPhase.onExitPhase(mCurrentPhase);
            }
            if (mCurrentPhase == null) return;

            mCurrentPhase.setTaskExecution(TaskExecution.this);
            for (int messageCode : mCurrentPhase.getHandledMessageCodes()) {
                mSlaveMessageRouter.registerHandler(messageCode, mMessageHandlerProxy);
            }
            mCurrentPhase.onEnterPhase(previousPhase);

            mExecutionListener.onEnterExecutionPhase(mCurrentPhase.getPhaseCode());
        } catch (Exception e) {
            failExecution(e);
        }
    }

    private class MessageHandlerProxy implements MessageHandler {

        @Override
        public void handle(final Message message, final PeerCommunicator sender) {
            final ExecutionPhase currentPhase = mCurrentPhase;
            if (currentPhase != null) {
                mTaskExecutor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            currentPhase.handleMessage(message, sender);
                        } catch (Exception e) {
                            failExecution(e);
                        }
                    }
                });
            }
        }
    }

    private class UncaughtExceptionFailer implements Thread.UncaughtExceptionHandler {

        @Override
        public void uncaughtException(Thread t, Throwable e) {
            failExecution(e);
        }
    }

    public interface TaskExecutionListener<R> {

        public void onEnterExecutionPhase(int code);

        public void onExecutionFailed(Throwable throwable);

        public void onExecutionSuccessful(R result);
    }
}
