package com.parentoop.storage.api;

import com.parentoop.slave.application.Finalizable;
import com.parentoop.slave.application.Initializable;

import java.io.Serializable;

public interface SlaveStorage extends Initializable, Finalizable {

    public void insert(String key, Serializable value);

    public Iterable<Object> read(String key);

}
