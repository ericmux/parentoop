package com.parentoop.slave.listeners.master;

import com.parentoop.core.networking.Messages;
import com.parentoop.network.api.messaging.MessageRouter;

public class MasterRouter extends MessageRouter {

    public MasterRouter() {
        register(Messages.MAP_CHUNK, new MapChunk());
        register(Messages.LOAD_JAR, new JarLoader());
        register(Messages.LOAD_DESCRIPTOR, new DescriptorLoader());
    }
}
