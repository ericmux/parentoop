package com.parentoop.slave.listeners.master;

import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.messaging.MessageRouter;

public class MasterRouter extends MessageRouter {

    public MasterRouter() {
        registerHandler(Messages.MAP_CHUNK, new MapChunk());
        registerHandler(Messages.LOAD_JAR, new JarLoader());
        registerHandler(Messages.LOAD_DESCRIPTOR, new DescriptorLoader());
    }
}
