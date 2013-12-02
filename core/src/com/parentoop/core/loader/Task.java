package com.parentoop.core.loader;

import com.parentoop.core.api.InputReader;
import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;

public class Task {

    private JarLoader mJarLoader;
    private Mapper mMapper;
    private Reducer mReducer;
    private InputReader mInputReader;
    private TaskConfig mTaskConfig;

    public Task(String pathToTask, String taskConfigBuilderName) throws IllegalAccessException, InstantiationException {
        mJarLoader = new JarLoader(pathToTask);
        TaskConfigBuilder taskBuilder = (TaskConfigBuilder) mJarLoader.loadClass(taskConfigBuilderName).newInstance();
        mTaskConfig = new TaskConfig();
        taskBuilder.configure(mTaskConfig);
    }

    public Task(TaskConfig taskConfig) {
        mTaskConfig = taskConfig;
    }

    public TaskConfig getTaskConfig() {
        return mTaskConfig;
    }

    public Mapper getMapper() {
        if (mMapper == null) {
            try {
                mMapper = (Mapper) mJarLoader.loadClass(mTaskConfig.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        };
        return mMapper;
    }

    public Reducer getReducer() {
        if (mReducer == null) {
            try {
                mReducer = (Reducer) mJarLoader.loadClass(mTaskConfig.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        };
        return mReducer;
    }

    public InputReader getInputReader() {
        if (mInputReader == null) {
            try {
                mInputReader = (InputReader) mJarLoader.loadClass(mTaskConfig.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        };
        return mInputReader;
    }
}
