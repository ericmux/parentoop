package com.parentoop.slave.listeners.master;

import com.parentoop.network.api.messaging.MessageRouter;

public class MasterRouter extends MessageRouter {

    public MasterRouter() {
        // TODO: Use common message type from Core module
        register(-1, new MapChunk());
    }
}
