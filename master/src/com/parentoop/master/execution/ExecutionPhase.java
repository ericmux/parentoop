package com.parentoop.master.execution;

import com.parentoop.core.loader.Task;
import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;

import java.util.Collection;
import java.util.concurrent.Executor;

public abstract class ExecutionPhase<R> {

    private TaskExecution<R> mTaskExecution;

    // we do this not to make implementing classes have access to the TaskExecution instance and to allow
    // phase creation on TaskExecution subclass constructor
    final void setTaskExecution(TaskExecution<R> taskExecution) {
        mTaskExecution = taskExecution;
    }

    protected Task getTask() {
        return mTaskExecution.getTask();
    }

    public Executor getTaskExecutor() {
        return mTaskExecution.getTaskExecutor();
    }

    protected void goToNextPhase() {
        if (mTaskExecution.getCurrentPhase() != this) return;
        mTaskExecution.goToNextPhase();
    }

    protected void failExecution(Throwable throwable) {
        mTaskExecution.failExecution(throwable);
    }

    protected void succeedExecution(R result) {
        mTaskExecution.succeedExecution(result);
    }

    protected Collection<PeerCommunicator> getParticipatingPeers() {
        return mTaskExecution.getParticipatingPeers();
    }

    protected void retainAllPeers(Collection<PeerCommunicator> peersToRetain) {
        mTaskExecution.retainAllPeers(peersToRetain);
    }

    public abstract int getPhaseCode();

    public abstract Collection<Integer> getHandledMessageCodes();

    public abstract void onEnterPhase(ExecutionPhase previousPhase) throws Exception;

    public abstract void handleMessage(Message message, PeerCommunicator sender) throws Exception;

    public abstract void onExitPhase(ExecutionPhase nextPhase) throws Exception;
}
