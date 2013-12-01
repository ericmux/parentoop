package com.parentoop.network.api;

import java.util.HashMap;
import java.util.Map;

public enum MessageType {



    LOAD_CLASSES(0), MAP_CHUNK(1), REDUCE(2), FINALIZE(3), NOP(4), IDLE(5), RETRIEVE_KEY_VALUES(6);

    private static final Map<Integer, MessageType> MESSAGE_TYPES_MAP = new HashMap<Integer, MessageType>() {{
        for (MessageType type : MessageType.values()) {
            MESSAGE_TYPES_MAP.put(type.getId(), type);
        }
    }};

    public static MessageType fromId(int code) {
        return MESSAGE_TYPES_MAP.get(code);
    }

    private final int mId;

    private MessageType(int id) {
        mId = id;
    }

    public int getId() {
        return mId;
    }
}
