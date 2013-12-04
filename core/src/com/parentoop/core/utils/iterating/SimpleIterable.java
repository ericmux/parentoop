package com.parentoop.core.utils.iterating;

import java.util.Iterator;

public class SimpleIterable<T> implements Iterable<T> {

    private final Iterator<T> mIterator;

    public SimpleIterable(Iterator<T> iterator) {
        mIterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        return mIterator;
    }

}
