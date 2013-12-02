package com.parentoop.slave.listeners.slave;

import com.parentoop.network.api.messaging.MessageRouter;

public class SlaveRouter extends MessageRouter {

    public SlaveRouter() {
        // TODO: Use common message type from Core module
        register(-1, new ValueRetriever());
    }

}
