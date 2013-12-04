package com.parentoop.core.data;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class DataPool<V> implements Iterable<V>, Yielder<V> {
    
    private Lock mLock = new ReentrantLock();
    private Condition mStreamUpdate = mLock.newCondition();

    private Queue<V> mBuffer = new LinkedList<>();
    private volatile boolean mOpened = true;

    public void yield(V item) {
        if (!mOpened) throw new IllegalStateException("Cannot yield data after closed.");
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

    private boolean mIteratorReturned = false;

    @Override
    public Iterator<V> iterator() {
        if (mIteratorReturned) {
            throw new IllegalStateException("Only one iterator is allowed since it consumes the list while iterating");
        }
        mIteratorReturned = true;
        return new DataPoolIterator();
    }

    private class DataPoolIterator implements Iterator<V> {

        @Override
        public boolean hasNext() {
            mLock.lock();
            try {
                while (mOpened && mBuffer.isEmpty()) mStreamUpdate.await();
                return !mBuffer.isEmpty();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return false;
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public V next() {
            if (!hasNext()) throw new IllegalStateException();
            mLock.lock();
            try {
                return mBuffer.remove();
            } finally {
                mLock.unlock();
            }
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }


    }

    public int getBufferSize() {
        return mBuffer.size();
    }

}
