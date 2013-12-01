package com.parentoop.network.api;

import java.io.InputStream;

public interface MessageReader {

    public void read(MessageType type, InputStream inputStream);

}
