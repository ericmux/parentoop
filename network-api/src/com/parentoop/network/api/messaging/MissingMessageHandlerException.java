package com.parentoop.network.api.messaging;

public class MissingMessageHandlerException extends RuntimeException {

    public MissingMessageHandlerException() {
        super();
    }

    public MissingMessageHandlerException(String message) {
        super(message);
    }

    public MissingMessageHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public MissingMessageHandlerException(Throwable cause) {
        super(cause);
    }

    protected MissingMessageHandlerException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
