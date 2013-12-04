package com.parentoop.core.api;

import java.io.Serializable;

public interface Mapper<T extends Serializable> {

    public void map(T chunk, MapYielder yielder);

}
