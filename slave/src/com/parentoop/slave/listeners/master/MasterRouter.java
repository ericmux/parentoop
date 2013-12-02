package com.parentoop.slave.listeners.master;

import com.parentoop.network.api.messaging.MessageRouter;
import com.parentoop.network.api.messaging.MessageType;

public class MasterRouter extends MessageRouter {

    public MasterRouter() {
        register(MessageType.MAP_CHUNK, new MapChunk());
    }
}
