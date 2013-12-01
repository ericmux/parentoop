package com.parentoop.storage.sqllite.result;

import com.parentoop.storage.utils.SerializableConverter;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;

public class ResultIterator implements Iterator<Object> {

    private final ResultSet mResultSet;
    private boolean mHasNext;
    private Object mNext;

    public ResultIterator(ResultSet resultSet) {
        mResultSet = resultSet;
        move();
    }

    private void move() {
        try {
            mHasNext = mResultSet.next();
            if (mHasNext) {
                byte[] value = mResultSet.getBytes("value");
                mNext = SerializableConverter.toObject(value);
            } else {
                mResultSet.close();
            }
        } catch (SQLException | IOException | ClassNotFoundException e) {
            AssertionError error = new AssertionError();
            error.initCause(e);
            throw error;
        }

    }

    @Override
    public boolean hasNext() {
        // SQLite doesn't support mResultSet.isLast
        return mHasNext;
    }

    @Override
    public Object next() {
        if (!mHasNext) throw new IllegalAccessError();
        Object next = mNext;
        move();
        return next;
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException();
    }

}
