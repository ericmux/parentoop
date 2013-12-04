package com.parentoop.examples;

import com.parentoop.core.api.Reducer;

public class WordCountReducer implements Reducer<Integer, Integer> {

    @Override
    public Integer reduce(String key, Iterable<? extends Integer> values) {
        int count = 0;
        for (Integer value : values) count += value;
        return count;
    }
}
