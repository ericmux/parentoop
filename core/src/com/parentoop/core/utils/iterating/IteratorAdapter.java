package com.parentoop.core.utils.iterating;

import java.util.Iterator;

public class IteratorAdapter<I, O> implements Iterator<O> {

    private final Iterator<I> mIterator;
    private final Converter<I,O> mConverter;

    public IteratorAdapter(Iterator<I> iterator, Converter<I, O> converter) {
        mIterator = iterator;
        mConverter = converter;
    }

    @Override
    public boolean hasNext() {
        return mIterator.hasNext();
    }

    @Override
    public O next() {
        return mConverter.convert(mIterator.next());
    }

    @Override
    public void remove() {
        mIterator.remove();
    }

}
