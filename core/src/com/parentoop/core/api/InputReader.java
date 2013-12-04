package com.parentoop.core.api;

import com.parentoop.core.data.Yielder;

import java.io.Serializable;
import java.nio.file.Path;

public interface InputReader {

    public void read(Path file, Yielder<Serializable> chunkYielder);

}
