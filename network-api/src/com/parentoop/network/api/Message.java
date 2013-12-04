package com.parentoop.network.api;

import java.util.Objects;

public class Message {

    private final int mCode;
    private final Object mData;

    public Message(int code) {
        this(code, null);
    }

    public Message(int code, Object data) {
        mCode = code;
        mData = data;
    }

    public int getCode() {
        return mCode;
    }

    public <DataType> DataType getData() {
        return (DataType) mData;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Message that = (Message) o;
        return mCode == that.mCode && Objects.equals(mData, that.mData);
    }

    @Override
    public int hashCode() {
        int result = mCode;
        result = 31 * result + (mData != null ? mData.hashCode() : 0);
        return result;
    }
}
