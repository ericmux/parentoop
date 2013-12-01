package com.parentoop.slave.application;

import com.parentoop.storage.api.SlaveStorage;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.ServiceLoader;

public class SlaveApplication implements Initializable, Finalizable {

    public static class ServiceNotAvailableException extends IllegalStateException { /* No-op */ }

    private static class InstanceHolder {
        private static final SlaveApplication INSTANCE = new SlaveApplication();
    }

    public static SlaveApplication getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final SlaveStorage mSlaveStorage;
    private final PrintStream mOut = System.out;
    private final PrintStream mErr = System.err;

    private SlaveApplication() {
        mSlaveStorage = load(SlaveStorage.class);
    }

    public void initialize() {
        try {
            mSlaveStorage.initialize();
        } catch (Exception e) {
            mOut.println("Error during slave initialization.");
            e.printStackTrace(mErr);
        }
    }

    public void terminate() {
        try {
            mSlaveStorage.terminate();
        } catch (Exception e) {
            mOut.println("Error during slave termination.");
            e.printStackTrace(mErr);
        }
    }

    private <T> T load(Class<T> service) {
        Iterator<T> i = ServiceLoader.load(service).iterator();
        if (!i.hasNext()) throw new ServiceNotAvailableException();
        return i.next();
    }

    public SlaveStorage getSlaveStorage() {
        return mSlaveStorage;
    }

}
