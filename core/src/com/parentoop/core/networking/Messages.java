package com.parentoop.core.networking;

// Maybe use enum and retrieve ordinal() for sending through the network
public class Messages {

    // Configuration
    public static final int LOAD_JAR = 1;                   // client-Master Master-slave
    public static final int START_TASK = 3;                 // client-Master
    public static final int FAILURE = 4;                    // Master-client
    public static final int LOAD_DESCRIPTOR = 5;            // Master-slave

    // Mapping phase
    public static final int MAP_CHUNK = 101;                // Master-slave
    public static final int END_MAP = 102;


    // Reducing phase
    public static final int LOAD_SLAVE_ADDRESSES = 201;     // Master-slave
    public static final int REDUCE_KEYS = 202;              // Master-slave
    public static final int RETRIEVE_VALUES = 203;           // slave-Slave
    public static final int RECEIVE_VALUE = 204;               // Slave-slave
    public static final int END_OF_VALUE_STREAM = 205;      // Slave-slave

    // Finalization
    public static final int SEND_RESULT_PAIR = 301;         // slave-Master
    public static final int END_OF_RESULT_STREAM = 302;     // slave-Master
    public static final int SEND_RESULT = 303;              // Master-client

    // State
    public static final int IDLE = 1001;                    // Master-client slave-Master
    public static final int MAPPING = 1002;                 // Master-client
    public static final int REDUCING = 1003;                // Master-client
    public static final int COLLECTING = 1004;              // Master-client


    // Prevents instantiation
    private Messages() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
