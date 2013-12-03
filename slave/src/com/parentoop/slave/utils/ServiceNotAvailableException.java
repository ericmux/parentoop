package com.parentoop.slave.utils;

public class ServiceNotAvailableException extends IllegalStateException {

    public ServiceNotAvailableException() {
        super();
    }

    public ServiceNotAvailableException(String s) {
        super(s);
    }

    public ServiceNotAvailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceNotAvailableException(Throwable cause) {
        super(cause);
    }
}
