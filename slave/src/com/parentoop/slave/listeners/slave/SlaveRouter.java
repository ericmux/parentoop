package com.parentoop.slave.listeners.slave;

import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.messaging.MessageRouter;

public class SlaveRouter extends MessageRouter {

    public SlaveRouter() {
        register(Messages.RETRIEVE_VALUE, new ValueRetriever());
    }

}
