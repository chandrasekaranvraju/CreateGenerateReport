package com.telemetry.process.exception;

import com.fasterxml.jackson.core.JsonProcessingException;

public class InvalidDataException extends RuntimeException {
    public InvalidDataException(Exception e) {
        super(e);
    }
}
