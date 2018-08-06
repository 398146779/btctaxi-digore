package com.btctaxi.gate.error;

public class ConcurrentError extends RuntimeException {
    public ConcurrentError() {
        super("Concurrent error");
    }
}
