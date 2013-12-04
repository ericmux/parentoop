package com.parentoop.core.api;

import java.io.Serializable;

public interface Reducer<M extends Serializable, F extends Serializable> {

    public F reduce(String key, Iterable<? extends M> values);

}
