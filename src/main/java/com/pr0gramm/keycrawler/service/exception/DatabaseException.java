package com.pr0gramm.keycrawler.service.exception;

public class DatabaseException extends RuntimeException {

    public DatabaseException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
