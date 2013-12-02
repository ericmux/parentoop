package com.parentoop.network.api;

public class Message {

    private final int mType;
    private final Object mData;

    public Message(int type) {
        this(type, null);
    }

    public Message(int type, Object data) {
        mType = type;
        mData = data;
    }

    public int getType() {
        return mType;
    }

    public <DataType> DataType getData() {
        return (DataType) mData;
    }
}
