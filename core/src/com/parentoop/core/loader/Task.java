package com.parentoop.core.loader;

import com.parentoop.core.api.InputReader;
import com.parentoop.core.api.Mapper;
import com.parentoop.core.api.Reducer;

import java.nio.file.Path;

public class Task {

    private Path mInputFile;
    private Path mJarFile;
    private JarLoader mJarLoader;
    private TaskDescriptor mDescriptor;

    private Mapper mMapper;
    private Reducer mReducer;
    private InputReader mInputReader;

    public static Task load(Path inputFile, Path jarFile, String taskConfiguratorClassName) {
        JarLoader jarLoader = new JarLoader(jarFile);

        TaskConfigurator configurator = null;
        try {
            Class<?> configuratorClass = jarLoader.loadClass(taskConfiguratorClassName);
            if (configuratorClass == null) return null;
            configurator = (TaskConfigurator) configuratorClass.newInstance();
        } catch (ClassCastException | ReflectiveOperationException e) {
            e.printStackTrace();
        }
        if (configurator == null) return null;

        TaskDescriptor descriptor = new TaskDescriptor();
        configurator.configure(descriptor);

        return new Task(inputFile, jarFile, descriptor, jarLoader);
    }

    public static Task load(Path inputFile, Path jarFile, TaskDescriptor descriptor) {
        return new Task(inputFile, jarFile, descriptor);
    }

    private Task(Path inputFile, Path jarFile, TaskDescriptor descriptor) {
        this(inputFile, jarFile, descriptor, new JarLoader(jarFile));
    }

    private Task(Path inputFile, Path jarFile, TaskDescriptor descriptor, JarLoader jarLoader) {
        mInputFile = inputFile;
        mJarFile = jarFile;
        mJarLoader = jarLoader;
        mDescriptor = descriptor;
    }

    public Path getJarFile() {
        return mJarFile;
    }

    public Path getInputFile() {
        return mInputFile;
    }

    public TaskDescriptor getDescriptor() {
        return mDescriptor;
    }

    public ClassLoader getJarClassLoader() {
        return mJarLoader.getClassLoader();
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
