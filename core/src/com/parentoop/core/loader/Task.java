package com.parentoop.core.loader;

import com.parentoop.core.api.InputReader;
import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;

public class Task {

    private JarLoader mJarLoader;
    private Mapper mMapper;
    private Reducer mReducer;
    private InputReader mInputReader;
    private TaskDescriptor mDescriptor;

    public static Task configure(String jarPath, String taskConfiguratorClassName) throws IllegalAccessException, InstantiationException {
        JarLoader jarLoader = new JarLoader(jarPath);
        TaskConfigurator configurator = (TaskConfigurator) jarLoader.loadClass(taskConfiguratorClassName).newInstance();
        TaskDescriptor descriptor = new TaskDescriptor();
        configurator.configure(descriptor);
        return new Task(jarLoader, descriptor);
    }

    public static Task load(String jarPath, TaskDescriptor descriptor) {
        JarLoader jarLoader = new JarLoader(jarPath);
        return new Task(jarLoader, descriptor);
    }

    private Task(JarLoader jarLoader, TaskDescriptor descriptor) {
        mJarLoader = jarLoader;
        mDescriptor = descriptor;
    }

    public TaskDescriptor getDescriptor() {
        return mDescriptor;
    }

    public Mapper getMapper() {
        if (mMapper == null) {
            try {
                mMapper = (Mapper) mJarLoader.loadClass(mDescriptor.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mMapper;
    }

    public Reducer getReducer() {
        if (mReducer == null) {
            try {
                mReducer = (Reducer) mJarLoader.loadClass(mDescriptor.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mReducer;
    }

    public InputReader getInputReader() {
        if (mInputReader == null) {
            try {
                mInputReader = (InputReader) mJarLoader.loadClass(mDescriptor.getMapperClass()).newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return mInputReader;
    }
}
