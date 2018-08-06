package com.btctaxi.gate.error;

public class BadRequestError extends RuntimeException {
    public BadRequestError() {
        super("Bad request");
    }
}
