package com.myapp.reservations.exception;

import java.time.Instant;

public class ErrorResponse {

    private Instant timestamp;
    private int status;
    private String error;
    private String path;

    public ErrorResponse(int status, String error, String path) {
        this.timestamp = Instant.now();
        this.status = status;
        this.error = error;
        this.path = path;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public int getStatus() {
        return status;
    }

    public String getPath() {
        return path;
    }

    public String getError() {
        return error;
    }
}

