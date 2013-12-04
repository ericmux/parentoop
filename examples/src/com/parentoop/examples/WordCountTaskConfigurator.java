package com.parentoop.examples;

import com.parentoop.core.loader.TaskConfigurator;
import com.parentoop.core.loader.TaskDescriptor;

public class WordCountTaskConfigurator implements TaskConfigurator {

    @Override
    public void configure(TaskDescriptor taskDescriptor) {
        taskDescriptor.setTaskName("Word Count");
        taskDescriptor.setInputReaderClass(LineChunksInputReader.class);
        taskDescriptor.setMapperClass(WordCounterMapper.class);
        taskDescriptor.setReducerClass(WordCountReducer.class);
    }
}
