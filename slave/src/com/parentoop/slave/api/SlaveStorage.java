package com.parentoop.slave.api;

import java.io.Serializable;

public interface SlaveStorage<T extends Serializable> {

    public void initialize() throws Exception;

    public void terminate();

    public void insert(String key, T value);

    public Iterable<T> read(String key);

}
