package com.parentoop.core.data;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataPool<V> implements Iterable<V>, Yielder<V> {
    
    private Lock mLock = new ReentrantLock();
    private Condition mStreamUpdate = mLock.newCondition();
    private List<V> mBuffer = new ArrayList<>();
    private volatile boolean mOpened = true;

    public void yield(V item) {
        if (mOpened) throw new IllegalStateException("Cannot yield data while closed.");
        mLock.lock();
        try {
            mBuffer.add(item);
            mStreamUpdate.signalAll();
        } finally {
            mLock.unlock();
        }
    }

    public void close() {
        mLock.lock();
        try {
            mOpened = false;
            mStreamUpdate.signalAll();
        } finally {
            mLock.unlock();
        }
    }

    @Override
    public Iterator<V> iterator() {
        // Problems with multiple iterators returned? throw error or return same
        return new DataPoolIterator();
    }

    private class DataPoolIterator implements Iterator<V> {

        private int mIndex = 0;

        @Override
        public boolean hasNext() {
            if (!mOpened) return false;
            mLock.lock();
            try {
                while (mOpened && mIndex == mBuffer.size() - 1) mStreamUpdate.await();
                return mIndex < mBuffer.size() - 1;
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public V next() {
            if (!hasNext()) throw new ArrayIndexOutOfBoundsException();
            mLock.lock();
            try {
                return mBuffer.get(mIndex++);
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

    }

}
