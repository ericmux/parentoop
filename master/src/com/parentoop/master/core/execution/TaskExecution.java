package com.parentoop.master.core.execution;

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

    public Task getTask() {
        return mTask;
    }

    public Executor getTaskExecutor() {
        return mTaskExecutor;
    }

    public synchronized Collection<PeerCommunicator> getParticipatingPeers() {
        return Collections.unmodifiableCollection(mParticipatingPeers);
    }

    public ExecutionPhase getCurrentPhase() {
        return mCurrentPhase;
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
                try {
                    ExecutionPhase previousPhase = mCurrentPhase;
                    if (mNextPhaseIdx < mExecutionPhases.size()) mCurrentPhase = mExecutionPhases.get(mNextPhaseIdx++);
                    else mCurrentPhase = null;

                    if (previousPhase != null) {
                        mSlaveMessageRouter.unregisterHandler(mMessageHandlerProxy);
                        previousPhase.onExitPhase(mCurrentPhase);
                    }
                    if (mCurrentPhase == null) {
                        mExecutionListener.onExecutionSuccessful(null);
                        return;
                    }
                    mCurrentPhase.setTaskExecution(TaskExecution.this);
                    mCurrentPhase.onEnterPhase(mCurrentPhase);
                } catch (Exception e) {
                    failExecution(e);
                    return;
                }
                for (int messageCode : mCurrentPhase.getHandledMessageCodes()) {
                    mSlaveMessageRouter.registerHandler(messageCode, mMessageHandlerProxy);
                }
                mExecutionListener.onEnterExecutionPhase(mCurrentPhase.getPhaseCode());
            }
        });
    }

    void failExecution(final Throwable throwable) {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mExecutionListener.onExecutionFailed(throwable);
                mTaskExecutor.shutdownNow();
            }
        });
    }

    void succeedExecution(final R result) {
        mTaskExecutor.execute(new Runnable() {
            @Override
            public void run() {
                mExecutionListener.onExecutionSuccessful(result);
            }
        });
    }

    private class MessageHandlerProxy implements MessageHandler {

        @Override
        public void handle(final Message message, final PeerCommunicator sender) {
            final ExecutionPhase currentPhase = getCurrentPhase();
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
