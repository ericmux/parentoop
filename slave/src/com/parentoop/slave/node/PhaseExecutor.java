package com.parentoop.slave.node;

import com.parentoop.network.api.Message;
import com.parentoop.network.api.PeerCommunicator;
import com.parentoop.network.api.messaging.MessageHandler;
import com.parentoop.slave.node.phases.LoadPhase;
import com.parentoop.slave.node.phases.Phase;

public class PhaseExecutor implements MessageHandler {

    private Phase mPhase;

    public PhaseExecutor() throws Exception {
        mPhase = new LoadPhase(this);
    }

    @Override
    public void handle(Message message, PeerCommunicator sender) {
        mPhase.execute(message, sender);
        mPhase = mPhase.nextPhase();
        mPhase.dispatchIdleMessage();
    }
}
