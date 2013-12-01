package com.parentoop.storage.sqllite.result;

import java.util.Iterator;

public class ResultIterable implements Iterable<Object> {

    private Iterator<Object> mIterator;

    public ResultIterable(Iterator<Object> iterator) {
        mIterator = iterator;
    }

    @Override
    public Iterator<Object> iterator() {
        // Since we are using the same ResultSet object, two iterators could interact with each other
        if (mIterator == null) throw new IllegalAccessError("Cannot retrieve two result iterators.");
        Iterator<Object> iterator = mIterator;
        mIterator = null;
        return iterator;
    }
}
