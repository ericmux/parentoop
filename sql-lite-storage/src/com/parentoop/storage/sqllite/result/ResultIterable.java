package com.parentoop.storage.sqllite.result;

import java.util.Iterator;

public class ResultIterable<T> implements Iterable<T> {

    private Iterator<T> mIterator;

    public ResultIterable(Iterator<T> iterator) {
        mIterator = iterator;
    }

    @Override
    public Iterator<T> iterator() {
        // Since we are using the same ResultSet object, two iterators could interact with each other
        if (mIterator == null) throw new IllegalAccessError("Cannot retrieve two result iterators.");
        Iterator<T> iterator = mIterator;
        mIterator = null;
        return iterator;
    }
}
