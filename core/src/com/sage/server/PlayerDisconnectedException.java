package com.sage.server;

public class PlayerDisconnectedException extends RuntimeException {
    public PlayerDisconnectedException() {
        super();
    }

    public PlayerDisconnectedException(String message) {
        super(message);
    }

    public PlayerDisconnectedException(String message, Throwable cause) {
        super(message, cause);
    }

    public PlayerDisconnectedException(Throwable cause) {
        super(cause);
    }
}
