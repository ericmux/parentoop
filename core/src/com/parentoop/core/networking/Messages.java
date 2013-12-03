package com.parentoop.core.networking;

// Maybe use enum and retrieve ordinal() for sending through the network
public class Messages {

    // Configuration
    public static final int LOAD_JAR = 1;                   // client-Master Master-slave
    public static final int LOAD_DESCRIPTOR = 2;            // client-Master Master-slave

    // Mapping phase
    public static final int MAP_CHUNK = 101;                // Master-slave

    // Reducing phase
    public static final int REDUCE_ADDRESSES = 201;         // Master-slave
    public static final int RETRIEVE_VALUE = 202;           // slave-Slave
    public static final int SEND_VALUE = 203;               // Slave-slave
    public static final int END_OF_VALUE_STREAM = 204;      // Slave-slave
    public static final int SEND_RESULT_PAIR = 205;         // slave-Master
    public static final int END_OF_RESULT_STREAM = 206;     // slave-Master

    // State
    public static final int IDLE = 1001;                    // slave-Master

    // Prevents instantiation
    private Messages() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
