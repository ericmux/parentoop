package com.parentoop.core.data;

import com.parentoop.core.api.MapYielder;
import com.parentoop.core.utils.iterating.Converter;
import com.parentoop.core.utils.iterating.IteratorAdapter;

import java.io.Serializable;
import java.util.Iterator;

public class NetworkDataPool extends DataPool<Datum> implements MapYielder, Iterable<Datum> {

    @Override
    public void yield(String key, Serializable value) {
        yield(new Datum(key, value));
    }

    private final Converter<Datum, Serializable> mConverter = new Converter<Datum, Serializable>() {
        @Override
        public Serializable convert(Datum datum) {
            return datum.getValue();
        }
    };

    public Iterator<Serializable> valueIterator() {
        return new IteratorAdapter<>(iterator(), mConverter);
    }

}
