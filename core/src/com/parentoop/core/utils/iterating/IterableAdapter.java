package com.parentoop.core.utils.iterating;

import java.util.Iterator;

public class IterableAdapter<I, O> implements Iterable<O> {

    private final Iterable<I> mIterable;
    private final Converter<I,O> mConverter;

    public IterableAdapter(Iterable<I> iterable, Converter<I, O> converter) {
        mIterable = iterable;
        mConverter = converter;
    }

    @Override
    public Iterator<O> iterator() {
        return new IteratorAdapter<>(mIterable.iterator(), mConverter);
    }

}
