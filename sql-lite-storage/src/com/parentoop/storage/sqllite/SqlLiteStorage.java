package com.parentoop.storage.sqllite;

import com.parentoop.slave.api.SlaveStorage;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SqlLiteStorage<T extends Serializable> implements SlaveStorage<T> {

    private Map<String, List<T>> mMap = new HashMap<>();

    @Override
    public void initialize() throws Exception {

    }

    @Override
    public void terminate() {

    }

    @Override
    public void insert(String key, T value) {
        List<T> list = mMap.get(key);
        if (list == null) {
            list = new ArrayList<>();
            mMap.put(key, list);
        }
        list.add(value);
    }

    @Override
    public Iterable<T> read(String key) {
        return mMap.get(key);
    }
}