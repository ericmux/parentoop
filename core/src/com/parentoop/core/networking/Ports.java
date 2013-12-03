package com.parentoop.core.networking;

public class Ports {

    public static final int MASTER_CLIENT_PORT = 13371;
    public static final int MASTER_SLAVE_PORT = 13372;
    public static final int SLAVE_SLAVE_PORT = 13373;

    // Prevents instantiation
    private Ports() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }
}
