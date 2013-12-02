package com.parentoop.core.api;

import java.io.InputStream;
import java.io.Serializable;

public interface InputReader {

    public Iterable<? extends Serializable> read(InputStream input);
}
