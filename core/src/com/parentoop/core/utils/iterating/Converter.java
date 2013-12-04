package com.parentoop.core.utils.iterating;

public interface Converter<I, O> {

    public abstract O convert(I input);

}
