package com.parentoop.slave.listeners.slave;

import com.parentoop.core.messaging.MessageType;
import com.parentoop.network.api.messaging.MessageRouter;

public class SlaveRouter extends MessageRouter {

    public SlaveRouter() {
        register(MessageType.RETRIEVE_VALUE, new ValueRetriever());
    }

}
