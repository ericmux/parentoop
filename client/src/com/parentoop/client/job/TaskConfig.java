package com.parentoop.client.job;

import com.parentoop.client.ui.InputReader;
import com.parentoop.client.ui.Mapper;
import com.parentoop.client.ui.Reducer;

import java.io.Serializable;

public class TaskConfig implements Serializable {

    private String mTaskName;

    private String mMapperClass;
    private String mReducerClass;
    private String mInputReaderClass;

    public String getTaskName() {
        return mTaskName;
    }

    public void setTaskName(String taskName) {
        mTaskName = taskName;
    }

    public String getMapperClass() {
        return mMapperClass;
    }

    public void setMapperClass(Class<? extends Mapper> mapperClass) {
        mMapperClass = mapperClass.getName();
    }

    public String getReducerClass() {
        return mReducerClass;
    }

    public void setReducerClass(Class<? extends Reducer> reducerClass) {
        mReducerClass = reducerClass.getName();
    }

    public String getInputReaderClass() {
        return mInputReaderClass;
    }

    public void setInputReaderClass(Class<? extends InputReader> inputReaderClass) {
        mInputReaderClass = inputReaderClass.getName();
    }
}
