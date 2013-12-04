package com.parentoop.examples;

import com.parentoop.core.api.MapYielder;
import com.parentoop.core.api.Mapper;

public class WordCounterMapper implements Mapper<String> {
    @Override
    public void map(String chunk, MapYielder yielder) {
        for (String word : chunk.split(" ")) {
            word = word.replaceAll("[^A-z]", "").toLowerCase();
            if (!word.isEmpty()) {
                yielder.yield(word, 1);
            }
        }
    }
}
