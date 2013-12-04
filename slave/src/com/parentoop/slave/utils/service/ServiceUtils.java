package com.parentoop.slave.utils.service;

import java.util.Iterator;
import java.util.ServiceLoader;

public class ServiceUtils {

    // Prevents instantiation
    private ServiceUtils() {
        throw new AssertionError("Cannot instantiate object from " + this.getClass());
    }

    public static <T> T load(Class<T> service) {
        Iterator<T> i = ServiceLoader.load(service).iterator();
        if (!i.hasNext()) throw new ServiceNotAvailableException();
        return i.next();
    }

}
