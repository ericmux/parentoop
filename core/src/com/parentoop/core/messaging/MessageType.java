package com.parentoop.core.messaging;

// Maybe use enum and retrieve ordinal() for sending through the network
public class MessageType {

    // Master to Slave
    public static int MAP_CHUNK = 1;
    public static int LOAD_DESCRIPTOR = 2;
    public static int LOAD_JAR = 3;

    // Slave to Slave
    public static int RETRIEVE_VALUE = 2;

    // Prevents instantiation
    private MessageType() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

}
