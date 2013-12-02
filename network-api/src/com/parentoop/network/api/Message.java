package com.parentoop.network.api;

import java.util.Objects;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message that = (Message) o;
        return mType == that.mType && Objects.equals(mData, that.mData);
    }

    @Override
    public int hashCode() {
        int result = mType;
        result = 31 * result + (mData != null ? mData.hashCode() : 0);
        return result;
    }
}
