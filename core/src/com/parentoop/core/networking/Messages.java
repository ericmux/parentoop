package com.parentoop.core.networking;

// Maybe use enum and retrieve ordinal() for sending through the network
public class Messages {

    // Configuration
    public static final int LOAD_JAR = 1;                   // client-Master Master-slave
    public static final int LOAD_INPUT_PATH = 2;            // client-Master
    public static final int START_TASK = 3;                 // client-Master
    public static final int LOAD_DESCRIPTOR = 4;            // Master-slave
    public static final int FAILURE = 5;                    // Master-client
    public static final int ABORT_TASK = 6;                 // Master-slave

    // Mapping phase
    public static final int MAP_CHUNK = 101;                // Master-slave
    public static final int END_MAP = 102;

    // Reducing phase
    public static final int LOAD_SLAVE_ADDRESSES = 201;     // Master-slave
    public static final int REDUCE_KEYS = 202;              // Master-slave
    public static final int REQUEST_VALUES = 203;           // slave-Slave
    public static final int KEY_VALUE = 204;                // Slave-slave
    public static final int END_OF_DATA_STREAM = 205;       // Slave-slave

    // Finalization
    public static final int RESULT_PAIR = 301;              // slave-Master
    public static final int END_OF_RESULT_STREAM = 302;     // slave-Master
    public static final int SEND_RESULT = 303;              // Master-client

    // State
    public static final int IDLE = 1001;                    // slave-Master
    public static final int SETTING_UP = 1002;              // Master-client
    public static final int MAPPING = 1003;                 // Master-client
    public static final int REDUCING = 1004;                // Master-client
    public static final int COLLECTING = 1005;              // Master-client


    // Prevents instantiation
    private Messages() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
