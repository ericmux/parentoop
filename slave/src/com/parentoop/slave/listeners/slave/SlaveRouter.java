package com.parentoop.slave.listeners.slave;

import com.parentoop.network.api.messaging.MessageRouter;
import com.parentoop.network.api.messaging.MessageType;

public class SlaveRouter extends MessageRouter {

    public SlaveRouter() {
        register(MessageType.RETRIEVE_KEY_VALUES, new ValueRetriever());
    }

}
