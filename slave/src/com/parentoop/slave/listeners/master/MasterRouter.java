package com.parentoop.slave.listeners.master;

import com.parentoop.core.messaging.MessageType;
import com.parentoop.network.api.messaging.MessageRouter;

public class MasterRouter extends MessageRouter {

    public MasterRouter() {
        register(MessageType.MAP_CHUNK, new MapChunk());
        register(MessageType.LOAD_JAR, new JarLoader());
        register(MessageType.LOAD_DESCRIPTOR, new DescriptorLoader());
    }
}
